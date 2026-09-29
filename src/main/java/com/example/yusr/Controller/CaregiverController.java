package com.example.yusr.Controller;


import com.example.yusr.Api.ApiResponse;
import com.example.yusr.Model.Caregiver;
import com.example.yusr.Model.CaregiverInvitation;
import com.example.yusr.Service.CaregiverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/caregiver")
@RequiredArgsConstructor
public class CaregiverController {

    private final CaregiverService caregiverService;

    @GetMapping("/get-caregivers")
    public ResponseEntity<?>getCaregiver(){
        return ResponseEntity.status(200).body(caregiverService.getCaregivers());
    }

    @PostMapping("/add-caregiver")
    public ResponseEntity<?>addCaregiver(@RequestBody @Valid Caregiver caregiver){
        caregiverService.addCaregiver(caregiver);
        return ResponseEntity.status(200).body(new ApiResponse("the caregiver is added successfully"));
    }


    @PutMapping("/update-caregiver/{id}")
    public ResponseEntity<?>updateCaregiver(@PathVariable Integer id,@RequestBody @Valid Caregiver caregiver){
        caregiverService.updateCaregiver(id, caregiver);
        return ResponseEntity.status(200).body(new ApiResponse("the caregiver with id: " + id + " is updated successfully"));
    }


    @DeleteMapping("/delete-caregiver/{id}")
    public ResponseEntity<?> deleteCaregiver(@PathVariable Integer id){
        caregiverService.deleteCaregiver(id);
        return ResponseEntity.status(200).body(new ApiResponse("the caregiver with id: " + id + " is deleted successfully"));
    }

    // Sends an invitation to add a caregiver to the patient
    @PostMapping("/assign-caregiver-to-patient/{primaryCaregiverId}/{patientId}")
    public ResponseEntity<?> assignCaregiverToPatient(@PathVariable Integer primaryCaregiverId, @PathVariable Integer patientId, @RequestParam String email, @RequestParam String phoneNumber) {
        caregiverService.assignCaregiverToPatient(primaryCaregiverId, patientId, email, phoneNumber);
        return ResponseEntity.status(200).body(new ApiResponse("caregiver invitation sent successfully"));
    }


    // Checks the caregiver's latest WhatsApp response
    @PostMapping("/check-caregiver-invitation-response")
    public ResponseEntity<?> checkCaregiverInvitationResponse(@RequestParam String phoneNumber) {
        Object result = caregiverService.checkCaregiverInvitationResponse(phoneNumber);
        return ResponseEntity.status(200).body(new ApiResponse(result.toString()));
    }

    // Unlinks a secondary or backup caregiver from the patient
    @PutMapping("/unlink-caregiver/{primaryCaregiverId}/{caregiverId}")
    public ResponseEntity<?> unlinkCaregiverFromPatient(@PathVariable Integer primaryCaregiverId, @PathVariable Integer caregiverId) {
        caregiverService.unlinkCaregiverFromPatient(primaryCaregiverId, caregiverId);
        return ResponseEntity.status(200).body(new ApiResponse("caregiver unlinked successfully"));
    }

    @GetMapping("/get-patient-caregivers/{patientId}")
    public ResponseEntity<?> getPatientCaregivers(@PathVariable Integer patientId){

        List<Caregiver> caregivers = caregiverService.getPatientCaregivers(patientId);
        return ResponseEntity.status(200).body(caregivers);
    }

    @PutMapping("/transfer-primary-role/{primaryCaregiverId}/{newPrimaryCaregiverId}")
    public ResponseEntity<?> transferPrimaryRole(@PathVariable Integer primaryCaregiverId, @PathVariable Integer newPrimaryCaregiverId){

        caregiverService.transferPrimaryRole(primaryCaregiverId, newPrimaryCaregiverId);

        return ResponseEntity.status(200).body(new ApiResponse("transferred role successfully"));
    }

    @PutMapping("/leave-patient/{caregiverId}")
    public ResponseEntity<?> leavePatient(@PathVariable Integer caregiverId, @RequestParam(required = false) Integer newPrimaryCaregiverId){

        Object result = caregiverService.leavePatient(caregiverId, newPrimaryCaregiverId);
        return ResponseEntity.status(200).body(new ApiResponse(result.toString()));
    }

    @GetMapping("/get-pending-caregiver-invitation/{caregiverId}")
    public ResponseEntity<?> getPendingCaregiverInvitation(@PathVariable Integer caregiverId){
        CaregiverInvitation invitation = caregiverService.getPendingCaregiverInvitation(caregiverId);
        return ResponseEntity.status(200).body(invitation);
    }


    @PutMapping("/accept-caregiver-invitation/{invitationId}/{caregiverId}")
    public ResponseEntity<?> acceptCaregiverInvitation(@PathVariable Integer invitationId, @PathVariable Integer caregiverId){
        caregiverService.acceptCaregiverInvitation(invitationId, caregiverId);
        return ResponseEntity.status(200).body(new ApiResponse("invitation accepted and caregiver assigned successfully"));
    }


    @PutMapping("/reject-caregiver-invitation/{invitationId}/{caregiverId}")
    public ResponseEntity<?> rejectCaregiverInvitation(@PathVariable Integer invitationId, @PathVariable Integer caregiverId){
        caregiverService.rejectCaregiverInvitation(invitationId, caregiverId);
        return ResponseEntity.status(200).body(new ApiResponse("invitation rejected successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password){
        Caregiver caregiver = caregiverService.login(email, password);
        return ResponseEntity.status(200).body(caregiver);
    }

    @GetMapping("/get-caregiver/{id}")
    public ResponseEntity<?> getCaregiverById(@PathVariable Integer id){

        Caregiver caregiver = caregiverService.getCaregiverById(id);
        return ResponseEntity.status(200).body(caregiver);
    }

    @GetMapping("/get-pending-patient-invitations/{patientId}")
    public ResponseEntity<?> getPendingPatientInvitations(@PathVariable Integer patientId) {
        List<CaregiverInvitation> invitations = caregiverService.getPendingPatientInvitations(patientId);
        return ResponseEntity.status(200).body(invitations);
    }

    @PutMapping("/cancel-caregiver-invitation/{invitationId}/{primaryCaregiverId}")
    public ResponseEntity<?> cancelCaregiverInvitation(@PathVariable Integer invitationId, @PathVariable Integer primaryCaregiverId) {
        caregiverService.cancelCaregiverInvitation(invitationId, primaryCaregiverId);
        return ResponseEntity.status(200).body(new ApiResponse("invitation cancelled successfully"));
    }
}
