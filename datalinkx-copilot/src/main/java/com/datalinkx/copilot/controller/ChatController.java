package com.datalinkx.copilot.controller;

import com.datalinkx.common.result.WebResult;
import com.datalinkx.copilot.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/copilot")
public class ChatController {

    @Autowired
    ChatService chatService;

    @RequestMapping("/chat")
    public WebResult chat(String question) {
        return WebResult.of(chatService.chat(question));
    }

    @RequestMapping("/stream/chat")
    public SseEmitter streamChat(String question) {
        return chatService.streamChat(question);
    }
}
