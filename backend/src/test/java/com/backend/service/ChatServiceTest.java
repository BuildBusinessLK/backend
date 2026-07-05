package com.backend.service;

import com.backend.entity.ChatSession;
import com.backend.entity.User;
import com.backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock private ChatSessionRepository chatSessionRepository;
    @Mock private ChatMessageRepository chatMessageRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserProfileRepository userProfileRepository;
    @Mock private BusinessRepository businessRepository;
    @Mock private BusinessProfileRepository businessProfileRepository;
    @Mock private BusinessProductRepository businessProductRepository;
    @Mock private BusinessSocialLinkRepository businessSocialLinkRepository;
    @Mock private AiClientService aiClientService;
    @Mock private AssistantActionDetector assistantActionDetector;

    @InjectMocks private ChatService chatService;

    @Test
    void deleteSessionRemovesMessagesAndSession() {
        User user = new User();
        user.setId(2L);

        ChatSession session = new ChatSession();
        session.setId(10L);
        session.setUser(user);

        when(chatSessionRepository.findByIdAndUser_Id(10L, 2L)).thenReturn(Optional.of(session));

        chatService.deleteSession(2L, 10L);

        verify(chatMessageRepository).deleteBySession_Id(10L);
        verify(chatSessionRepository).delete(session);
    }
}
