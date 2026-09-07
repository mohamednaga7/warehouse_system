package com.mohamednagah.warehouse_api.controller;

import com.mohamednagah.warehouse_api.model.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @GetMapping("/info")
    public ResponseEntity<AuthenticatedUser> getUserInfo(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(principal);
    }
}
