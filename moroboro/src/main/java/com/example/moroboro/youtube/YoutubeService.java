package com.example.moroboro.youtube;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class YoutubeService {

    private final RestClient restClient;

    public YoutubeService() {
        this.restClient = RestClient.builder().build();
    }

    /**
     * Scrapes YouTube channel metadata (ID, Title, Avatar) using the custom channel handle.
     */
    public Map<String, String> scrapeChannelMetadata(String handle) {
        if (!handle.startsWith("@")) {
            handle = "@" + handle;
        }
        Map<String, String> meta = new HashMap<>();
        meta.put("handle", handle);
        try {
            String html = restClient.get()
                    .uri("https://www.youtube.com/" + handle)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .retrieve()
                    .body(String.class);
            if (html != null) {
                // 1. Channel ID
                Pattern pattern = Pattern.compile("<meta itemprop=\"channelId\" content=\"(UC[^\"]+)\"");
                Matcher matcher = pattern.matcher(html);
                if (matcher.find()) {
                    meta.put("channelId", matcher.group(1));
                } else {
                    pattern = Pattern.compile("href=\"https://www.youtube.com/channel/(UC[^\"]+)\"");
                    matcher = pattern.matcher(html);
                    if (matcher.find()) {
                        meta.put("channelId", matcher.group(1));
                    }
                }

                // 2. Channel Title
                pattern = Pattern.compile("<meta property=\"og:title\" content=\"([^\"]+)\"");
                matcher = pattern.matcher(html);
                if (matcher.find()) {
                    meta.put("title", matcher.group(1));
                } else {
                    meta.put("title", handle);
                }

                // 3. Channel Avatar
                pattern = Pattern.compile("<meta property=\"og:image\" content=\"([^\"]+)\"");
                matcher = pattern.matcher(html);
                if (matcher.find()) {
                    meta.put("avatar", matcher.group(1));
                } else {
                    meta.put("avatar", "/assets/avatar.png");
                }
            }
        } catch (Exception e) {
            meta.put("channelId", "");
            meta.put("title", handle);
            meta.put("avatar", "/assets/avatar.png");
        }
        return meta;
    }

    /**
     * Fetches public uploads using the XML RSS feed.
     */
    public Map<String, Object> parseRssFeed(String channelId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> videos = new ArrayList<>();
        try {
            String url = "https://www.youtube.com/feeds/videos.xml?channel_id=" + channelId;
            String xml = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);
            if (xml != null) {
                Pattern entryPattern = Pattern.compile("<entry>([\\s\\S]*?)</entry>");
                Matcher entryMatcher = entryPattern.matcher(xml);

                while (entryMatcher.find()) {
                    String entryXml = entryMatcher.group(1);
                    Map<String, Object> video = new HashMap<>();

                    String id = getTagValue(entryXml, "yt:videoId");
                    String title = getTagValue(entryXml, "title");
                    String description = getTagValue(entryXml, "media:description");
                    String publishedAt = getTagValue(entryXml, "published");

                    String thumbnail = "";
                    Pattern thumbPattern = Pattern.compile("<media:thumbnail url=\"([^\"]+)\"");
                    Matcher thumbMatcher = thumbPattern.matcher(entryXml);
                    if (thumbMatcher.find()) {
                        thumbnail = thumbMatcher.group(1);
                    } else {
                        thumbnail = "https://i.ytimg.com/vi/" + id + "/mqdefault.jpg";
                    }

                    Map<String, Object> snippet = new HashMap<>();
                    snippet.put("title", title);
                    snippet.put("description", description);
                    snippet.put("publishedAt", publishedAt);
                    snippet.put("thumbnails", Map.of("medium", Map.of("url", thumbnail)));
                    
                    video.put("id", id);
                    video.put("snippet", snippet);
                    videos.add(video);
                }
            }
            result.put("items", videos);
        } catch (Exception e) {
            result.put("error", "Failed to parse channel RSS feed: " + e.getMessage());
        }
        return result;
    }

    /**
     * Scrapes public playlists of a YouTube channel using the custom handle page.
     */
    public Map<String, Object> scrapePlaylists(String handle) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> playlists = new ArrayList<>();
        if (!handle.startsWith("@")) {
            handle = "@" + handle;
        }
        try {
            String html = restClient.get()
                    .uri("https://www.youtube.com/" + handle + "/playlists")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .retrieve()
                    .body(String.class);
            if (html != null) {
                Pattern pattern = Pattern.compile("var ytInitialData = (\\{.*?\\});</script>");
                Matcher matcher = pattern.matcher(html);
                if (!matcher.find()) {
                    pattern = Pattern.compile("window\\[\"ytInitialData\"\\] = (\\{.*?\\});");
                    matcher = pattern.matcher(html);
                }

                if (matcher.find()) {
                    String jsonStr = matcher.group(1);
                    Pattern playlistPattern = Pattern.compile(
                        "\"playlistId\":\"([^\"]+)\"[\\s\\S]*?\"title\":\\{\"runs\":\\[\\{\"text\":\"([^\"]+)\"\\}\\]\\}[\\s\\S]*?\"videoCount\":\"(\\d+)\""
                    );
                    Matcher playlistMatcher = playlistPattern.matcher(jsonStr);
                    int count = 0;
                    Set<String> uniqueIds = new HashSet<>();
                    while (playlistMatcher.find() && count < 20) {
                        String id = playlistMatcher.group(1);
                        String title = playlistMatcher.group(2);
                        int videoCount = Integer.parseInt(playlistMatcher.group(3));

                        if (id.startsWith("LL") || id.startsWith("WL") || uniqueIds.contains(id)) continue;
                        uniqueIds.add(id);

                        Map<String, Object> playlistMap = new HashMap<>();
                        playlistMap.put("id", id);
                        
                        Map<String, Object> snippet = new HashMap<>();
                        snippet.put("title", title);
                        snippet.put("thumbnails", Map.of("medium", Map.of("url", "https://images.unsplash.com/photo-1611162617213-7d7a39e9b1d7?w=600&auto=format&fit=crop&q=80")));
                        
                        playlistMap.put("snippet", snippet);
                        playlistMap.put("contentDetails", Map.of("itemCount", videoCount));
                        
                        playlists.add(playlistMap);
                        count++;
                    }
                }
            }
            result.put("items", playlists);
        } catch (Exception e) {
            result.put("error", "Failed to scrape playlists: " + e.getMessage());
        }
        return result;
    }

    /**
     * Scrapes public videos from a playlist.
     */
    public Map<String, Object> scrapePlaylistItems(String playlistId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> videos = new ArrayList<>();
        try {
            String html = restClient.get()
                    .uri("https://www.youtube.com/playlist?list=" + playlistId)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .retrieve()
                    .body(String.class);
            if (html != null) {
                Pattern pattern = Pattern.compile("var ytInitialData = (\\{.*?\\});</script>");
                Matcher matcher = pattern.matcher(html);
                if (!matcher.find()) {
                    pattern = Pattern.compile("window\\[\"ytInitialData\"\\] = (\\{.*?\\});");
                    matcher = pattern.matcher(html);
                }

                if (matcher.find()) {
                    String jsonStr = matcher.group(1);
                    Pattern videoPattern = Pattern.compile(
                        "\"videoId\":\"([^\"]+)\"[\\s\\S]*?\"title\":\\{\"runs\":\\[\\{\"text\":\"([^\"]+)\"\\}\\]\\}"
                    );
                    Matcher videoMatcher = videoPattern.matcher(jsonStr);
                    int count = 0;
                    Set<String> uniqueIds = new HashSet<>();
                    while (videoMatcher.find() && count < 30) {
                        String videoId = videoMatcher.group(1);
                        String title = videoMatcher.group(2);
                        
                        if (uniqueIds.contains(videoId)) continue;
                        uniqueIds.add(videoId);

                        Map<String, Object> video = new HashMap<>();
                        video.put("id", videoId);
                        
                        Map<String, Object> snippet = new HashMap<>();
                        snippet.put("title", title);
                        snippet.put("description", "Playlist video.");
                        snippet.put("publishedAt", "2026-01-01T00:00:00Z");
                        snippet.put("thumbnails", Map.of("medium", Map.of("url", "https://i.ytimg.com/vi/" + videoId + "/mqdefault.jpg")));
                        
                        video.put("snippet", snippet);
                        videos.add(video);
                        count++;
                    }
                }
            }
            result.put("items", videos);
        } catch (Exception e) {
            result.put("error", "Failed to scrape playlist items: " + e.getMessage());
        }
        return result;
    }

    /**
     * Searches videos inside a channel using scraping.
     */
    public Map<String, Object> searchVideosScraped(String query, String handle) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> videos = new ArrayList<>();
        if (!handle.startsWith("@")) {
            handle = "@" + handle;
        }
        try {
            String url = "https://www.youtube.com/" + handle + "/search?query=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
            String html = restClient.get()
                    .uri(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .retrieve()
                    .body(String.class);
            if (html != null) {
                Pattern pattern = Pattern.compile("var ytInitialData = (\\{.*?\\});</script>");
                Matcher matcher = pattern.matcher(html);
                if (!matcher.find()) {
                    pattern = Pattern.compile("window\\[\"ytInitialData\"\\] = (\\{.*?\\});");
                    matcher = pattern.matcher(html);
                }

                if (matcher.find()) {
                    String jsonStr = matcher.group(1);
                    Pattern videoPattern = Pattern.compile(
                        "\"videoId\":\"([^\"]+)\"[\\s\\S]*?\"title\":\\{\"runs\":\\[\\{\"text\":\"([^\"]+)\"\\}\\]\\}"
                    );
                    Matcher videoMatcher = videoPattern.matcher(jsonStr);
                    int count = 0;
                    Set<String> uniqueIds = new HashSet<>();
                    while (videoMatcher.find() && count < 20) {
                        String videoId = videoMatcher.group(1);
                        String title = videoMatcher.group(2);

                        if (uniqueIds.contains(videoId)) continue;
                        uniqueIds.add(videoId);

                        Map<String, Object> video = new HashMap<>();
                        video.put("id", videoId);

                        Map<String, Object> snippet = new HashMap<>();
                        snippet.put("title", title);
                        snippet.put("description", "Search result video.");
                        snippet.put("publishedAt", "2026-01-01T00:00:00Z");
                        snippet.put("thumbnails", Map.of("medium", Map.of("url", "https://i.ytimg.com/vi/" + videoId + "/mqdefault.jpg")));

                        video.put("snippet", snippet);
                        videos.add(video);
                        count++;
                    }
                }
            }
            result.put("items", videos);
        } catch (Exception e) {
            result.put("error", "Failed to search videos: " + e.getMessage());
        }
        return result;
    }

    private String getTagValue(String xml, String tagName) {
        Pattern pattern = Pattern.compile("<" + tagName + ">([\\s\\S]*?)</" + tagName + ">");
        Matcher matcher = pattern.matcher(xml);
        if (matcher.find()) {
            return matcher.group(1).trim()
                    .replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&amp;", "&")
                    .replace("&quot;", "\"")
                    .replace("&#039;", "'");
        }
        return "";
    }
}
