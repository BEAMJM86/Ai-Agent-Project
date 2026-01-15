package com.yupi.yuaiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.model.Media;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.regex.Pattern;

@Slf4j
public class BannedWordsAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    public enum Mode {
        /** 命中直接拦截（推荐） */
        BLOCK,
        /** 命中将违禁词替换为 ***，继续调用 */
        MASK
    }

    private final List<Pattern> bannedPatterns;
    private final Mode mode;
    private final int order;

    public BannedWordsAdvisor(Collection<String> bannedWords, Mode mode, int order) {
        this.bannedPatterns = compile(bannedWords);
        this.mode = mode == null ? Mode.BLOCK : mode;
        this.order = order;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        advisedRequest = before(advisedRequest);
        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);
        // 需要的话你也可以在这里做 after()，比如审计 assistant 输出
        return advisedResponse;
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        advisedRequest = before(advisedRequest);
        return chain.nextAroundStream(advisedRequest);
        // 如需对流式输出做二次过滤/审计，可以 aggregate 后处理（参考 MessageAggregator）
    }

    private AdvisedRequest before(AdvisedRequest request) {
        String userText = request.userText();
        if (userText == null) {
            return request;
        }

        Optional<String> hit = firstHit(userText);
        if (hit.isEmpty()) {
            return request;
        }

        String word = hit.get();
        log.warn("BannedWordsAdvisor hit banned word: {}", word);

        if (mode == Mode.BLOCK) {
            // 直接拦截：让 controller 返回 400/业务错误
            throw new IllegalArgumentException("输入包含违禁词，已被拦截");
        }

        // mode == MASK：替换后继续走下游模型
        String masked = mask(userText);
        // 同时建议把 messages 里最后一条 user message 也同步替换，避免模型看到原文
        List<Message> newMessages = replaceLastUserMessage(request.messages(), masked, request.media());

        return AdvisedRequest.from(request)
                .userText(masked)
                .messages(newMessages)
                .build();
    }

    private Optional<String> firstHit(String text) {
        for (Pattern p : bannedPatterns) {
            var m = p.matcher(text);
            if (m.find()) {
                return Optional.of(m.group());
            }
        }
        return Optional.empty();
    }

    private String mask(String text) {
        String result = text;
        for (Pattern p : bannedPatterns) {
            result = p.matcher(result).replaceAll("***");
        }
        return result;
    }

    private static List<Message> replaceLastUserMessage(List<Message> messages, String newText, List<Media> media) {
        if (messages == null || messages.isEmpty()) {
            return messages;
        }
        List<Message> copy = new ArrayList<>(messages);

        for (int i = copy.size() - 1; i >= 0; i--) {
            Message msg = copy.get(i);
            if (msg instanceof UserMessage) {
                copy.set(i, new UserMessage(newText, media));
                break;
            }
        }
        return copy;
    }

    private static List<Pattern> compile(Collection<String> words) {
        if (words == null || words.isEmpty()) {
            return List.of();
        }
        List<Pattern> patterns = new ArrayList<>();
        for (String w : words) {
            if (w == null || w.isBlank()) continue;
            // 用 Pattern.quote 防止关键词里出现正则特殊字符
            patterns.add(Pattern.compile(Pattern.quote(w), Pattern.CASE_INSENSITIVE));
        }
        return patterns;
    }

    public static class Builder {
        private final List<String> bannedWords = new ArrayList<>();
        private Mode mode = Mode.BLOCK;
        private int order = 0;

        public Builder addWord(String word) {
            this.bannedWords.add(word);
            return this;
        }

        public Builder addWords(Collection<String> words) {
            if (words != null) this.bannedWords.addAll(words);
            return this;
        }

        public Builder mode(Mode mode) {
            this.mode = mode;
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public BannedWordsAdvisor build() {
            return new BannedWordsAdvisor(bannedWords, mode, order);
        }
    }
}
