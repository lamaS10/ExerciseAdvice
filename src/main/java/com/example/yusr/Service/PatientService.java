package com.example.yusr.Service;


import com.example.yusr.Api.ApiException;
import com.example.yusr.Model.Caregiver;
import com.example.yusr.Model.Medication;
import com.example.yusr.Model.MedicationSchedule;
import com.example.yusr.Model.Patient;
import com.example.yusr.Repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final CaregiverRepository caregiverRepository;
    private final ChronicConditionRepository chronicConditionRepository;
    private final MedicationRepository medicationRepository;
    private final MedicationScheduleRepository medicationScheduleRepository;
    private final MedicationLogRepository medicationLogRepository;
    private final CareTaskRepository careTaskRepository;
    private final HealthReadingRepository healthReadingRepository;
    private final IncidentRepository incidentRepository;
    private final HandoverRepository handoverRepository;
    private final CaregiverInvitationRepository caregiverInvitationRepository;
    private final EmailService emailService;

    public List<Patient>getPatients(){
        return patientRepository.findAll();
    }

    public void addPatient(Patient patient){
        patient.setPatientCode(generatePatientCode());
        patient.setStatus("inactive");
        patientRepository.save(patient);
    }

    public Patient getPatientById(Integer id){
        Patient patient=patientRepository.findPatientById(id);
        if (patient == null){
            throw new ApiException("didn't find the requested patient");
        }
        return patient;
    }

    public void updatePatient(Integer id,Patient patient){
        Patient oldPatient=patientRepository.findPatientById(id);
        if (oldPatient==null){
            throw new ApiException("didn't find the requested patient");
        }

        oldPatient.setFullName(patient.getFullName());
        oldPatient.setAllergies(patient.getAllergies());
        oldPatient.setBloodType(patient.getBloodType());
        oldPatient.setDateOfBirth(patient.getDateOfBirth());
        oldPatient.setEmergencyContactName(patient.getEmergencyContactName());
        oldPatient.setEmergencyContactPhone(patient.getEmergencyContactPhone());
        oldPatient.setGender(patient.getGender());
        oldPatient.setMedicalNotes(patient.getMedicalNotes());
        patientRepository.save(oldPatient);
    }



    @Transactional
    public void deletePatient(Integer patientId, Integer caregiverId){

        Patient patient = patientRepository.findPatientById(patientId);

        if (patient == null){
            throw new ApiException("didn't find the requested patient");
        }

        Caregiver caregiver = caregiverRepository.findCaregiverById(caregiverId);

        if (caregiver == null){
            throw new ApiException("didn't find the requested caregiver");
        }

        if (caregiver.getPatientId() == null || !caregiver.getPatientId().equals(patientId)){
            throw new ApiException("caregiver does not belong to patient");
        }

        if (!caregiver.getRole().equals("primary")){
            throw new ApiException("only primary caregiver can delete patient");
        }

        Boolean hasOtherCaregiver = caregiverRepository.existsCaregiverByPatientIdAndIdNot(patientId, caregiverId);

        if (hasOtherCaregiver){
            throw new ApiException("unlink other caregivers before deleting patient");
        }

        // Delete medication logs, schedules, then medications
        List<Medication> medications = medicationRepository.findMedicationsByPatientId(patientId);

        for (Medication medication : medications){

            List<MedicationSchedule> schedules = medicationScheduleRepository.findMedicationSchedulesByMedicationId(medication.getId());

            for (MedicationSchedule schedule : schedules){
                medicationLogRepository.deleteAllByScheduleId(schedule.getId());
            }

            medicationScheduleRepository.deleteAllByMedicationId(medication.getId());
        }

        medicationRepository.deleteAllByPatientId(patientId);

        // Delete patient related records
        chronicConditionRepository.deleteAllByPatientId(patientId);
        careTaskRepository.deleteAllByPatientId(patientId);
        healthReadingRepository.deleteAllByPatientId(patientId);
        incidentRepository.deleteAllByPatientId(patientId);
        handoverRepository.deleteAllByPatientId(patientId);
        caregiverInvitationRepository.deleteAllByPatientId(patientId);

        // Unlink the primary caregiver
        caregiver.setPatientId(null);
        caregiver.setRole("unassigned");
        caregiver.setAssignedAt(null);
        caregiver.setIsCurrentCaregiver(false);

        caregiverRepository.save(caregiver);

        // Finally delete patient
        patientRepository.delete(patient);
    }


    @Transactional
    public void addPatientByCaregiver(Integer caregiverId, Patient patient){

        Caregiver caregiver = caregiverRepository.findCaregiverById(caregiverId);

        if (caregiver == null){
            throw new ApiException("didn't find the requested caregiver");
        }

        if (caregiver.getPatientId() != null){
            throw new ApiException("caregiver already has patient");
        }

        patient.setPatientCode(generatePatientCode());
        patient.setStatus("active");

        patientRepository.save(patient);

        caregiver.setPatientId(patient.getId());
        caregiver.setRole("primary");
        caregiver.setAssignedAt(LocalDateTime.now());
        caregiver.setIsCurrentCaregiver(true);

        caregiverRepository.save(caregiver);

        try {
            emailService.sendPatientCreatedEmail(caregiver.getEmail(), caregiver.getFullName(), patient.getFullName(), patient.getPatientCode());
        } catch (Exception e) {
            System.out.println("Failed to send patient created email: " + e.getMessage());
        }

    }


    @Transactional
    public void linkExistingPatient(Integer caregiverId, String patientCode) {

        Caregiver caregiver = caregiverRepository.findCaregiverById(caregiverId);

        if (caregiver == null) {
            throw new ApiException("caregiver not found");
        }

        if (caregiver.getPatientId() != null) {
            throw new ApiException("caregiver already has patient");
        }

        if (patientCode == null || patientCode.trim().isEmpty()) {
            throw new ApiException("patient code cannot be empty");
        }

        Patient patient = patientRepository.findPatientByPatientCode(patientCode.trim().toUpperCase());

        if (patient == null) {
            throw new ApiException("didn't find the requested patient");
        }

        if (patient.getStatus().equals("active")) {
            throw new ApiException("patient is already active");
        }

        patient.setStatus("active");

        caregiver.setPatientId(patient.getId());
        caregiver.setRole("primary");
        caregiver.setAssignedAt(LocalDateTime.now());
        caregiver.setIsCurrentCaregiver(true);

        patientRepository.save(patient);
        caregiverRepository.save(caregiver);
    }

    //helper method

    private String generatePatientCode(){

        String patientCode;

        do {
            patientCode = "YUSR-P-" + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();

        } while (patientRepository.findPatientByPatientCode(patientCode) != null);

        return patientCode;
    }
}
