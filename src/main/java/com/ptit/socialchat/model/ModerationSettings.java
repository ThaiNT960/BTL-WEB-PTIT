package com.ptit.socialchat.model;

import javax.persistence.*;

@Entity
@Table(name = "moderation_settings")
public class ModerationSettings {

    @Id
    private int id = 1;

    @Column(name = "mode", length = 20, nullable = false)
    private String mode = "NONE";

    @Column(name = "ai_service_url", length = 255)
    private String aiServiceUrl = "http://localhost:8000";

    public ModerationSettings() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getAiServiceUrl() {
        return aiServiceUrl;
    }

    public void setAiServiceUrl(String aiServiceUrl) {
        this.aiServiceUrl = aiServiceUrl;
    }
}
