package com.github.nikolospl.creditcardmanager;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class staticController {
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }
}
