package com.cognizant.server.repositories;

import com.cognizant.server.entities.Customer;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.converters.uni.UniReactorConverters;
import org.hibernate.reactive.mutiny.Mutiny;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

@Repository
public class CustomerRepository {

    private Mutiny.SessionFactory getSessionFactory() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("HibernateReactive");
        return emf.unwrap(Mutiny.SessionFactory.class);
    }

    private Uni<Mutiny.StatelessSession> getSession() {
        return getSessionFactory().openStatelessSession();
    }

    private <T> Mono<T> persist(T item) {
        return getSessionFactory().withTransaction((session, transaction) -> session.persist(item))
                .convert()
                .with(UniReactorConverters.toMono())
                .thenReturn(item);
    }

    public Mono<Customer> getCustomerById(Long id) {
        return getSession().chain(session -> session.get(Customer.class, id)
                        .eventually(session::close))
                .convert()
                .with(UniReactorConverters.toMono());
    }

    public Flux<Customer> findAll() {
        return getSession().chain(session -> session.createQuery("from Customer", Customer.class)
                        .getResultList()
                        .eventually(session::close))
                .convert()
                .with(UniReactorConverters.toMono()) // Mono<List<Customer>>
                .flatMapMany(Flux::fromIterable);
    }

    // TODO: Implement criteria queries

    public Mono<Customer> updateCustomer(Long id, Customer c) {
        return getSession().chain(session -> session.get(Customer.class, id)
                        .call(customer -> {
                            if (customer != null && c.getId().equals(id)) {
                                customer.setName(c.getName());
                                return session.update(customer);
                            } else return Uni.createFrom().nullItem();
                        }).eventually(session::close))
                .convert()
                .with(UniReactorConverters.toMono());
    }

    public Mono<Customer> createCustomer(String name) {
        Customer c = new Customer();
        c.setName(name);

        return persist(c);
    }

    public Mono<Customer> removeCustomer(Long id) {
        return getSession().chain(session -> session.get(Customer.class, id)
                        .call(customer -> {
                            if (customer != null) {
                                return session.delete(customer);
                            } else return Uni.createFrom().nullItem();
                        }).eventually(session::close))
                .convert()
                .with(UniReactorConverters.toMono());
                // .switchIfEmpty(Mono.error(new Exception()));
    }

}
