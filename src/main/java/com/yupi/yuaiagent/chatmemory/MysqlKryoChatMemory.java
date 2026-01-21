package com.yupi.yuaiagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.yupi.yuaiagent.domain.dto.AiChatMemoryEntity;
import com.yupi.yuaiagent.mapper.AiChatMemoryMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MysqlKryoChatMemory implements ChatMemory {


    @Autowired
    private  AiChatMemoryMapper mapper;

    private static final ThreadLocal<Kryo> KRYO = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        return kryo;
    });

    public MysqlKryoChatMemory(AiChatMemoryMapper chatMemoryMapper) {
        this.mapper=chatMemoryMapper;
    }


    @Override
    public void add(String conversationId, List<Message> messages) {
        if (conversationId == null || conversationId.isBlank() || messages == null || messages.isEmpty()) {
            return;
        }

        List<Message> all = getAll(conversationId);
        all.addAll(messages);

        byte[] blob = toBytes(all);
        mapper.upsert(conversationId, blob, all.size());
    }

    @Override
    public List<Message> get(String conversationId) {
        if (conversationId == null || conversationId.isBlank() ) {
            return List.of();
        }
        return getAll(conversationId);
    }

    @Override
    public void clear(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return;
        }
        mapper.deleteByConversationId(conversationId);
    }

    private List<Message> getAll(String conversationId) {
        AiChatMemoryEntity entity = mapper.selectByConversationId(conversationId);
        if (entity == null || entity.getMessagesBlob() == null) {
            return new ArrayList<>();
        }

        try {
            return fromBytes(entity.getMessagesBlob());
        } catch (Exception e) {
            log.warn("Failed to deserialize chat memory. convId={}", conversationId, e);
            // 反序列化失败可选择清表或返回空
            return new ArrayList<>();
        }
    }

    private static byte[] toBytes(List<Message> messages) {
        Kryo kryo = KRYO.get();
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             Output output = new Output(bos)) {
            kryo.writeObject(output, new ArrayList<>(messages));
            output.flush();
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Kryo serialize failed", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Message> fromBytes(byte[] bytes) {
        Kryo kryo = KRYO.get();
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             Input input = new Input(bis)) {
            return (ArrayList<Message>) kryo.readObject(input, ArrayList.class);
        } catch (Exception e) {
            throw new RuntimeException("Kryo deserialize failed", e);
        }
    }
}
