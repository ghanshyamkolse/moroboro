package com.example.moroboro.youtube;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class YoutubeService {

    @Value("${youtube.api-key:}")
    private String apiKey;

    @Value("${youtube.channel-id:}")
    private String channelId;

    private final RestClient restClient;

    public YoutubeService() {
        this.restClient = RestClient.builder().build();
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty() && channelId != null && !channelId.trim().isEmpty();
    }

    public Map<String, Object> getConfigStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("backendConfigured", isConfigured());
        status.put("channelId", channelId);
        return status;
    }

    /**
     * Retrieves the Uploads playlist ID for a YouTube Channel.
     */
    public String getUploadsPlaylistId(String customChannelId, String customApiKey) {
        String activeChannelId = (customChannelId != null && !customChannelId.trim().isEmpty()) ? customChannelId : this.channelId;
        String activeApiKey = (customApiKey != null && !customApiKey.trim().isEmpty()) ? customApiKey : this.apiKey;

        if (activeChannelId == null || activeChannelId.trim().isEmpty() || activeApiKey == null || activeApiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("YouTube API Key and Channel ID must be configured.");
        }

        try {
            String url = UriComponentsBuilder.fromUriString("https://www.googleapis.com/youtube/v3/channels")
                    .queryParam("part", "contentDetails")
                    .queryParam("id", activeChannelId)
                    .queryParam("key", activeApiKey)
                    .toUriString();

            Map<?, ?> response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("items")) {
                List<?> items = (List<?>) response.get("items");
                if (items != null && !items.isEmpty()) {
                    Map<?, ?> item = (Map<?, ?>) items.get(0);
                    Map<?, ?> contentDetails = (Map<?, ?>) item.get("contentDetails");
                    if (contentDetails != null && contentDetails.containsKey("relatedPlaylists")) {
                        Map<?, ?> relatedPlaylists = (Map<?, ?>) contentDetails.get("relatedPlaylists");
                        if (relatedPlaylists != null && relatedPlaylists.containsKey("uploads")) {
                            return (String) relatedPlaylists.get("uploads");
                        }
                    }
                }
            }
            throw new RuntimeException("Could not retrieve uploads playlist ID from YouTube API response.");
        } catch (Exception e) {
            throw new RuntimeException("Error communicating with YouTube API to find uploads playlist: " + e.getMessage(), e);
        }
    }

    /**
     * Fetches items from a YouTube Playlist.
     */
    public Map<?, ?> fetchPlaylistItems(String playlistId, Integer maxResults, String pageToken, String customApiKey) {
        String activeApiKey = (customApiKey != null && !customApiKey.trim().isEmpty()) ? customApiKey : this.apiKey;
        int activeMax = maxResults != null ? maxResults : 20;

        if (playlistId == null || playlistId.trim().isEmpty()) {
            throw new IllegalArgumentException("Playlist ID is required.");
        }
        if (activeApiKey == null || activeApiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("YouTube API Key is required.");
        }

        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("https://www.googleapis.com/youtube/v3/playlistItems")
                    .queryParam("part", "snippet,contentDetails")
                    .queryParam("playlistId", playlistId)
                    .queryParam("maxResults", activeMax)
                    .queryParam("key", activeApiKey);

            if (pageToken != null && !pageToken.trim().isEmpty()) {
                builder.queryParam("pageToken", pageToken);
            }

            String url = builder.toUriString();
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Error fetching playlist items: " + e.getMessage());
            return err;
        }
    }

    /**
     * Fetches playlists of a channel.
     */
    public Map<?, ?> fetchPlaylists(String customChannelId, Integer maxResults, String pageToken, String customApiKey) {
        String activeChannelId = (customChannelId != null && !customChannelId.trim().isEmpty()) ? customChannelId : this.channelId;
        String activeApiKey = (customApiKey != null && !customApiKey.trim().isEmpty()) ? customApiKey : this.apiKey;
        int activeMax = maxResults != null ? maxResults : 20;

        if (activeChannelId == null || activeChannelId.trim().isEmpty() || activeApiKey == null || activeApiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("YouTube API Key and Channel ID are required.");
        }

        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("https://www.googleapis.com/youtube/v3/playlists")
                    .queryParam("part", "snippet,contentDetails")
                    .queryParam("channelId", activeChannelId)
                    .queryParam("maxResults", activeMax)
                    .queryParam("key", activeApiKey);

            if (pageToken != null && !pageToken.trim().isEmpty()) {
                builder.queryParam("pageToken", pageToken);
            }

            String url = builder.toUriString();
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Error fetching playlists: " + e.getMessage());
            return err;
        }
    }

    /**
     * Searches videos inside a channel.
     */
    public Map<?, ?> searchChannelVideos(String query, String customChannelId, Integer maxResults, String pageToken, String customApiKey) {
        String activeChannelId = (customChannelId != null && !customChannelId.trim().isEmpty()) ? customChannelId : this.channelId;
        String activeApiKey = (customApiKey != null && !customApiKey.trim().isEmpty()) ? customApiKey : this.apiKey;
        int activeMax = maxResults != null ? maxResults : 20;

        if (activeChannelId == null || activeChannelId.trim().isEmpty() || activeApiKey == null || activeApiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("YouTube API Key and Channel ID are required.");
        }

        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("https://www.googleapis.com/youtube/v3/search")
                    .queryParam("part", "snippet")
                    .queryParam("channelId", activeChannelId)
                    .queryParam("q", query)
                    .queryParam("type", "video")
                    .queryParam("maxResults", activeMax)
                    .queryParam("key", activeApiKey);

            if (pageToken != null && !pageToken.trim().isEmpty()) {
                builder.queryParam("pageToken", pageToken);
            }

            String url = builder.toUriString();
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Error searching channel videos: " + e.getMessage());
            return err;
        }
    }
}
