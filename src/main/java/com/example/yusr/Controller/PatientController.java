package com.example.yusr.Controller;


import com.example.yusr.Api.ApiResponse;
import com.example.yusr.Model.Patient;
import com.example.yusr.Service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping("/get-patients")
    public ResponseEntity<?>getPatients(){
        return ResponseEntity.status(200).body(patientService.getPatients());
    }

    @PostMapping("/add-patient")
    public ResponseEntity<?>addPatient(@RequestBody @Valid Patient patient){

        patientService.addPatient(patient);
        return ResponseEntity.status(200).body(new ApiResponse("the patient added successfully"));
    }

    @PutMapping("/update-patient/{id}")
    public ResponseEntity<?>updatePatient(@PathVariable Integer id,@RequestBody @Valid Patient patient){
        patientService.updatePatient(id,patient);
        return ResponseEntity.status(200).body(new ApiResponse("the patient with id: "+id+" is updated successfully"));

    }


    @DeleteMapping("/delete-patient/{patientId}/{caregiverId}")
    public ResponseEntity<?> deletePatient(@PathVariable Integer patientId, @PathVariable Integer caregiverId){
        patientService.deletePatient(patientId, caregiverId);
        return ResponseEntity.status(200).body(new ApiResponse("patient and all related data deleted successfully"));
    }


    @PostMapping("/add-patient-by-caregiver/{caregiverId}")
    public ResponseEntity<?> addPatientByCaregiver(@PathVariable Integer caregiverId, @RequestBody @Valid Patient patient){
        patientService.addPatientByCaregiver(caregiverId, patient);
        return ResponseEntity.status(200).body(new ApiResponse("the patient added successfully"));
    }

    @GetMapping("/get-patient/{id}")
    public ResponseEntity<?> getPatientById(@PathVariable Integer id){
        Patient patient = patientService.getPatientById(id);
        return ResponseEntity.status(200).body(patient);
    }

    @PostMapping("/link-existing-patient/{caregiverId}")
    public ResponseEntity<?> linkExistingPatient(@PathVariable Integer caregiverId, @RequestParam String patientCode){
        patientService.linkExistingPatient(caregiverId, patientCode);
        return ResponseEntity.status(200).body(new ApiResponse("patient linked successfully"));
    }


}
