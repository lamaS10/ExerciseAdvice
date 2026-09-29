package com.example.yusr.Controller;


import com.example.yusr.Api.ApiResponse;
import com.example.yusr.Model.MedicationLog;
import com.example.yusr.Service.MedicationLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medication-log")
@RequiredArgsConstructor
public class MedicationLogController {
    private final MedicationLogService medicationLogService;

    @GetMapping("/get-medication-logs")
    public ResponseEntity<?>getMedicationLogs(){
        return ResponseEntity.status(200).body(medicationLogService.getMedicationLogs());
    }

    @PostMapping("/add-medication-log")
    public ResponseEntity<?>addMedicationLog(@RequestBody @Valid MedicationLog medicationLog){
        medicationLogService.addMedicationLog(medicationLog);
        return ResponseEntity.status(200).body(new ApiResponse("the medication log is added successfully"));
    }


    @PutMapping("/update-medication-log/{id}")
    public ResponseEntity<?>updateMedicationLog(@PathVariable Integer id,@RequestBody @Valid MedicationLog medicationLog){
        medicationLogService.updateMedicationLog(id, medicationLog);
        return ResponseEntity.status(200).body(new ApiResponse("the medication log is updated successfully"));

    }


    @DeleteMapping("/delete-medication-log/{id}")
    public ResponseEntity<?>deleteMedicationLog(@PathVariable Integer id){
        medicationLogService.deleteMedicationLog(id);
        return ResponseEntity.status(200).body(new ApiResponse("the medication log is deleted successfully"));
    }

    @GetMapping("/get-today-medication-summary/{patientId}")
    public ResponseEntity<?> getTodayMedicationSummary(@PathVariable Integer patientId){
        String summary = medicationLogService.getTodayMedicationSummary(patientId);
        return ResponseEntity.status(200).body(new ApiResponse(summary));
    }

    @GetMapping("/get-today-medication-logs/{patientId}")
    public ResponseEntity<?> getTodayMedicationLogs(@PathVariable Integer patientId){
        List<MedicationLog> medicationLogs = medicationLogService.getTodayMedicationLogs(patientId);
        return ResponseEntity.status(200).body(medicationLogs);
    }
}
