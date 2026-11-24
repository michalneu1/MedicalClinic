package com.medicalclinic.controller;

import com.medicalclinic.dto.EditPasswordCommand;
import com.medicalclinic.dto.PatientCreateCommand;
import com.medicalclinic.dto.PatientDto;
import com.medicalclinic.dto.PatientUpdateCommand;
import com.medicalclinic.dto.visit.VisitDto;
import com.medicalclinic.exception.AmbiguousPatientQueryException;
import com.medicalclinic.service.PatientService;
import com.medicalclinic.service.VisitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/patients")
@Tag(name = "Patients", description = "manages patients")
public class PatientController {
    private final PatientService patientService;
    private final VisitService visitService;


    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "List of patients (no parameters or 'fragment') or a single patient ('email')",
                    content = @Content(schema = @Schema(oneOf = {PatientDto[].class, PatientDto.class}))),
            @ApiResponse(responseCode = "400",
                    description = "Both 'email' and 'fragment' were given",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404",
                    description = "Email not found",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class)))
    })
    @Operation(summary = "Get patients: all, one by email, or search by name fragment",
            description = "Without parameters returns all patients. With 'email' returns the single patient "
                    + "with that e-mail. With 'fragment' returns patients whose first or last name contains it. "
                    + "The parameters are mutually exclusive: sending both is rejected with 400.")
    @GetMapping
    public Object find(
            @Parameter(description = "Exact e-mail of the patient; cannot be combined with 'fragment'")
            @RequestParam(required = false) String email,
            @Parameter(description = "Fragment of first or last name; cannot be combined with 'email'")
            @RequestParam(required = false) String fragment) {
        if (email != null && fragment != null) {
            throw new AmbiguousPatientQueryException();
        }
        if (email != null) {
            return patientService.findByEmail(email);
        }
        if (fragment != null) {
            return patientService.searchByFragment(fragment);
        }
        return patientService.findAll();
    }

    @ApiResponses({
            @ApiResponse(responseCode = "201",description = "Patient created"),
            @ApiResponse(responseCode = "409",
                    description = "Patient with this email or id card number already exists",
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
            @ApiResponse(responseCode = "200",
                    description = "Visits of the patient, empty list when there are none"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid id",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404",
                    description = "ID not found",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class)))
    })
    @Operation(summary = "List visits booked by patient, earliest first")
    @GetMapping("/{id}/visits")
    public List<VisitDto> findVisits(@PathVariable Long id) {
        return visitService.findByPatientId(id);
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
            @ApiResponse(responseCode = "204",description = "Patient and their account deleted"),
            @ApiResponse(responseCode = "404",
                    description = "ID not found",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409",
                    description = "Patient has visits",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class)))
    })
    @Operation(summary = "Delete patient")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id) {
        patientService.deleteById(id);
    }


}
