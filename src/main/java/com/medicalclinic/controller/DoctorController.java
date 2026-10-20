package com.medicalclinic.controller;

import com.medicalclinic.dto.doctor.DoctorCreateCommand;
import com.medicalclinic.dto.doctor.DoctorDto;
import com.medicalclinic.service.DoctorService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/doctors")
@Tag(name = "Doctors", description = "manages doctors")
public class DoctorController {
    private final DoctorService doctorService;

    @ApiResponse(responseCode = "200", description = "Returns list")
    @Operation(summary = "Get all doctors")
    @GetMapping
    public List<DoctorDto> findAll() {
        return doctorService.findAll();
    }

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Doctor created"),
            @ApiResponse(responseCode = "400",
                    description = "Request body is not valid",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404",
                    description = "User not found",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409",
                    description = "User already has a doctor",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class)))
    })
    @Operation(summary = "Add doctor to existing user")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorDto create(@Valid @RequestBody DoctorCreateCommand command) {
        return doctorService.create(command);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Doctor found"),
            @ApiResponse(responseCode = "404",
                    description = "ID not found",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class)))
    })
    @Operation(summary = "Get doctor by id")
    @GetMapping("/{id}")
    public DoctorDto findById(@PathVariable Long id) {
        return doctorService.findById(id);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Doctor deleted"),
            @ApiResponse(responseCode = "404",
                    description = "ID not found",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409",
                    description = "Doctor has visits",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class)))
    })
    @Operation(summary = "Delete doctor")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id) {
        doctorService.deleteById(id);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Facility assigned"),
            @ApiResponse(responseCode = "404",
                    description = "Doctor or facility not found",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class)))
    })
    @Operation(summary = "Assign facility to doctor")
    @PostMapping("/{id}/facilities/{facilityId}")
    public DoctorDto assignFacility(@PathVariable Long id, @PathVariable Long facilityId) {
        return doctorService.assignFacility(id, facilityId);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Facility unassigned"),
            @ApiResponse(responseCode = "404",
                    description = "Doctor or facility not found",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class)))
    })
    @Operation(summary = "Unassign facility from doctor")
    @DeleteMapping("/{id}/facilities/{facilityId}")
    public DoctorDto unassignFacility(@PathVariable Long id, @PathVariable Long facilityId) {
        return doctorService.unassignFacility(id, facilityId);
    }
}
