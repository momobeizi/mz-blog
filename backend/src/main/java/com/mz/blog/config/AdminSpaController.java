package com.mz.blog.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminSpaController {

    @GetMapping({"/admin", "/admin/"})
    public String adminIndex() {
        return "forward:/admin/index.html";
    }
}
