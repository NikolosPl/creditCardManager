package com.github.nikolospl.creditcardmanager;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/api")
class authController {
        @RequestMapping("/login")
        public String login() {
            return "Login page";
        }
}
