package com.rudraksha.shopsphere.chat.service.impl;

import com.rudraksha.shopsphere.chat.dto.ChatMessageResponse;
import com.rudraksha.shopsphere.chat.entity.ChatMessage;
import com.rudraksha.shopsphere.chat.entity.Conversation;
import com.rudraksha.shopsphere.chat.repository.ChatMessageRepository;
import com.rudraksha.shopsphere.chat.repository.ConversationRepository;
import com.rudraksha.shopsphere.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Override
    public Conversation getOrCreateConversation(Long user1Id, Long user2Id) {
        log.info("Getting or creating conversation between users: {} and {}", user1Id, user2Id);
        
        return conversationRepository.findConversationBetweenUsers(user1Id, user2Id)
            .orElseGet(() -> {
                Conversation conversation = Conversation.builder()
                    .user1Id(user1Id)
                    .user2Id(user2Id)
                    .build();
                return conversationRepository.save(conversation);
            });
    }

    @Override
    public ChatMessageResponse sendMessage(Long conversationId, Long senderId, String content) {
        log.info("Sending message to conversation: {} from user: {}", conversationId, senderId);

        Conversation conversation = getConversation(conversationId);

        ChatMessage message = ChatMessage.builder()
            .conversationId(conversationId)
            .senderId(senderId)
            .content(content)
            .isRead(false)
            .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);
        log.info("Message sent successfully with ID: {}", savedMessage.getId());

        return mapToResponse(savedMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getConversationMessages(Long conversationId, Pageable pageable) {
        log.info("Fetching messages for conversation: {}", conversationId);
        return chatMessageRepository.findByConversationId(conversationId, pageable)
            .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getUnreadMessages(Long conversationId) {
        log.info("Fetching unread messages for conversation: {}", conversationId);
        return chatMessageRepository.findUnreadMessages(conversationId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public void markAsRead(Long messageId) {
        log.info("Marking message as read: {}", messageId);
        chatMessageRepository.findById(messageId)
            .ifPresent(message -> {
                message.setIsRead(true);
                chatMessageRepository.save(message);
            });
    }

    @Override
    public void markConversationAsRead(Long conversationId) {
        log.info("Marking conversation as read: {}", conversationId);
        List<ChatMessage> unreadMessages = chatMessageRepository.findUnreadMessages(conversationId);
        unreadMessages.forEach(message -> {
            message.setIsRead(true);
            chatMessageRepository.save(message);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Conversation> getUserConversations(Long userId) {
        log.info("Fetching conversations for user: {}", userId);
        return conversationRepository.findUserConversations(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Conversation getConversation(Long conversationId) {
        log.info("Fetching conversation: {}", conversationId);
        return conversationRepository.findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation not found with ID: " + conversationId));
    }

    @Override
    public void deleteConversation(Long conversationId) {
        log.info("Deleting conversation: {}", conversationId);
        conversationRepository.deleteById(conversationId);
    }

    private ChatMessageResponse mapToResponse(ChatMessage message) {
        return ChatMessageResponse.builder()
            .id(message.getId())
            .conversationId(message.getConversationId())
            .senderId(message.getSenderId())
            .content(message.getContent())
            .isRead(message.getIsRead())
            .createdAt(message.getCreatedAt())
            .build();
    }
}
