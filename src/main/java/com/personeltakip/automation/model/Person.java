package com.personeltakip.automation.model;

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
    @Column(name = "psicno", nullable = false)
    private Integer sicilNo;

    @Column(name = "tckiml", nullable = false)
    private Long tcKimlikNo;

    @Column(name = "peradi")
    private String adi;

    @Column(name = "soyadi")
    private String soyadi;
}
