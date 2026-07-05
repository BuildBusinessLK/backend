package com.backend.repository;

import com.backend.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findTop50BySession_IdOrderByCreatedAtAsc(Long sessionId);

    void deleteBySession_Id(Long sessionId);
}
