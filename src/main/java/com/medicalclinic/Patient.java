package com.medicalclinic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Patient {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
}
