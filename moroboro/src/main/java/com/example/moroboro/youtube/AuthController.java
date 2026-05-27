package com.example.moroboro.youtube;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final YoutubeService youtubeService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, YoutubeService youtubeService) {
        this.userRepository = userRepository;
        this.youtubeService = youtubeService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostMapping("/register")
    public Map<String, Object> register(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String youtubeHandle) {
        
        if (email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            youtubeHandle == null || youtubeHandle.trim().isEmpty()) {
            return Map.of("error", "Email, password, and YouTube handle are required.");
        }

        Optional<User> existing = userRepository.findByEmail(email.trim());
        if (existing.isPresent()) {
            return Map.of("error", "An account with this email already exists.");
        }

        // Fetch YouTube channel details
        Map<String, String> channelMeta = youtubeService.scrapeChannelMetadata(youtubeHandle.trim());
        String channelId = channelMeta.get("channelId");
        if (channelId == null || channelId.trim().isEmpty()) {
            return Map.of("error", "Could not find a valid YouTube channel for handle: " + youtubeHandle);
        }

        User user = new User();
        user.setEmail(email.trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setYoutubeHandle(channelMeta.get("handle"));
        user.setChannelId(channelId);
        user.setChannelTitle(channelMeta.get("title"));
        user.setChannelAvatar(channelMeta.get("avatar"));

        userRepository.save(user);

        return Map.of(
            "success", true,
            "message", "Registration successful. You can now log in!"
        );
    }

    @PostMapping("/login")
    public Map<String, Object> login(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session) {

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return Map.of("error", "Email and password are required.");
        }

        Optional<User> userOpt = userRepository.findByEmail(email.trim());
        if (userOpt.isEmpty()) {
            return Map.of("error", "Invalid email or password.");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Map.of("error", "Invalid email or password.");
        }

        session.setAttribute("user", user);

        return Map.of(
            "authenticated", true,
            "name", user.getChannelTitle(),
            "email", user.getEmail(),
            "avatar", user.getChannelAvatar(),
            "youtubeHandle", user.getYoutubeHandle(),
            "channelId", user.getChannelId()
        );
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        session.invalidate();
        return Map.of("success", true);
    }

    @GetMapping("/me")
    public Map<String, Object> getMe(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Map.of("authenticated", false);
        }

        return Map.of(
            "authenticated", true,
            "name", user.getChannelTitle(),
            "email", user.getEmail(),
            "avatar", user.getChannelAvatar(),
            "youtubeHandle", user.getYoutubeHandle(),
            "channelId", user.getChannelId()
        );
    }
}
