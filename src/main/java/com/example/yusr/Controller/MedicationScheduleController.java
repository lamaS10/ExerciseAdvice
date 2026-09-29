package com.example.yusr.Controller;


import com.example.yusr.Api.ApiResponse;
import com.example.yusr.Model.MedicationSchedule;
import com.example.yusr.Service.MedicationScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medication-schedule")
@RequiredArgsConstructor
public class MedicationScheduleController {

    private final MedicationScheduleService medicationScheduleService;

    @GetMapping("/get-medication-schedules")
    public ResponseEntity<?>getMedicationSchedules(){
        return ResponseEntity.status(200).body(medicationScheduleService.getMedicationSchedules());
    }

    @PostMapping("/add-medication-schedule")
    public ResponseEntity<?>addMedicationSchedule(@RequestBody @Valid MedicationSchedule medicationSchedule){
        medicationScheduleService.addMedicationSchedule(medicationSchedule);
        return ResponseEntity.status(200).body(new ApiResponse("medication schedule is added successfully"));
    }


    @PutMapping("/update-medication-schedule/{id}")
    public ResponseEntity<?>updateMedicationSchedule(@PathVariable Integer id,@RequestBody @Valid MedicationSchedule medicationSchedule){
        medicationScheduleService.updateMedicationSchedule(id, medicationSchedule);
        return ResponseEntity.status(200).body(new ApiResponse("Medication Schedule is updated successfully"));
    }


    @DeleteMapping("/delete-medication-schedule/{id}")
    public ResponseEntity<?>deleteMedicationSchedule(@PathVariable Integer id){
        medicationScheduleService.deleteMedicationSchedule(id);
        return ResponseEntity.status(200).body(new ApiResponse("Medication Schedule is deleted successfully"));
    }

    @GetMapping("/get-today-medication-schedule/{patientId}")
    public ResponseEntity<?> getTodayMedicationSchedule(@PathVariable Integer patientId){
        List<MedicationSchedule> schedules = medicationScheduleService.getTodayMedicationSchedule(patientId);
        return ResponseEntity.status(200).body(schedules);
    }

}
