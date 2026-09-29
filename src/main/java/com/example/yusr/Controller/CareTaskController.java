package com.example.yusr.Controller;


import com.example.yusr.Api.ApiResponse;
import com.example.yusr.Model.CareTask;
import com.example.yusr.Service.CareTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/care-task")
@RequiredArgsConstructor
public class CareTaskController {

    private final CareTaskService careTaskService;


    @GetMapping("/get-care-task")
    public ResponseEntity<?>getCareTasks(){
        return ResponseEntity.status(200).body(careTaskService.getCareTasks());
    }


    @PostMapping("/add-care-task")
    public ResponseEntity<?>addCareTask(@RequestBody @Valid CareTask careTask){
        careTaskService.addCareTask(careTask);
        return ResponseEntity.status(200).body(new ApiResponse("care task added is successfully"));
    }


    @PutMapping("/update-care-task/{id}")
    public ResponseEntity<?>updateCareTask(@PathVariable Integer id,@RequestBody @Valid CareTask careTask){
        careTaskService.updateCareTask(id, careTask);
        return ResponseEntity.status(200).body(new ApiResponse("care task is updated successfully"));
    }


    @DeleteMapping("/delete-care-task/{id}")
    public ResponseEntity<?>deleteCareTask(@PathVariable Integer id){
        careTaskService.deleteCareTask(id);
        return ResponseEntity.status(200).body(new ApiResponse("care task is deleted successfully"));
    }

    @PutMapping("/complete-care-task/{id}")
    public ResponseEntity<?> completeCareTask(@PathVariable Integer id, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime completedAt){
        careTaskService.completeCareTask(id, completedAt);
        return ResponseEntity.status(200).body(new ApiResponse("care task is completed successfully"));
    }

    @PutMapping("/cancel-care-task/{id}")
    public ResponseEntity<?> cancelCareTask(@PathVariable Integer id){
        careTaskService.cancelCareTask(id);
        return ResponseEntity.status(200).body(new ApiResponse("care task is cancelled successfully"));
    }

    @GetMapping("/get-patient-care-tasks/{patientId}")
    public ResponseEntity<?> getPatientCareTasks(@PathVariable Integer patientId){
        List<CareTask> careTasks = careTaskService.getPatientCareTasks(patientId);
        return ResponseEntity.status(200).body(careTasks);
    }


}
