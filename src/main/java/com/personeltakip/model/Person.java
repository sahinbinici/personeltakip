package com.personeltakip.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "person")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "psicno", nullable = false)
    private String psicno;

    @Column(name = "tckiml", nullable = false)
    private String tckiml;

    @Column(name = "peradi")
    private String firstName;

    @Column(name = "soyadi")
    private String lastName;
}
