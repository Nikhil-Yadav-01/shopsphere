package com.rudraksha.shopsphere.chat.repository;

import com.rudraksha.shopsphere.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m WHERE m.conversationId = :conversationId ORDER BY m.createdAt DESC")
    Page<ChatMessage> findByConversationId(@Param("conversationId") Long conversationId, Pageable pageable);

    @Query("SELECT m FROM ChatMessage m WHERE m.conversationId = :conversationId AND m.isRead = false ORDER BY m.createdAt")
    List<ChatMessage> findUnreadMessages(@Param("conversationId") Long conversationId);

    @Query("SELECT m FROM ChatMessage m WHERE m.senderId = :senderId ORDER BY m.createdAt DESC")
    List<ChatMessage> findBySenderId(@Param("senderId") Long senderId);
}
