package com.cognizant.client.services;

import com.cognizant.client.models.Customer;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class WebFluxService {
    WebClient client;

    public WebFluxService() {
        client = WebClient.create("http://localhost:8090");
    }

    public void getSimpleData(Long id) {
        Mono<Customer> data = client.get().uri("/webflux/simple/" + id.toString()).retrieve().bodyToMono(Customer.class);
        data.subscribe(s -> LogFactory.getLog(WebFluxService.class).info(s.getName()));

        // This log should be shown before the name of the Customer, as at this point, the request has not been resolved.
        LogFactory.getLog(WebFluxService.class).info("Request completed");
    }

    public void getMultipleData() {
        Flux<Customer> data = client.get().uri("/webflux/multiple").retrieve().bodyToFlux(Customer.class);
        data.subscribe(s -> LogFactory.getLog(WebFluxService.class).info(s.getName()));
        LogFactory.getLog(WebFluxService.class).info("Request completed");
    }
}
