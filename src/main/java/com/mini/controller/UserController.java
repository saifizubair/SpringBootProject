package com.mini.controller;

import com.mini.entity.User;
import com.mini.service.UserService;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/process")
    public ResponseEntity<String> processUser(@RequestParam(defaultValue = "1") int size) {
        try {
            userService.processUserData(size);
            return ResponseEntity.ok("User data processed and saved successfully!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process user data: " + e.getMessage());
        }
    }
    
    @GetMapping("")
    public ResponseEntity<List<User>> getUsers(
            @RequestParam(value = "sortType", required = false) String sortType,
            @RequestParam(value = "sortOrder", required = false) String sortOrder,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "offset", required = false) Integer offset) {

        try {
            List<User> users = userService.getUsersWithSortingAndPagination(sortType, sortOrder, limit, offset);
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    }
