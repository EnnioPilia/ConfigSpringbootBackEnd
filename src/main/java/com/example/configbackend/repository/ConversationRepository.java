package com.example.configbackend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.configbackend.model.Conversation;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    // Trouve toutes les conversations où user1 ou user2 a l'id spécifié (UUID)
    List<Conversation> findByUser1IdOrUser2Id(UUID user1Id, UUID user2Id);
}
