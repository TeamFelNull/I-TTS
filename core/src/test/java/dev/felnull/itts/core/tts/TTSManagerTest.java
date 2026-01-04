package dev.felnull.itts.core.tts;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @description TTSManagerのテスト
 */
@ExtendWith(MockitoExtension.class)
class TTSManagerTest {

    @Spy
    private TTSManager ttsManager;

    @Mock
    private Guild guild;

    @Mock
    private MessageChannel messageChannel;

    @Mock
    private Member member;

    @Mock
    private Message message;

    @Test
    @DisplayName("空のメッセージの場合、sayGuildMemberTextが呼ばれない")
    void sayChat_emptyMessage_shouldNotCallSayGuildMemberText() {
        when(message.getContentRaw()).thenReturn("");

        ttsManager.sayChat(guild, messageChannel, member, message);

        verify(ttsManager, never()).sayGuildMemberText(any(), any(), any(), any());
    }
}
