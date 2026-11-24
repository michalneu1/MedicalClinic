package com.medicalclinic.facade;

import com.medicalclinic.dto.BookingSummaryDto;
import com.medicalclinic.dto.PatientDto;
import com.medicalclinic.dto.doctor.DoctorDto;
import com.medicalclinic.dto.visit.VisitDto;
import com.medicalclinic.service.DoctorService;
import com.medicalclinic.service.PatientService;
import com.medicalclinic.service.VisitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitBookingFacade {
    private final VisitService visitService;
    private final PatientService patientService;
    private final DoctorService doctorService;

    @Transactional
    public BookingSummaryDto book(Long visitId, Long patientId) {
        VisitDto visit = visitService.book(visitId, patientId);
        PatientDto patient = patientService.findById(patientId);
        DoctorDto doctor = doctorService.findById(visit.doctorId());
        long freeSlotsThatDay = visitService.countFreeSlots(visit.doctorId(), visit.startsAt().toLocalDate());
        log.debug("built booking summary for visit {} of doctor {}", visit.id(), visit.doctorId());
        return new BookingSummaryDto(visit, patient, doctor, freeSlotsThatDay);
    }
}
