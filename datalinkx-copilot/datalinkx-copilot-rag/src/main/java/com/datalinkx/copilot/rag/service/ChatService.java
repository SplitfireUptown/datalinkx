package com.datalinkx.copilot.rag.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ChatService {

    String chat(String prompt);

    SseEmitter streamChat(String question);
}
