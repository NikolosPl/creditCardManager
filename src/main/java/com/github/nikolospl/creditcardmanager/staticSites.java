package com.github.nikolospl.creditcardmanager;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
class staticSites {

    @RequestMapping("/")
    public String getIndex() {
        return "forward:/index.html";
    }

    @RequestMapping("/home")
    public String getHome() {
        return "forward:/home.html";
    }
}
