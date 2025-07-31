package com.example.configbackend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.example.configbackend.model.Conversation;
import com.example.configbackend.model.Message;
import com.example.configbackend.model.User;
import com.example.configbackend.repository.ConversationRepository;
import com.example.configbackend.repository.MessageRepository;
import com.example.configbackend.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository,
                          ConversationRepository conversationRepository,
                          UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    public Message sendMessage(UUID conversationId, UUID senderId, String content) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found"));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContenu(content);

        return messageRepository.save(message);
    }

    public List<Message> getMessagesByConversation(UUID conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found"));

        return messageRepository.findByConversationOrderByDateEnvoiAsc(conversation);
    }

    public void deleteMessage(UUID messageId, String currentUsername) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        if (!message.getSender().getEmail().equalsIgnoreCase(currentUsername)) {
            throw new AccessDeniedException("You are not authorized to delete this message");
        }

        messageRepository.delete(message);
    }
}
