package com.example.configbackend.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.configbackend.dto.MessageDTO;
import com.example.configbackend.model.Message;
import com.example.configbackend.service.MessageService;

@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    // Create a new message in a conversation
    @PostMapping("/send/{conversationId}")
    public ResponseEntity<Message> sendMessage(
            @PathVariable UUID conversationId,
            @RequestBody MessageDTO messageDto) {

        Message message = messageService.sendMessage(
                conversationId,
                messageDto.getSenderId(),
                messageDto.getContenu());

        return ResponseEntity.ok(message);
    }

    // Get all messages from a conversation
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<Message>> getMessagesByConversation(
            @PathVariable UUID conversationId) {

        List<Message> messages = messageService.getMessagesByConversation(conversationId);
        return ResponseEntity.ok(messages);
    }

    // Delete a message (with access control)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable UUID id, Principal principal) {
        messageService.deleteMessage(id, principal.getName()); // uses email/username
        return ResponseEntity.noContent().build();
    }  
}
