package com.example.moroboro.youtube;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/youtube")
@CrossOrigin(origins = "*")
public class YoutubeProxyController {

    private final YoutubeService youtubeService;

    public YoutubeProxyController(YoutubeService youtubeService) {
        this.youtubeService = youtubeService;
    }

    @GetMapping("/uploads")
    public Map<?, ?> getUploads(HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                return youtubeService.parseRssFeed(user.getChannelId());
            }
            return Map.of("error", "Not authenticated with MoroBoro.");
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/playlist-items")
    public Map<?, ?> getPlaylistItems(
            @RequestParam String playlistId,
            HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                return youtubeService.scrapePlaylistItems(playlistId);
            }
            return Map.of("error", "Not authenticated with MoroBoro.");
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/playlists")
    public Map<?, ?> getPlaylists(HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                return youtubeService.scrapePlaylists(user.getYoutubeHandle());
            }
            return Map.of("error", "Not authenticated with MoroBoro.");
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/search")
    public Map<?, ?> searchVideos(
            @RequestParam String q,
            HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                return youtubeService.searchVideosScraped(q, user.getYoutubeHandle());
            }
            return Map.of("error", "Not authenticated with MoroBoro.");
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/profile")
    public Map<String, Object> getUserProfile(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            return Map.of(
                "authenticated", true,
                "name", user.getChannelTitle(),
                "email", user.getEmail(),
                "avatar", user.getChannelAvatar()
            );
        }
        return Map.of("authenticated", false);
    }

    @GetMapping("/my-channel-details")
    public Map<?, ?> getMyChannelDetails(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Map.of("error", "Not authenticated with MoroBoro.");
        }
        
        Map<String, Object> snippet = new HashMap<>();
        snippet.put("title", user.getChannelTitle());
        snippet.put("thumbnails", Map.of("high", Map.of("url", user.getChannelAvatar())));

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("subscriberCount", "100000");

        Map<String, Object> item = new HashMap<>();
        item.put("id", user.getChannelId());
        item.put("snippet", snippet);
        item.put("statistics", statistics);

        return Map.of("items", List.of(item));
    }
}
