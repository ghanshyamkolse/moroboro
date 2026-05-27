package com.example.moroboro.youtube;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
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

    /**
     * Executes YouTube API requests using OAuth2 Access Token in headers.
     */
    private Map<?, ?> callYoutubeApi(String url, String accessToken) {
        try {
            return restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Error calling YouTube API with OAuth2: " + e.getMessage());
            return err;
        }
    }

    /**
     * Fetches channels the authenticated user is subscribed to.
     */
    public Map<?, ?> fetchUserSubscriptions(String accessToken) {
        String url = UriComponentsBuilder.fromUriString("https://www.googleapis.com/youtube/v3/subscriptions")
                .queryParam("part", "snippet,contentDetails")
                .queryParam("mine", "true")
                .queryParam("maxResults", "50")
                .toUriString();
        return callYoutubeApi(url, accessToken);
    }

    /**
     * Fetches uploads from a specific YouTube Channel using OAuth2 credentials.
     */
    public Map<?, ?> fetchChannelUploads(String channelId, String accessToken) {
        String uploadsPlaylistId = channelId.startsWith("UC") ? "UU" + channelId.substring(2) : channelId;
        String url = UriComponentsBuilder.fromUriString("https://www.googleapis.com/youtube/v3/playlistItems")
                .queryParam("part", "snippet,contentDetails")
                .queryParam("playlistId", uploadsPlaylistId)
                .queryParam("maxResults", "20")
                .toUriString();
        return callYoutubeApi(url, accessToken);
    }

    /**
     * Fetches and aggregates latest uploads from top user subscriptions.
     */
    public Map<String, Object> fetchSubscribedFeed(String accessToken) {
        Map<?, ?> subsResponse = fetchUserSubscriptions(accessToken);
        Map<String, Object> feedResult = new HashMap<>();
        List<Map<String, Object>> aggregatedVideos = new ArrayList<>();

        if (subsResponse == null || subsResponse.containsKey("error")) {
            feedResult.put("error", subsResponse != null ? subsResponse.get("error") : "Failed to load subscriptions");
            return feedResult;
        }

        List<?> items = (List<?>) subsResponse.get("items");
        if (items == null || items.isEmpty()) {
            feedResult.put("items", Collections.emptyList());
            return feedResult;
        }

        // Limit to top 6 subscribed channels to optimize quota and load times
        int count = 0;
        for (Object itemObj : items) {
            if (count >= 6) break;
            Map<?, ?> item = (Map<?, ?>) itemObj;
            Map<?, ?> snippet = (Map<?, ?>) item.get("snippet");
            if (snippet == null) continue;
            Map<?, ?> resourceId = (Map<?, ?>) snippet.get("resourceId");
            if (resourceId == null) continue;
            String channelId = (String) resourceId.get("channelId");
            if (channelId == null) continue;

            Map<?, ?> uploads = fetchChannelUploads(channelId, accessToken);
            if (uploads != null && uploads.containsKey("items")) {
                List<?> videoItems = (List<?>) uploads.get("items");
                if (videoItems != null) {
                    for (Object videoObj : videoItems) {
                        Map<String, Object> video = new HashMap<>();
                        Map<?, ?> vMap = (Map<?, ?>) videoObj;
                        Map<?, ?> vSnippet = (Map<?, ?>) vMap.get("snippet");
                        if (vSnippet != null) {
                            video.put("id", vMap.get("id"));
                            video.put("snippet", vSnippet);
                            aggregatedVideos.add(video);
                        }
                    }
                }
            }
            count++;
        }

        // Sort videos chronologically (newest first)
        aggregatedVideos.sort((v1, v2) -> {
            try {
                Map<?, ?> s1 = (Map<?, ?>) v1.get("snippet");
                Map<?, ?> s2 = (Map<?, ?>) v2.get("snippet");
                String p1 = (String) s1.get("publishedAt");
                String p2 = (String) s2.get("publishedAt");
                return p2.compareTo(p1);
            } catch (Exception e) {
                return 0;
            }
        });

        feedResult.put("items", aggregatedVideos);
        return feedResult;
    }

    /**
     * Fetches playlists owned by the authenticated user.
     */
    public Map<?, ?> fetchUserPlaylists(String accessToken) {
        String url = UriComponentsBuilder.fromUriString("https://www.googleapis.com/youtube/v3/playlists")
                .queryParam("part", "snippet,contentDetails")
                .queryParam("mine", "true")
                .queryParam("maxResults", "50")
                .toUriString();
        return callYoutubeApi(url, accessToken);
    }

    /**
     * Fetches items from a playlist using OAuth2 credentials.
     */
    public Map<?, ?> fetchPlaylistItemsOAuth(String playlistId, Integer maxResults, String pageToken, String accessToken) {
        int activeMax = maxResults != null ? maxResults : 20;
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("https://www.googleapis.com/youtube/v3/playlistItems")
                .queryParam("part", "snippet,contentDetails")
                .queryParam("playlistId", playlistId)
                .queryParam("maxResults", activeMax);

        if (pageToken != null && !pageToken.trim().isEmpty()) {
            builder.queryParam("pageToken", pageToken);
        }

        return callYoutubeApi(builder.toUriString(), accessToken);
    }
}
