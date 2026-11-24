package com.medicalclinic.controller;

import com.medicalclinic.dto.BookingSummaryDto;
import com.medicalclinic.dto.PageResponse;
import com.medicalclinic.dto.visit.VisitCreateCommand;
import com.medicalclinic.dto.visit.VisitDto;
import com.medicalclinic.facade.VisitBookingFacade;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/visits")
@Tag(name = "Visits", description = "manages visits")
public class VisitController {
    private final VisitService visitService;
    private final VisitBookingFacade visitBookingFacade;

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Free slot created"),
            @ApiResponse(responseCode = "400",
                    description = "Request body is not valid",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404",
                    description = "Doctor not found",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409",
                    description = "Visit overlaps an existing one",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class)))
    })
    @Operation(summary = "Add free visit slot")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VisitDto create(@Valid @RequestBody VisitCreateCommand command) {
        return visitService.create(command);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Visit booked"),
            @ApiResponse(responseCode = "400",
                    description = "Visit is in the past or id is invalid",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404",
                    description = "Visit or patient not found",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409",
                    description = "Visit already booked",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class)))
    })
    @Operation(summary = "Book visit for patient and return booking summary")
    @PatchMapping("/{visitId}/book/{patientId}")
    public BookingSummaryDto book(@PathVariable Long visitId, @PathVariable Long patientId) {
        return visitBookingFacade.book(visitId, patientId);
    }


    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "One page of visits (no 'doctorId') or the full list of visits of the doctor",
                    content = @Content(schema = @Schema(oneOf = {PageResponse.class, VisitDto[].class}))),
            @ApiResponse(responseCode = "400",
                    description = "Sort property is not allowed (allowed: startsAt, endsAt, id) or invalid doctorId",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class)))
    })
    @Operation(summary = "List visits: page by page, or all visits of one doctor",
            description = "Without 'doctorId' returns one page of all visits (parameters: page, size, sort). "
                    + "With 'doctorId' returns the full list of visits of that doctor, without paging.")
    @GetMapping
    public Object findAll(
            @Parameter(description = "Id of the doctor; when given, paging parameters are ignored")
            @RequestParam(required = false) Long doctorId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "startsAt", direction = Sort.Direction.ASC) Pageable pageable) {
        if (doctorId != null) {
            return visitService.findByDoctorId(doctorId);
        }
        return PageResponse.from(visitService.findAll(pageable));
    }
}
