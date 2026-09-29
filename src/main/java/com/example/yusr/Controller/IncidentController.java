package com.example.yusr.Controller;


import com.example.yusr.Api.ApiResponse;
import com.example.yusr.Model.Incident;
import com.example.yusr.Service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/incident")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping("/get-incidents")
    public ResponseEntity<?>getIncidents(){
        return ResponseEntity.status(200).body(incidentService.getIncidents());
    }

    @PostMapping("/add-incident")
    public ResponseEntity<?>addIncident(@RequestBody @Valid Incident incident){
        incidentService.addIncident(incident);
        return ResponseEntity.status(200).body(new ApiResponse("incident is added successfully"));
    }


    @PutMapping("/update-incident/{id}")
    public ResponseEntity<?>updateIncident(@PathVariable Integer id,@RequestBody @Valid Incident incident){
        incidentService.updateIncident(id, incident);
        return ResponseEntity.status(200).body(new ApiResponse("incident is updated successfully"));
    }


    @DeleteMapping("/delete-incident/{id}")
    public ResponseEntity<?>deleteIncident(@PathVariable Integer id){
        incidentService.deleteIncident(id);
        return ResponseEntity.status(200).body(new ApiResponse("incident is deleted successfully"));
    }

    @PutMapping("/start-monitoring-incident/{id}")
    public ResponseEntity<?> startMonitoringIncident(@PathVariable Integer id){
        incidentService.startMonitoringIncident(id);
        return ResponseEntity.status(200).body(new ApiResponse("incident is under monitoring successfully"));
    }

    @PutMapping("/resolve-incident/{id}")
    public ResponseEntity<?> resolveIncident(@PathVariable Integer id, @RequestParam(required = false) LocalDateTime resolvedAt){
        incidentService.resolveIncident(id, resolvedAt);
        return ResponseEntity.status(200).body(new ApiResponse("incident is resolved successfully"));
    }


    @GetMapping("/get-patient-incidents/{patientId}")
    public ResponseEntity<?> getPatientIncidents(@PathVariable Integer patientId){
        List<Incident> incidents = incidentService.getPatientIncidents(patientId);
        return ResponseEntity.status(200).body(incidents);
    }
}
