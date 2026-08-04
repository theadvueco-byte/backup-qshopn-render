package com.synix.uwait_backend.controller.testmaildomain;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mail")
public class MailController {

    @Autowired
    private MailServiceDomain mailService;

    @PostMapping("/send")
    public String sendMail(@RequestParam String to) {
        return mailService.sendMail(to);
    }
}
