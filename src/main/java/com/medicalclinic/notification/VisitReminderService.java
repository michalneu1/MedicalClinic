package com.medicalclinic.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class VisitReminderService {
    private final ReminderSender reminderSender;
    private static final String NOTIFICATION = "Przypomnienie o wizycie jutro o 10:00.";

    public VisitReminderService(@Qualifier("smsReminderSender") ReminderSender reminderSender) {
        this.reminderSender = reminderSender;
    }


    public void remind(String to) {
        reminderSender.send(to, NOTIFICATION);
    }
}
