package com.example.yusr.Controller;


import com.example.yusr.Api.ApiResponse;
import com.example.yusr.Model.HealthReading;
import com.example.yusr.Service.HealthReadingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/health-reading")
@RequiredArgsConstructor
public class HealthReadingController {

    private final HealthReadingService healthReadingService;

    @GetMapping("/get-health-readings")
    public ResponseEntity<?>getHealthReadings(){
        return ResponseEntity.status(200).body(healthReadingService.getHealthReadings());
    }

    @PostMapping("/add-health-reading")
    public ResponseEntity<?> addHealthReading(@RequestBody @Valid HealthReading healthReading){
        healthReadingService.addHealthReading(healthReading);
        return ResponseEntity.status(200).body(new ApiResponse("health reading is added successfully"));
    }

    @PutMapping("/update-health-reading/{id}")
    public ResponseEntity<?> updateHealthReading(@PathVariable Integer id, @RequestBody @Valid HealthReading healthReading){
        healthReadingService.updateHealthReading(id, healthReading);
        return ResponseEntity.status(200).body(new ApiResponse("health reading is updated successfully"));
    }

    @DeleteMapping("/delete-health-reading/{id}")
    public ResponseEntity<?> deleteHealthReading(@PathVariable Integer id){
        healthReadingService.deleteHealthReading(id);
        return ResponseEntity.status(200).body(new ApiResponse("health reading is deleted successfully"));
    }

    @GetMapping("/get-patient-health-readings/{patientId}")
    public ResponseEntity<?> getPatientHealthReadings(@PathVariable Integer patientId){
        List<HealthReading> healthReadings = healthReadingService.getPatientHealthReadings(patientId);
        return ResponseEntity.status(200).body(healthReadings);
    }

    @GetMapping("/get-latest-health-readings/{patientId}")
    public ResponseEntity<?> getLatestHealthReadings(@PathVariable Integer patientId){
        List<HealthReading> healthReadings = healthReadingService.getLatestHealthReadings(patientId);
        return ResponseEntity.status(200).body(healthReadings);
    }

    @GetMapping("/get-abnormal-health-readings/{patientId}")
    public ResponseEntity<?> getAbnormalHealthReadings(@PathVariable Integer patientId){
        List<HealthReading> healthReadings = healthReadingService.getAbnormalHealthReadings(patientId);
        return ResponseEntity.status(200).body(healthReadings);
    }
}
