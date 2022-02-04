package com.cognizant.client.controllers;

import com.cognizant.client.services.WebFluxService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebFluxController {
    private final WebFluxService webFluxService;

    public WebFluxController(WebFluxService webFluxService) {
        this.webFluxService = webFluxService;
    }

    @GetMapping("/simple/{id}")
    public void getSimple(@PathVariable Long id) {
        webFluxService.getSimpleData(id);
    }

    @GetMapping("/multiple")
    public void getMultiple() {
        webFluxService.getMultipleData();
    }
}
