package com.example.configbackend.dto;

import java.util.UUID;

public class MessageDTO {
    private UUID senderId;
    private String contenu;

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }
}
