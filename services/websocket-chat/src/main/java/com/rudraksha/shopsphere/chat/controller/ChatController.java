package com.rudraksha.shopsphere.chat.controller;

import com.rudraksha.shopsphere.chat.dto.ChatMessageRequest;
import com.rudraksha.shopsphere.chat.dto.ChatMessageResponse;
import com.rudraksha.shopsphere.chat.entity.Conversation;
import com.rudraksha.shopsphere.chat.service.ChatService;
import com.rudraksha.shopsphere.shared.models.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/conversations")
    public ResponseEntity<ApiResponse<Conversation>> getOrCreateConversation(
            @RequestParam Long user1Id,
            @RequestParam Long user2Id) {
        log.info("Get or create conversation request - users: {}, {}", user1Id, user2Id);
        Conversation conversation = chatService.getOrCreateConversation(user1Id, user2Id);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(conversation, "Conversation created or retrieved successfully"));
    }

    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @RequestParam Long conversationId,
            @RequestParam Long senderId,
            @RequestBody @Valid ChatMessageRequest request) {
        log.info("Send message request - conversation: {}, sender: {}", conversationId, senderId);
        ChatMessageResponse response = chatService.sendMessage(conversationId, senderId, request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Message sent successfully"));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<Page<ChatMessageResponse>>> getConversationMessages(
            @PathVariable Long conversationId,
            Pageable pageable) {
        log.info("Get conversation messages request - conversation: {}", conversationId);
        Page<ChatMessageResponse> messages = chatService.getConversationMessages(conversationId, pageable);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @GetMapping("/conversations/{conversationId}/unread")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getUnreadMessages(
            @PathVariable Long conversationId) {
        log.info("Get unread messages request - conversation: {}", conversationId);
        List<ChatMessageResponse> messages = chatService.getUnreadMessages(conversationId);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PutMapping("/messages/{messageId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long messageId) {
        log.info("Mark message as read request - message: {}", messageId);
        chatService.markAsRead(messageId);
        return ResponseEntity.ok(ApiResponse.success("Message marked as read"));
    }

    @PutMapping("/conversations/{conversationId}/read")
    public ResponseEntity<ApiResponse<Void>> markConversationAsRead(
            @PathVariable Long conversationId) {
        log.info("Mark conversation as read request - conversation: {}", conversationId);
        chatService.markConversationAsRead(conversationId);
        return ResponseEntity.ok(ApiResponse.success("Conversation marked as read"));
    }

    @GetMapping("/users/{userId}/conversations")
    public ResponseEntity<ApiResponse<List<Conversation>>> getUserConversations(
            @PathVariable Long userId) {
        log.info("Get user conversations request - user: {}", userId);
        List<Conversation> conversations = chatService.getUserConversations(userId);
        return ResponseEntity.ok(ApiResponse.success(conversations));
    }

    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<ApiResponse<Conversation>> getConversation(
            @PathVariable Long conversationId) {
        log.info("Get conversation request - conversation: {}", conversationId);
        Conversation conversation = chatService.getConversation(conversationId);
        return ResponseEntity.ok(ApiResponse.success(conversation));
    }

    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(
            @PathVariable Long conversationId) {
        log.info("Delete conversation request - conversation: {}", conversationId);
        chatService.deleteConversation(conversationId);
        return ResponseEntity.ok(ApiResponse.success("Conversation deleted successfully"));
    }

    @PostMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Chat Service is running"));
    }
}
