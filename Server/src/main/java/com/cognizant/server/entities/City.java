package com.cognizant.server.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Table(name = "city")
@Entity
@Getter
@Setter
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name="id")
    private Long id;

    @Column(name="name")
    private String name;
}
