package com.medicalclinic.notification;

import org.springframework.stereotype.Component;

@Component
public class SmsReminderSender implements ReminderSender {
    @Override
    public void send(String to, String message) {
        System.out.printf("Sms do <%s>: <%s>", to, message);
    }
}
