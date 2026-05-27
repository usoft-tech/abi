package com.usoft.framework.ai.store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.usoft.framework.ai.api.ConversationMessageQueryRequest;
import com.usoft.framework.ai.api.ConversationMessageResponse;
import com.usoft.framework.ai.service.ConversationMessageService;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DbChatMemoryStore implements ChatMemoryStore {

    private final ConversationMessageService conversationMessageService;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String conversationId = (String) memoryId;
        ConversationMessageQueryRequest req = new ConversationMessageQueryRequest();
        req.setConversationId(conversationId);
        req.setLimit(20);
        List<ConversationMessageResponse> list = new ArrayList<>(conversationMessageService.list(req));
        Collections.reverse(list);

        List<ChatMessage> messages = new LinkedList<>();
        list.forEach(item -> {
            messages.add(UserMessage.from(item.getPrompt()));
            messages.add(AiMessage.from(item.getAnswer()));
        });
        return messages;
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
    }

    @Override
    public void deleteMessages(Object memoryId) {
    }

}
