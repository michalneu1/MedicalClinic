package com.medicalclinic.controller;

import com.medicalclinic.dto.EditPasswordCommand;
import com.medicalclinic.dto.PatientCreateCommand;
import com.medicalclinic.dto.PatientDto;
import com.medicalclinic.dto.PatientUpdateCommand;
import com.medicalclinic.service.PatientService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@AllArgsConstructor
@RequestMapping("/patients")
public class PatientController {
    private final PatientService patientService;

    @GetMapping
    public List<PatientDto> findAll() {
        return patientService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientDto create(@Valid @RequestBody PatientCreateCommand patient) {
        return patientService.create(patient);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientDto> findPatient(@PathVariable Long id) {
        return patientService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping(params = "email")
    public ResponseEntity<PatientDto> findPatientByEmail(@RequestParam String email) {
        return patientService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping("/{id}")
    public ResponseEntity<PatientDto> editPatient(@PathVariable Long id, @Valid @RequestBody PatientUpdateCommand command) {
        return patientService.update(id, command)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PatchMapping("/{id}/password")
    public ResponseEntity<PatientDto> editPassword(@PathVariable Long id, @Valid @RequestBody EditPasswordCommand command) {
        return patientService.updatePassword(id, command.password())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        return patientService.deleteById(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }



}
