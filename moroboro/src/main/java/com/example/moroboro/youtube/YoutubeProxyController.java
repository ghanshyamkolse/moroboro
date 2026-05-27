package com.example.moroboro.youtube;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/youtube")
@CrossOrigin(origins = "*")
public class YoutubeProxyController {

    private final YoutubeService youtubeService;
    private final OAuth2AuthorizedClientService clientService;

    public YoutubeProxyController(YoutubeService youtubeService, OAuth2AuthorizedClientService clientService) {
        this.youtubeService = youtubeService;
        this.clientService = clientService;
    }

    private String getAccessToken(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken token) {
            OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                token.getAuthorizedClientRegistrationId(),
                token.getName()
            );
            if (client != null && client.getAccessToken() != null) {
                return client.getAccessToken().getTokenValue();
            }
        }
        return null;
    }

    @GetMapping("/config-status")
    public Map<String, Object> getConfigStatus() {
        return youtubeService.getConfigStatus();
    }

    @GetMapping("/uploads")
    public Map<?, ?> getUploads(
            @RequestParam(required = false) String channelId,
            @RequestParam(required = false) Integer maxResults,
            @RequestParam(required = false) String pageToken,
            @RequestParam(required = false) String apiKey) {
        try {
            String uploadsPlaylistId = youtubeService.getUploadsPlaylistId(channelId, apiKey);
            return youtubeService.fetchPlaylistItems(uploadsPlaylistId, maxResults, pageToken, apiKey);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/playlist-items")
    public Map<?, ?> getPlaylistItems(
            @RequestParam String playlistId,
            @RequestParam(required = false) Integer maxResults,
            @RequestParam(required = false) String pageToken,
            @RequestParam(required = false) String apiKey,
            Authentication authentication) {
        try {
            String token = getAccessToken(authentication);
            if (token != null) {
                return youtubeService.fetchPlaylistItemsOAuth(playlistId, maxResults, pageToken, token);
            }
            return youtubeService.fetchPlaylistItems(playlistId, maxResults, pageToken, apiKey);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/playlists")
    public Map<?, ?> getPlaylists(
            @RequestParam(required = false) String channelId,
            @RequestParam(required = false) Integer maxResults,
            @RequestParam(required = false) String pageToken,
            @RequestParam(required = false) String apiKey,
            Authentication authentication) {
        try {
            String token = getAccessToken(authentication);
            if (token != null) {
                return youtubeService.fetchUserPlaylists(token);
            }
            return youtubeService.fetchPlaylists(channelId, maxResults, pageToken, apiKey);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/search")
    public Map<?, ?> searchVideos(
            @RequestParam String q,
            @RequestParam(required = false) String channelId,
            @RequestParam(required = false) Integer maxResults,
            @RequestParam(required = false) String pageToken,
            @RequestParam(required = false) String apiKey) {
        try {
            return youtubeService.searchChannelVideos(q, channelId, maxResults, pageToken, apiKey);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/subscribed-feed")
    public Map<?, ?> getSubscribedFeed(Authentication authentication) {
        try {
            String token = getAccessToken(authentication);
            if (token == null) {
                return Map.of("error", "Not authenticated with Google.");
            }
            return youtubeService.fetchSubscribedFeed(token);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/subscriptions")
    public Map<?, ?> getSubscriptions(Authentication authentication) {
        try {
            String token = getAccessToken(authentication);
            if (token == null) {
                return Map.of("error", "Not authenticated with Google.");
            }
            return youtubeService.fetchUserSubscriptions(token);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/channel-uploads")
    public Map<?, ?> getChannelUploads(
            @RequestParam String channelId,
            Authentication authentication) {
        try {
            String token = getAccessToken(authentication);
            if (token == null) {
                return Map.of("error", "Not authenticated with Google.");
            }
            return youtubeService.fetchChannelUploads(channelId, token);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/profile")
    public Map<String, Object> getUserProfile(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken token) {
            OAuth2User principal = token.getPrincipal();
            return Map.of(
                "authenticated", true,
                "name", principal.getAttribute("name") != null ? principal.getAttribute("name") : "",
                "email", principal.getAttribute("email") != null ? principal.getAttribute("email") : "",
                "avatar", principal.getAttribute("picture") != null ? principal.getAttribute("picture") : ""
            );
        }
        return Map.of("authenticated", false);
    }
}
