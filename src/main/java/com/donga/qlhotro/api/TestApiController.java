package com.donga.qlhotro.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestApiController {

    @GetMapping("/test")
    public Map<String, Object> testApi() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "API hoạt động bình thường");
        return response;
    }
}
