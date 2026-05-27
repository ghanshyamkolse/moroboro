package com.example.moroboro.youtube;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/youtube")
@CrossOrigin(origins = "*")
public class YoutubeProxyController {

    private final YoutubeService youtubeService;

    public YoutubeProxyController(YoutubeService youtubeService) {
        this.youtubeService = youtubeService;
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
            @RequestParam(required = false) String apiKey) {
        try {
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
            @RequestParam(required = false) String apiKey) {
        try {
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
}
