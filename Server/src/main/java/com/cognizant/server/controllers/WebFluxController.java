package com.cognizant.server.controllers;

import com.cognizant.server.entities.Customer;
import com.cognizant.server.repositories.CustomerRepository;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/webflux")
public class WebFluxController {
    private final CustomerRepository repository;

    public WebFluxController(CustomerRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/simple/{id}")
    public Mono<Customer> getSimpleData(@PathVariable Long id) {
        return repository.getCustomerById(id);
    }

    @GetMapping("/multiple")
    public Flux<Customer> getMultipleData() {
        return repository.findAll();
    }

    @GetMapping("/multiple/search")
    public Flux<Customer> getMultipleByText(@RequestParam String text) {
        return repository.findByName(text);
    }

    @PostMapping("/insert")
    public Mono<Customer> insertData(@RequestBody String name) {
        return repository.createCustomer(name);
    }

    @PutMapping("/update/{id}")
    public Mono<Customer> updateData(@PathVariable Long id, @RequestBody Customer customer) {
        return repository.updateCustomer(id, customer);
    }

    @DeleteMapping("/delete/{id}")
    public Mono<Customer> deleteData(@PathVariable Long id) {
        return repository.removeCustomer(id);
    }
}
