package com.medicalclinic;

import com.medicalclinic.notification.VisitReminderService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class MedicalClinicApplication {
    public static void main(String[] args) {
        var context = SpringApplication.run(MedicalClinicApplication.class, args);

//        context.getBean(VisitReminderService.class).remind("anna.kowalska@example.com");

//        String[] names = context.getBeanDefinitionNames();
//        Arrays.sort(names);
//        for (String name : names) {
//            System.out.println(name);
//        }


    }
}
