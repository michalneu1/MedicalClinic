package com.medicalclinic.controller;

import com.medicalclinic.dto.PatientDto;
import com.medicalclinic.dto.visit.VisitCreateCommand;
import com.medicalclinic.dto.visit.VisitDto;
import com.medicalclinic.service.VisitService;
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
@RequestMapping("/visits")
@Tag(name = "Visits", description = "manages visits")
public class VisitController {
    private final VisitService visitService;

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Free slot created"),
            @ApiResponse(responseCode = "400",
                    description = "Request body is not valid",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404",
                    description = "Doctor not found",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class)))
    })
    @Operation(summary = "Add free visit slot")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VisitDto create(@Valid @RequestBody VisitCreateCommand command) {
        return visitService.create(command);
    }


    @GetMapping(params = "doctorId")
    public List<VisitDto> findByDoctorId(@RequestParam Long doctorId) {
        return visitService.findByDoctorId(doctorId);
    }
}
