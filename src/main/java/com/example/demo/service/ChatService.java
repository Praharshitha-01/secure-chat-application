package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

import com.example.demo.model.ChatMessage;
import com.example.demo.repository.ChatRepository;

@Service
public class ChatService {

    @Autowired
    private ChatRepository repo;

    public void save(String sender, String receiver, String content) {
        ChatMessage msg = new ChatMessage();
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setContent(content);
        repo.save(msg);
    }

    public List<ChatMessage> getAll() {
        return repo.findAll();
    }
}