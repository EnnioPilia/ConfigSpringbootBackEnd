package com.example.configbackend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.configbackend.model.Conversation;
import com.example.configbackend.model.User;
import com.example.configbackend.repository.ConversationRepository;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;

    public ConversationService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    public Conversation createConversation(User currentUser, Conversation conversation) {
        if (conversation.getUser2() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User2 must be provided");
        }
        conversation.setUser1(currentUser);
        return conversationRepository.save(conversation);
    }

    // Passage de Long à UUID ici
    public Conversation getConversationById(UUID id, User currentUser) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        boolean isParticipant = conversation.getUser1().getId().equals(currentUser.getId())
                || conversation.getUser2().getId().equals(currentUser.getId());

        if (!isParticipant) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        return conversation;
    }

    // Passage de Long à UUID ici aussi
    public void deleteConversation(UUID id, User currentUser) {
        Conversation conversation = getConversationById(id, currentUser);
        conversationRepository.delete(conversation);
    }

    // Idem pour les ids utilisateurs (UUID)
    public List<Conversation> getConversationsByUserId(UUID userId) {
        return conversationRepository.findByUser1IdOrUser2Id(userId, userId);
    }

    public List<Conversation> getAllConversations() {
        return conversationRepository.findAll();
    }
}
