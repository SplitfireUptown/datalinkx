package com.datalinkx.copilot.controller;

import com.datalinkx.common.result.WebResult;
import com.datalinkx.copilot.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/copilot")
public class ChatController {

    @Autowired
    ChatService chatService;

    @RequestMapping("/chat")
    public WebResult chat(String question) {
        return WebResult.of(chatService.chat(question));
    }
}
