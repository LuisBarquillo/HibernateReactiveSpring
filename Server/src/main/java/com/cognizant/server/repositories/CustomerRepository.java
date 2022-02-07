package com.cognizant.server.repositories;

import com.cognizant.server.entities.City;
import com.cognizant.server.entities.City_;
import com.cognizant.server.entities.Customer;
import com.cognizant.server.entities.Customer_;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.converters.uni.UniReactorConverters;
import org.hibernate.reactive.mutiny.Mutiny;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

@Repository
public class CustomerRepository {

    private Mutiny.SessionFactory getSessionFactory() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("HibernateReactive");
        return emf.unwrap(Mutiny.SessionFactory.class);
    }

    private Uni<Mutiny.StatelessSession> getSession() {
        return getSessionFactory().openStatelessSession();
    }

    private CriteriaBuilder getCriteriaBuilder() {
        return getSessionFactory().getCriteriaBuilder();
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

    public Flux<Customer> findByCity(String searchTerm) {
        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Customer> query = cb.createQuery(Customer.class);
        Root<Customer> root = query.from(Customer.class);
        Join<Customer, City> city = root.join(Customer_.CITY);
        query.where(cb.like(city.get(City_.NAME), "%" + searchTerm + "%"));

        return getSession().chain(session -> session.createQuery(query)
                        .getResultList()
                        .eventually(session::close))
                .convert()
                .with(UniReactorConverters.toMono())
                .flatMapMany(Flux::fromIterable);
    }

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
