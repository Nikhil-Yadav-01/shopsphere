package com.rudraksha.shopsphere.chat.service;

import com.rudraksha.shopsphere.chat.dto.ChatMessageResponse;
import com.rudraksha.shopsphere.chat.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatService {

    Conversation getOrCreateConversation(Long user1Id, Long user2Id);

    ChatMessageResponse sendMessage(Long conversationId, Long senderId, String content);

    Page<ChatMessageResponse> getConversationMessages(Long conversationId, Pageable pageable);

    List<ChatMessageResponse> getUnreadMessages(Long conversationId);

    void markAsRead(Long messageId);

    void markConversationAsRead(Long conversationId);

    List<Conversation> getUserConversations(Long userId);

    Conversation getConversation(Long conversationId);

    void deleteConversation(Long conversationId);
}
