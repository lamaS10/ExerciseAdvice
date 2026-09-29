package com.example.yusr.Service;

import com.example.yusr.Api.ApiException;
import com.example.yusr.Model.*;
import com.example.yusr.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CareSummaryService {

    private final CareSummaryRepository careSummaryRepository;
    private final CaregiverRepository caregiverRepository;
    private final PatientRepository patientRepository;
    private final ChronicConditionRepository chronicConditionRepository;
    private final MedicationRepository medicationRepository;
    private final MedicationLogRepository medicationLogRepository;
    private final HealthReadingRepository healthReadingRepository;
    private final CareTaskRepository careTaskRepository;
    private final IncidentRepository incidentRepository;
    private final AIService aiService;
    private final EmailService emailService;


    public CareSummary getLatestCareSummary(Integer caregiverId) {

        Caregiver caregiver = caregiverRepository.findCaregiverById(caregiverId);

        if (caregiver == null || caregiver.getPatientId() == null) {
           throw new ApiException("didn't find the caregiver or patient");
        }
        CareSummary careSummary=careSummaryRepository.findCareSummaryByPatientId(caregiver.getPatientId());
        if (careSummary == null) {
            throw new ApiException("didn't find care summary");

        }

        return careSummary;
    }


    public void generateOnDemandSummary(Integer caregiverId, LocalDate fromDate, LocalDate toDate) {

        Caregiver caregiver = caregiverRepository.findCaregiverById(caregiverId);

        if (caregiver == null) {
            throw new ApiException("didn't find the caregiver");
        }

        if (caregiver.getPatientId() == null) {
            throw new ApiException("caregiver is not assigned to a patient");
        }

        if (fromDate == null || toDate == null) {
            throw new ApiException("from date and to date cannot be null");
        }

        if (fromDate.isAfter(toDate)) {
            throw new ApiException("from date cannot be after to date");
        }

        if (toDate.isAfter(LocalDate.now())) {
            throw new ApiException("to date cannot be in the future");
        }

        Patient patient = patientRepository.findPatientById(caregiver.getPatientId());

        if (patient == null) {
            throw new ApiException("didn't find the patient");
        }

        LocalDateTime startDateTime = fromDate.atStartOfDay();
        LocalDateTime endDateTime = toDate.plusDays(1).atStartOfDay();


        List<ChronicCondition> chronicConditions = chronicConditionRepository.findChronicConditionsByPatientId(patient.getId());

        List<Medication> medications = medicationRepository.findMedicationsByPatientId(patient.getId());

        List<MedicationLog> medicationLogs = medicationLogRepository.getMedicationLogsByPatientAndPeriod(patient.getId(), startDateTime, endDateTime);

        List<HealthReading> healthReadings = healthReadingRepository.findHealthReadingsByPatientIdAndMeasuredAtBetween(patient.getId(), startDateTime, endDateTime);

        List<CareTask> careTasks = careTaskRepository.findCareTasksByPatientIdAndDueDateTimeBetween(patient.getId(), startDateTime, endDateTime);

        List<Incident> incidents = incidentRepository.findIncidentsByPatientIdAndIncidentAtBetween(patient.getId(), startDateTime, endDateTime);


        String patientData = buildPatientData(patient, fromDate, toDate, chronicConditions, medications, medicationLogs, healthReadings, careTasks, incidents);

        String generatedSummary;

        try {
            generatedSummary = aiService.generateSummary(patientData);
        } catch (Exception e) {
            throw new ApiException("AI service is temporarily unavailable");
        }

        saveLatestSummary(patient.getId(), generatedSummary, fromDate, toDate, "on_demand");
        try {
            emailService.sendSummaryEmail(
                    caregiver.getEmail(),
                    patient.getFullName(),
                    generatedSummary
            );
        } catch (Exception e) {
            System.out.println("Failed to send summary email: " + e.getMessage());
        }
    }



    public void generateAutomaticSummary(Integer caregiverId, String frequency) {

        Caregiver caregiver = caregiverRepository.findCaregiverById(caregiverId);

        if (caregiver == null) {
            throw new ApiException("didn't find caregiver");
        }

        if (caregiver.getPatientId() == null) {
            throw new ApiException("caregiver is not assigned to a patient");
        }

        Patient patient = patientRepository.findPatientById(caregiver.getPatientId());

        if (patient == null) {
            throw new ApiException("patient not found");
        }


        LocalDate toDate = LocalDate.now();
        LocalDate fromDate;

        if (frequency.equals("daily")) {
            fromDate = toDate;
        } else if (frequency.equals("weekly")) {
            fromDate = toDate.minusDays(6);
        } else {
            fromDate = toDate.minusMonths(1).plusDays(1);
        }


        LocalDateTime startDateTime = fromDate.atStartOfDay();
        LocalDateTime endDateTime = toDate.plusDays(1).atStartOfDay();


        List<ChronicCondition> chronicConditions = chronicConditionRepository.findChronicConditionsByPatientId(patient.getId());

        List<Medication> medications = medicationRepository.findMedicationsByPatientId(patient.getId());

        List<MedicationLog> medicationLogs = medicationLogRepository.getMedicationLogsByPatientAndPeriod(patient.getId(), startDateTime, endDateTime);

        List<HealthReading> healthReadings = healthReadingRepository.findHealthReadingsByPatientIdAndMeasuredAtBetween(patient.getId(), startDateTime, endDateTime);

        List<CareTask> careTasks = careTaskRepository.findCareTasksByPatientIdAndDueDateTimeBetween(patient.getId(), startDateTime, endDateTime);

        List<Incident> incidents = incidentRepository.findIncidentsByPatientIdAndIncidentAtBetween(patient.getId(), startDateTime, endDateTime);


        String patientData = buildPatientData(patient, fromDate, toDate, chronicConditions, medications, medicationLogs, healthReadings, careTasks, incidents);



        String generatedSummary;

        try {
            generatedSummary = aiService.generateSummary(patientData);
        } catch (Exception e) {
            throw new ApiException("AI service is temporarily unavailable");
        }

        saveLatestSummary(patient.getId(), generatedSummary, fromDate, toDate, "automatic");
        try {
            emailService.sendSummaryEmail(
                    caregiver.getEmail(),
                    patient.getFullName(),
                    generatedSummary
            );
        } catch (Exception e) {
            System.out.println("Failed to send summary email: " + e.getMessage());
        }

    }

    public void sendLatestSummaryEmail(Integer caregiverId) {

        Caregiver caregiver = caregiverRepository.findCaregiverById(caregiverId);

        if (caregiver == null) {
            throw new ApiException("didn't find caregiver");
        }

        if (caregiver.getPatientId() == null) {
            throw new ApiException("caregiver is not assigned to a patient");
        }

        Patient patient = patientRepository.findPatientById(caregiver.getPatientId());

        if (patient == null) {
            throw new ApiException("didn't find patient");
        }

        CareSummary careSummary = careSummaryRepository.findCareSummaryByPatientId(patient.getId());

        if (careSummary == null) {
            throw new ApiException("didn't find care summary");
        }

        try {
            emailService.sendSummaryEmail(
                    caregiver.getEmail(),
                    patient.getFullName(),
                    careSummary.getSummary()
            );
        } catch (Exception e) {
            throw new ApiException("email could not be sent");
        }
    }


    //helper method
    private void saveLatestSummary(Integer patientId, String generatedSummary, LocalDate fromDate, LocalDate toDate, String generationType) {

        CareSummary careSummary = careSummaryRepository.findCareSummaryByPatientId(patientId);

        if (careSummary == null) {
            careSummary = new CareSummary();
            careSummary.setPatientId(patientId);
        }

        careSummary.setSummary(generatedSummary);
        careSummary.setFromDate(fromDate);
        careSummary.setToDate(toDate);
        careSummary.setGeneratedAt(LocalDateTime.now());
        careSummary.setGenerationType(generationType);

        careSummaryRepository.save(careSummary);
    }



    private String buildPatientData(Patient patient, LocalDate fromDate, LocalDate toDate,
                                    List<ChronicCondition> chronicConditions,
                                    List<Medication> medications,
                                    List<MedicationLog> medicationLogs,
                                    List<HealthReading> healthReadings,
                                    List<CareTask> careTasks,
                                    List<Incident> incidents) {

        StringBuilder data = new StringBuilder();

        data.append("Summary Period: ")
                .append(fromDate)
                .append(" to ")
                .append(toDate)
                .append("\n\n");


        data.append("Patient Information:\n");
        data.append("Name: ").append(patient.getFullName()).append("\n");
        data.append("Date of Birth: ").append(patient.getDateOfBirth()).append("\n");
        data.append("Gender: ").append(patient.getGender()).append("\n");
        data.append("Blood Type: ").append(patient.getBloodType()).append("\n");
        data.append("Allergies: ").append(patient.getAllergies()).append("\n");
        data.append("Medical Notes: ").append(patient.getMedicalNotes()).append("\n\n");


        data.append("Chronic Conditions:\n");

        if (chronicConditions.isEmpty()) {
            data.append("No chronic conditions recorded.\n");
        } else {
            for (ChronicCondition condition : chronicConditions) {
                data.append("- ")
                        .append(condition.getConditionName())
                        .append(", Diagnosis Date: ")
                        .append(condition.getDiagnosisDate())
                        .append(", Notes: ")
                        .append(condition.getNotes())
                        .append("\n");
            }
        }


        data.append("\nMedications:\n");

        if (medications.isEmpty()) {
            data.append("No medications recorded.\n");
        } else {
            for (Medication medication : medications) {
                data.append("- ")
                        .append(medication.getName())
                        .append(", Dosage: ")
                        .append(medication.getDosageValue())
                        .append(" ")
                        .append(medication.getDosageUnit())
                        .append(", Status: ")
                        .append(medication.getStatus())
                        .append(", Start Date: ")
                        .append(medication.getStartDate())
                        .append(", End Date: ")
                        .append(medication.getEndDate())
                        .append("\n");
            }
        }


        data.append("\nMedication Logs During Period:\n");

        if (medicationLogs.isEmpty()) {
            data.append("No medication logs recorded during this period.\n");
        } else {
            for (MedicationLog log : medicationLogs) {
                data.append("- Scheduled: ")
                        .append(log.getScheduledDateTime())
                        .append(", Status: ")
                        .append(log.getStatus())
                        .append(", Taken At: ")
                        .append(log.getTakenAt())
                        .append("\n");
            }
        }


        data.append("\nHealth Readings During Period:\n");

        if (healthReadings.isEmpty()) {
            data.append("No health readings recorded during this period.\n");
        } else {
            for (HealthReading reading : healthReadings) {

                data.append("- Type: ")
                        .append(reading.getReadingType());

                if (reading.getReadingType().equals("blood_pressure")) {
                    data.append(", Systolic: ")
                            .append(reading.getSystolicValue())
                            .append(", Diastolic: ")
                            .append(reading.getDiastolicValue());
                } else {
                    data.append(", Value: ")
                            .append(reading.getReadingValue());
                }

                data.append(" ")
                        .append(reading.getUnit())
                        .append(", Measured At: ")
                        .append(reading.getMeasuredAt())
                        .append(", Notes: ")
                        .append(reading.getNotes())
                        .append("\n");
            }
        }


        data.append("\nCare Tasks During Period:\n");

        if (careTasks.isEmpty()) {
            data.append("No care tasks recorded during this period.\n");
        } else {
            for (CareTask task : careTasks) {
                data.append("- ")
                        .append(task.getTitle())
                        .append(", Priority: ")
                        .append(task.getPriority())
                        .append(", Status: ")
                        .append(task.getStatus())
                        .append(", Due: ")
                        .append(task.getDueDateTime())
                        .append(", Completed At: ")
                        .append(task.getCompletedAt())
                        .append("\n");
            }
        }


        data.append("\nIncidents During Period:\n");

        if (incidents.isEmpty()) {
            data.append("No incidents recorded during this period.\n");
        } else {
            for (Incident incident : incidents) {
                data.append("- Type: ")
                        .append(incident.getIncidentType())
                        .append(", Severity: ")
                        .append(incident.getSeverity())
                        .append(", Description: ")
                        .append(incident.getDescription())
                        .append(", Action Taken: ")
                        .append(incident.getActionTaken())
                        .append(", Status: ")
                        .append(incident.getStatus())
                        .append(", Incident At: ")
                        .append(incident.getIncidentAt())
                        .append("\n");
            }
        }

        return data.toString();
    }
}