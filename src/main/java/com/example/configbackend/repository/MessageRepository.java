package com.example.configbackend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.configbackend.model.Conversation;
import com.example.configbackend.model.Message;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByConversationOrderByDateEnvoiAsc(Conversation conversation);
}
