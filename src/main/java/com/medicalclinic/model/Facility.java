package com.medicalclinic.model;

import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Facility other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Facility.class.hashCode();
    }
}
