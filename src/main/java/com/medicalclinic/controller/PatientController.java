package com.medicalclinic.controller;

import com.medicalclinic.model.Patient;
import com.medicalclinic.service.PatientService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/patients")
public class PatientController {
    private final PatientService patientService;

    @GetMapping
    public List<Patient> findAll() {
        return patientService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Patient create(@RequestBody Patient patient) {
        return patientService.create(patient);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patient> findPatient(@PathVariable Long id) {
        return patientService.findById(id)
                .map(patient -> ResponseEntity.ok(patient))
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping(params = "email")
    public ResponseEntity<Patient> findPatientByEmail(@RequestParam String email) {
        return patientService.findByEmail(email)
                .map(patient -> ResponseEntity.ok(patient))
                .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping("/{id}")
    public ResponseEntity<Patient> editPatient(@PathVariable Long id, @RequestBody Patient patient) {
        return patientService.update(id, patient)
                .map(editedPatient -> ResponseEntity.ok(editedPatient))
                .orElse(ResponseEntity.notFound().build());
    }
    @PatchMapping("/{id}/password")
    public ResponseEntity<Patient> editPassword(@PathVariable Long id, @RequestBody Map<String,String> patient) {
        return patientService.updatePassword(id, patient.get("password"))
                .map(editedPatient -> ResponseEntity.ok(editedPatient))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        return patientService.deleteById(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }



}
