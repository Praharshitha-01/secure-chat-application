package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.example.demo.model.ChatMessage;
import com.example.demo.service.ChatService;
import com.example.demo.security.AESUtil;

@RestController
@RequestMapping("/messages")
public class ChatRestController {

    @Autowired
    private ChatService service;

    @GetMapping
    public List<ChatMessage> getAllMessages() {

        List<ChatMessage> list = service.getAll();

        list.forEach(msg -> {
            try {
                msg.setContent(AESUtil.decrypt(msg.getContent()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return list;
    }
}