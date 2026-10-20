package com.medicalclinic.controller;

import com.medicalclinic.dto.facility.FacilityCreateCommand;
import com.medicalclinic.dto.facility.FacilityDto;
import com.medicalclinic.service.FacilityService;
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
@RequestMapping("/facilities")
@Tag(name = "Facilities", description = "manages facilities")
public class FacilityController {
    private final FacilityService facilityService;

    @ApiResponse(responseCode = "200", description = "Returns list")
    @Operation(summary = "Get all facilities")
    @GetMapping
    public List<FacilityDto> findAll() {
        return facilityService.findAll();
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Facility found"),
            @ApiResponse(responseCode = "404",
                    description = "ID not found",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class)))
    })
    @Operation(summary = "Get facility by id")
    @GetMapping("/{id}")
    public FacilityDto findById(@PathVariable Long id) {
        return facilityService.findById(id);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Facility created"),
            @ApiResponse(responseCode = "400",
                    description = "Request body is not valid",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409",
                    description = "Facility with this name already exists",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class)))
    })
    @Operation(summary = "Add facility")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FacilityDto create(@Valid @RequestBody FacilityCreateCommand command) {
        return facilityService.create(command);
    }
}
