package com.parshuramKund.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/applicant")
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "Working";
    }
}