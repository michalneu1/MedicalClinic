package com.medicalclinic.controller;

import com.medicalclinic.dto.BookingSummaryDto;
import com.medicalclinic.dto.PageResponse;
import com.medicalclinic.dto.visit.VisitCreateCommand;
import com.medicalclinic.dto.visit.VisitDto;
import com.medicalclinic.facade.VisitBookingFacade;
import com.medicalclinic.mapper.VisitMapper;
import com.medicalclinic.service.VisitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    private final VisitBookingFacade visitBookingFacade;
    private final VisitMapper visitMapper;

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


    @Operation(summary = "List visits page by page")
    @GetMapping
    public PageResponse<VisitDto> findAll(
            @PageableDefault(size = 20, sort = "startsAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return PageResponse.from(visitService.findAll(pageable).map(visitMapper::toDto));
    }

    @GetMapping(params = "doctorId")
    public List<VisitDto> findByDoctorId(@RequestParam Long doctorId) {
        return visitService.findByDoctorId(doctorId);
    }
}
