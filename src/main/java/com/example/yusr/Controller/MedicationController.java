package com.example.yusr.Controller;


import com.example.yusr.Api.ApiResponse;
import com.example.yusr.Model.Medication;
import com.example.yusr.Service.MedicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/medication")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService medicationService;

    @GetMapping("/get-medications")
    public ResponseEntity<?>getMedications(){
        return ResponseEntity.status(200).body(medicationService.getMedications());
    }

    @PostMapping("/add-medication")
    public ResponseEntity<?>addMedication(@RequestBody @Valid Medication medication){
        medicationService.addMedication(medication);
        return ResponseEntity.status(200).body(new ApiResponse("the medication is added successfully"));
    }


    @PutMapping("/update-medication/{id}")
    public ResponseEntity<?>updateMedication(@PathVariable Integer id,@RequestBody @Valid Medication medication){
        medicationService.updateMedication(id, medication);
        return ResponseEntity.status(200).body(new ApiResponse("the medication is updated successfully"));

    }

    @DeleteMapping("/delete-medication/{id}")
    public ResponseEntity<?>deleteMedication(@PathVariable Integer id){
        medicationService.deleteMedication(id);
        return ResponseEntity.status(200).body(new ApiResponse("the medication is deleted successfully"));
    }

    @PutMapping("/discontinue-medication/{medicationId}")
    public ResponseEntity<?> discontinueMedication(@PathVariable Integer medicationId){
        medicationService.discontinueMedication(medicationId);
        return ResponseEntity.status(200).body(new ApiResponse("medication is discontinued successfully"));
    }

    @GetMapping("/get-medication-Warnings/{patientId}")
    public ResponseEntity<?> getMedicationWarnings(@PathVariable Integer patientId){
        List<Medication> medications = medicationService.getMedicationWarnings(patientId);
        return ResponseEntity.status(200).body(medications);
    }

    @GetMapping("/get-patient-medications/{patientId}")
    public ResponseEntity<?> getPatientMedications(@PathVariable Integer patientId){
        List<Medication> medications = medicationService.getPatientMedications(patientId);
        return ResponseEntity.status(200).body(medications);
    }

    @PutMapping("/add-medication-stock/{medicationId}")
    public ResponseEntity<?> addMedicationStock(@PathVariable Integer medicationId, @RequestParam Integer quantity){
        medicationService.addMedicationStock(medicationId, quantity);
        return ResponseEntity.status(200).body(new ApiResponse("medication stock is added successfully"));
    }

    @PutMapping("/reactivate-medication/{medicationId}")
    public ResponseEntity<?> reactivateMedication(@PathVariable Integer medicationId, @RequestParam(required = false) LocalDate startDate, @RequestParam(required = false) LocalDate endDate){
        medicationService.reactivateMedication(medicationId, startDate, endDate);
        return ResponseEntity.status(200).body(new ApiResponse("medication is reactivated successfully"));
    }
}
