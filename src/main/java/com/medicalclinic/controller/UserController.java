package com.medicalclinic.controller;

import com.medicalclinic.dto.user.UserCreateCommand;
import com.medicalclinic.dto.user.UserDto;
import com.medicalclinic.service.UserService;
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
@RequestMapping("/users")
@Tag(name = "Users", description = "manages user accounts")
public class UserController {
    private final UserService userService;

    @ApiResponse(responseCode = "200", description = "Returns list")
    @Operation(summary = "Get all users")
    @GetMapping
    public List<UserDto> findAll() {
        return userService.findAll();
    }

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(responseCode = "409",
                    description = "User with this email already exists",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "400",
                    description = "Request body is not valid",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class)))
    })
    @Operation(summary = "Add user")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody UserCreateCommand command) {
        return userService.create(command);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid id",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404",
                    description = "ID not found",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class)))
    })
    @Operation(summary = "Get user by id")
    @GetMapping("/{id}")
    public UserDto findUser(@PathVariable Long id) {
        return userService.findById(id);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted"),
            @ApiResponse(responseCode = "404",
                    description = "ID not found",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409",
                    description = "User is assigned to a patient",
                    content = @Content(schema =
                    @Schema(implementation = ProblemDetail.class)))
    })
    @Operation(summary = "Delete user")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id) {
        userService.deleteById(id);
    }
}
