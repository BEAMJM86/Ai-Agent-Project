package com.yupi.yuaiagent.controller;

import com.yupi.yuaiagent.app.LoveApp;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/chat")
public class ImageChatController {

    private final LoveApp loveApp;

    public ImageChatController(LoveApp loveApp) {
        this.loveApp = loveApp;
    }

    @PostMapping("/image")
    public String chatWithImage(@RequestParam("chatId") String chatId,
                                @RequestParam("message") String message,
                                @RequestParam("image") MultipartFile image) throws Exception {


        return loveApp.doChatWithImage(message,image,chatId);
    }
}
