package com.example.configbackend.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.configbackend.model.Conversation;
import com.example.configbackend.model.User;
import com.example.configbackend.service.ConversationAccessService;
import com.example.configbackend.service.ConversationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/conversation")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private ConversationAccessService accessService;

    @GetMapping("/{id}")
    public ResponseEntity<Conversation> getConversationById(@PathVariable Long id, Principal principal) {
        User currentUser = accessService.getCurrentUser(principal);
        Conversation conversation = conversationService.getConversationById(id, currentUser);
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Conversation>> getAllConversations() {
        return ResponseEntity.ok(conversationService.getAllConversations());
    }

    @PostMapping
    public ResponseEntity<Conversation> createConversation(
            Principal principal,
            @RequestBody @Valid Conversation conversation) {

        User currentUser = accessService.getCurrentUser(principal);
        Conversation saved = conversationService.createConversation(currentUser, conversation);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConversation(@PathVariable Long id, Principal principal) {
        User currentUser = accessService.getCurrentUser(principal);
        conversationService.deleteConversation(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
