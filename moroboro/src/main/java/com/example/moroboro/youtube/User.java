package com.example.moroboro.youtube;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("app_user")
public class User {

    @Id
    private Long id;
    private String email;
    private String password;
    private String youtubeHandle;
    private String channelId;
    private String channelTitle;
    private String channelAvatar;

    public User() {
    }

    public User(String email, String password, String youtubeHandle, String channelId, String channelTitle, String channelAvatar) {
        this.email = email;
        this.password = password;
        this.youtubeHandle = youtubeHandle;
        this.channelId = channelId;
        this.channelTitle = channelTitle;
        this.channelAvatar = channelAvatar;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getYoutubeHandle() { return youtubeHandle; }
    public void setYoutubeHandle(String youtubeHandle) { this.youtubeHandle = youtubeHandle; }

    public String getChannelId() { return channelId; }
    public void setChannelId(String channelId) { this.channelId = channelId; }

    public String getChannelTitle() { return channelTitle; }
    public void setChannelTitle(String channelTitle) { this.channelTitle = channelTitle; }

    public String getChannelAvatar() { return channelAvatar; }
    public void setChannelAvatar(String channelAvatar) { this.channelAvatar = channelAvatar; }
}
