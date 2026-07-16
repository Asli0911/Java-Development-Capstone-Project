package com.project.back_end.controllers;

import com.project.back_end.models.Prescription;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.Service;

//import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController // 1. REST controller for JSON API
@RequestMapping("${api.path}prescription") // e.g. /api/prescription
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final AppointmentService appointmentService;
    private final Service service;

    // 2. Constructor injection
    //@Autowired
    public PrescriptionController(PrescriptionService prescriptionService,
                                  AppointmentService appointmentService,
                                  Service service) {
        this.prescriptionService = prescriptionService;
        this.appointmentService = appointmentService;
        this.service = service;
    }

    // 3. Save prescription for an appointment
    @PostMapping("/save/{token}")
    public ResponseEntity<Map<String, String>> savePrescription(@Valid @RequestBody Prescription prescription,
                                                                  @PathVariable String token) {
        Map<String, String> response = new HashMap<>();

        if (!service.validateToken(token, "doctor")) {
            response.put("message", "Invalid or expired token.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        try {
            // Update appointment status (e.g., to "1" meaning 'Completed')
            appointmentService.changeAppointmentStatus(prescription.getAppointmentId(), 1);
            prescriptionService.savePrescription(prescription);
            response.put("message", "Prescription saved successfully.");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            response.put("message", "Failed to save prescription: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 4. Get prescription by appointment ID
    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<?> getPrescription(@PathVariable Long appointmentId, @PathVariable String token) {
        Map<String, String> response = new HashMap<>();

        if (!service.validateToken(token, "doctor")) {
            response.put("message", "Invalid or expired token.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        return prescriptionService.getPrescription(appointmentId);
    }
}
