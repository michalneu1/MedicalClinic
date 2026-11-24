package com.medicalclinic.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VisitReminderService {
    private static final String NOTIFICATION = "Przypomnienie o wizycie jutro o 10:00.";
    @Qualifier("smsReminderSender")
    private final ReminderSender reminderSender;

    public void remind(String to) {
        reminderSender.send(to, NOTIFICATION);
    }
}
