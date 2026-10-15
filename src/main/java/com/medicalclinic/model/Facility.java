package com.medicalclinic.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "facilities")
public class Facility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false)
    private String city;
    private String zipCode;
    private String street;
    private String number;
    @ManyToMany(mappedBy = "facilities", fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    private Set<Doctor> doctors = new HashSet<>();
}
