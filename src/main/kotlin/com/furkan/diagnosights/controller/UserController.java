package com.furkan.diagnosights.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;


@RestController
@RequestMapping("/")
public class UserController {
    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(Authentication authentication) {
        return Map.of("user", Objects.requireNonNull(authentication).getName());

    }
}
