package com.example.configbackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.configbackend.model.Conversation;
import com.example.configbackend.model.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationOrderByDateEnvoiAsc(Conversation conversation);
}
