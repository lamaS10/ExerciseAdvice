package com.example.yusr.Service;


import com.example.yusr.Api.ApiException;
import com.example.yusr.Model.Medication;
import com.example.yusr.Model.Patient;
import com.example.yusr.Repository.MedicationRepository;
import com.example.yusr.Repository.MedicationScheduleRepository;
import com.example.yusr.Repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicationService {
    private final MedicationRepository medicationRepository;
    private final PatientRepository patientRepository;
    private final MedicationScheduleRepository medicationScheduleRepository;



    public List<Medication>getMedications(){
        return medicationRepository.findAll();
    }

    public void addMedication(Medication medication){

        Patient checkPatient = patientRepository.findPatientById(medication.getPatientId());
        if (checkPatient == null){
            throw new ApiException("didn't find the requested patient");
        }

        if (medication.getEndDate() != null && medication.getEndDate().isBefore(medication.getStartDate())){
            throw new ApiException("end date cannot be before start date");
        }
        medication.setStatus("active");
        medicationRepository.save(medication);

    }

    public void updateMedication(Integer id, Medication medication){

        Medication oldMedication =medicationRepository.findMedicationById(id);

        if (oldMedication == null){
            throw new ApiException("didn't find the requested medication");
        }

        if (!oldMedication.getPatientId().equals(medication.getPatientId())){
            throw new ApiException("patient id cannot be changed");
        }

        if (medication.getEndDate() != null && medication.getEndDate().isBefore(medication.getStartDate())){
            throw new ApiException("end date cannot be before start date");
        }

        oldMedication.setName(medication.getName());
        oldMedication.setDosageValue(medication.getDosageValue());
        oldMedication.setDosageUnit(medication.getDosageUnit());
        oldMedication.setStockQuantity(medication.getStockQuantity());
        oldMedication.setLowStockLimit(medication.getLowStockLimit());
        oldMedication.setStartDate(medication.getStartDate());
        oldMedication.setEndDate(medication.getEndDate());

        medicationRepository.save(oldMedication);
    }

    public void deleteMedication(Integer id){

        Medication medication = medicationRepository.findMedicationById(id);

        if (medication == null){
            throw new ApiException("didn't find the requested medication");
        }

        Boolean hasSchedules = medicationScheduleRepository.existsMedicationScheduleByMedicationId(id);

        if (hasSchedules){
            throw new ApiException("medication has schedules");
        }

        medicationRepository.delete(medication);
    }


    public void discontinueMedication(Integer medicationId){

        Medication medication = medicationRepository.findMedicationById(medicationId);

        if (medication == null){
            throw new ApiException("didn't find the requested medication");
        }

        if (medication.getStatus().equals("discontinued")){
            throw new ApiException("medication already discontinued");
        }
        medication.setStatus("discontinued");
        medication.setEndDate(LocalDate.now());
        medicationRepository.save(medication);
    }

    public List<Medication> getMedicationWarnings(Integer patientId){
        Patient patient = patientRepository.findPatientById(patientId);
        if (patient == null){
            throw new ApiException("didn't find the requested patient");
        }
        LocalDate today = LocalDate.now();
        LocalDate afterSevenDays = today.plusDays(7);
        return medicationRepository.getMedicationAlerts(patientId, today, afterSevenDays);
    }

    public List<Medication> getPatientMedications(Integer patientId){

        Patient patient = patientRepository.findPatientById(patientId);

        if (patient == null){
            throw new ApiException("didn't find the requested patient");
        }

        return medicationRepository.findMedicationsByPatientId(patientId);
    }

    public void addMedicationStock(Integer medicationId, Integer quantity){

        Medication medication = medicationRepository.findMedicationById(medicationId);

        if (medication == null){
            throw new ApiException("didn't find the requested medication");
        }

        if (quantity == null || quantity <= 0){
            throw new ApiException("quantity must be more than zero");
        }

        medication.setStockQuantity(medication.getStockQuantity() + quantity);

        medicationRepository.save(medication);

    }

    public void reactivateMedication(Integer medicationId, LocalDate startDate, LocalDate endDate){

        Medication medication = medicationRepository.findMedicationById(medicationId);

        if (medication == null){
            throw new ApiException("didn't find the requested medication");
        }

        if (medication.getStatus().equals("active")){
            throw new ApiException("medication already active");
        }

        if (startDate != null){
            if (endDate != null && endDate.isBefore(startDate)){
                throw new ApiException("end date cannot be before start date");
            }

            medication.setStartDate(startDate);
            medication.setEndDate(endDate);
        }

        medication.setStatus("active");
        medicationRepository.save(medication);
    }


}
