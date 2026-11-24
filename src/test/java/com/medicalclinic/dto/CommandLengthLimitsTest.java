package com.medicalclinic.dto;

import com.medicalclinic.dto.doctor.DoctorCreateCommand;
import com.medicalclinic.dto.facility.FacilityCreateCommand;
import com.medicalclinic.dto.user.UserCreateCommand;
import com.medicalclinic.model.Role;
import com.medicalclinic.model.Specialization;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandLengthLimitsTest {
    private static final LocalDate BIRTHDAY = LocalDate.of(1990, 1, 1);
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeFactory() {
        factory.close();
    }

    @Test
    void patientCreate_valuesExactlyAtLimits_hasNoViolations() {
        // given
        String email = "a".repeat(64) + "@" + "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(58) + ".com";
        PatientCreateCommand command = new PatientCreateCommand(email, "p".repeat(72), "ABC123456",
                "J".repeat(100), "K".repeat(100), "123456789", BIRTHDAY);

        // when
        List<String> messages = messagesOf(command);

        // then
        assertEquals(255, email.length());
        assertEquals(List.of(), messages);
    }

    @Test
    void patientCreate_valuesOneOverLimits_reportsEachField() {
        // given
        PatientCreateCommand command = new PatientCreateCommand("a".repeat(244) + "@example.com",
                "p".repeat(73), "ABC123456", "J".repeat(101), "K".repeat(101), "123456789", BIRTHDAY);

        // when
        List<String> messages = messagesOf(command);

        // then
        assertTrue(messages.contains("email must be at most 255 characters long"));
        assertTrue(messages.contains("password must be at most 72 bytes long"));
        assertTrue(messages.contains("first name must be at most 100 characters long"));
        assertTrue(messages.contains("last name must be at most 100 characters long"));
    }

    @Test
    void patientCreate_passwordUnder72CharactersButOver72Bytes_reportsPassword() {
        // given
        String password = "ż".repeat(40);
        PatientCreateCommand command = new PatientCreateCommand("jan@example.com", password, "ABC123456",
                "Jan", "Kowalski", "123456789", BIRTHDAY);

        // when
        List<String> messages = messagesOf(command);

        // then
        assertEquals(80, password.getBytes(StandardCharsets.UTF_8).length);
        assertEquals(List.of("password must be at most 72 bytes long"), messages);
    }

    @Test
    void patientUpdate_namesOneOverLimit_reportsBothNames() {
        // given
        PatientUpdateCommand command = new PatientUpdateCommand("J".repeat(101), "K".repeat(101),
                "123456789", BIRTHDAY);

        // when
        List<String> messages = messagesOf(command);

        // then
        assertEquals(List.of("first name must be at most 100 characters long",
                "last name must be at most 100 characters long"), messages);
    }

    @Test
    void editPassword_passwordOver72Bytes_reportsPassword() {
        // given
        EditPasswordCommand command = new EditPasswordCommand("p".repeat(73));

        // when
        List<String> messages = messagesOf(command);

        // then
        assertEquals(List.of("password must be at most 72 bytes long"), messages);
    }

    @Test
    void userCreate_emailAndPasswordOverLimits_reportsBoth() {
        // given
        UserCreateCommand command = new UserCreateCommand("a".repeat(244) + "@example.com",
                "p".repeat(73), Role.PATIENT);

        // when
        List<String> messages = messagesOf(command);

        // then
        assertTrue(messages.contains("email must be at most 255 characters long"));
        assertTrue(messages.contains("password must be at most 72 bytes long"));
    }

    @Test
    void facilityCreate_allFieldsOneOverLimit_reportsAllFive() {
        // given
        String tooLong = "x".repeat(256);
        FacilityCreateCommand command = new FacilityCreateCommand(tooLong, tooLong, tooLong, tooLong, tooLong);

        // when
        List<String> messages = messagesOf(command);

        // then
        assertEquals(List.of("city must be at most 255 characters long",
                "name must be at most 255 characters long",
                "number must be at most 255 characters long",
                "street must be at most 255 characters long",
                "zip code must be at most 255 characters long"), messages);
    }

    @Test
    void facilityCreate_optionalFieldsMissing_hasNoViolations() {
        // given
        FacilityCreateCommand command = new FacilityCreateCommand("Przychodnia", "Poznan", null, null, null);

        // when
        List<String> messages = messagesOf(command);

        // then
        assertEquals(List.of(), messages);
    }

    @Test
    void doctorCreate_namesOneOverLimit_reportsBothNames() {
        // given
        DoctorCreateCommand command = new DoctorCreateCommand(1L, "J".repeat(101), "K".repeat(101),
                Specialization.CARDIOLOGIST);

        // when
        List<String> messages = messagesOf(command);

        // then
        assertEquals(List.of("first name must be at most 100 characters long",
                "last name must be at most 100 characters long"), messages);
    }

    private <T> List<String> messagesOf(T command) {
        return validator.validate(command).stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .toList();
    }
}
