package com.medicalclinic.controller;

import com.medicalclinic.dto.EditPasswordCommand;
import com.medicalclinic.dto.PatientCreateCommand;
import com.medicalclinic.dto.PatientDto;
import com.medicalclinic.dto.PatientUpdateCommand;
import com.medicalclinic.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@AllArgsConstructor
@RequestMapping("/patients")
@Tag(name = "Patients", description = "manages patients")
public class PatientController {
    private final PatientService patientService;


    @ApiResponse(responseCode = "200", description = "Returns list")
    @Operation(summary = "Get all patients")
    @GetMapping
    public List<PatientDto> findAll() {
        return patientService.findAll();
    }

    @ApiResponses({
            @ApiResponse(responseCode = "201",description = "Patient created"),
            @ApiResponse(responseCode = "409",
                    description = "Patient with this email already exists",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "400",
                    description = "Request body is not valid",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class)))
    })
    @Operation(summary = "Add patient")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientDto create(@Valid @RequestBody PatientCreateCommand patient) {
        return patientService.create(patient);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200",description = "Patient found"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid id",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404",
                    description = "ID not found",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class)))
    })
    @Operation(summary = "Get patient by id")
    @GetMapping("/{id}")
    public PatientDto findPatient(@PathVariable Long id) {
        return patientService.findById(id);

    }

    @ApiResponses({
            @ApiResponse(responseCode = "200",description = "Patient found"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid email",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404",
                    description = "Email not found",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class)))
    })
    @Operation(summary = "Get patient by email")
    @GetMapping(params = "email")
    public PatientDto findPatientByEmail(@RequestParam String email) {
        return patientService.findByEmail(email);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200",description = "Patient updated"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid request id or body",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404",
                    description = "ID not found",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class)))
    })
    @Operation(summary = "Update patient")
    @PutMapping("/{id}")
    public PatientDto editPatient(@PathVariable Long id, @Valid @RequestBody PatientUpdateCommand command) {
        return patientService.update(id, command);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200",description = "Password changed"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid id",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404",
                    description = "ID not found",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class)))
    })
    @Operation(summary = "Change password")
    @PatchMapping("/{id}/password")
    public PatientDto editPassword(@PathVariable Long id, @Valid @RequestBody EditPasswordCommand command) {
        return patientService.updatePassword(id, command.password());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "204",description = "Patient deleted"),
            @ApiResponse(responseCode = "404",
                    description = "ID not found",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class)))
    })
    @Operation(summary = "Delete patient")
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        patientService.deleteById(id);
    }


}
