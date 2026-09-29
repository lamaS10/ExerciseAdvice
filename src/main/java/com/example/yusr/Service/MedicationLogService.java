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
public class MedicationLogService {
    private final MedicationLogRepository medicationLogRepository;
    private final MedicationScheduleRepository medicationScheduleRepository;
    private final MedicationRepository medicationRepository;
    private final PatientRepository patientRepository;
    private final CaregiverRepository caregiverRepository;
    private final WhatsAppService whatsAppService;

    public List<MedicationLog>getMedicationLogs(){
        return medicationLogRepository.findAll();
    }

    public void addMedicationLog(MedicationLog medicationLog){

        MedicationSchedule medicationSchedule = medicationScheduleRepository.findMedicationScheduleById(medicationLog.getScheduleId());

        if (medicationSchedule == null){
            throw new ApiException("didn't find medication schedule");
        }

        Boolean checkLog = medicationLogRepository.existsMedicationLogByScheduleIdAndScheduledDateTime(medicationLog.getScheduleId(), medicationLog.getScheduledDateTime());

        if (checkLog){
            throw new ApiException("medication log already exists");
        }

        Medication medication = medicationRepository.findMedicationById(medicationSchedule.getMedicationId());

        if (medication == null){
            throw new ApiException("didn't find medication");
        }

        medicationLog.setRecordedAt(LocalDateTime.now());

        if (medicationLog.getStatus().equals("missed")){
            medicationLog.setTakenAt(null);
        }

        if (medicationLog.getStatus().equals("taken") ||medicationLog.getStatus().equals("late")){
            if (medication.getStockQuantity() <= 0){
                throw new ApiException("medication is out of stock");
            }

            if (medicationLog.getTakenAt() == null){
                medicationLog.setTakenAt(LocalDateTime.now());
            }

            medication.setStockQuantity(medication.getStockQuantity() - 1);
            medicationRepository.save(medication);
            checkLowStock(medication);
        }

        medicationLogRepository.save(medicationLog);
    }


    public void updateMedicationLog(Integer id, MedicationLog medicationLog){
        Boolean stockDecreased = false;

        MedicationLog oldMedicationLog = medicationLogRepository.findMedicationLogById(id);

        if (oldMedicationLog == null){
            throw new ApiException("didn't find medication log");
        }

        if (!oldMedicationLog.getScheduleId().equals(medicationLog.getScheduleId())){
            throw new ApiException("schedule id cannot be changed");
        }

        if (!oldMedicationLog.getScheduledDateTime().equals(medicationLog.getScheduledDateTime())){
            throw new ApiException("scheduled date time cannot be changed");
        }

        MedicationSchedule medicationSchedule = medicationScheduleRepository.findMedicationScheduleById(oldMedicationLog.getScheduleId());

        Medication medication = medicationRepository.findMedicationById(medicationSchedule.getMedicationId());

        if (oldMedicationLog.getStatus().equals("missed") && !medicationLog.getStatus().equals("missed")){

            if (medication.getStockQuantity() <= 0){
                throw new ApiException("medication is out of stock");
            }

            medication.setStockQuantity(medication.getStockQuantity() - 1);
            if (medicationLog.getTakenAt() == null){
                oldMedicationLog.setTakenAt(LocalDateTime.now());
            }else{
                oldMedicationLog.setTakenAt(medicationLog.getTakenAt());
            }
            stockDecreased = true;
        }

        if (!oldMedicationLog.getStatus().equals("missed") && medicationLog.getStatus().equals("missed")){
            medication.setStockQuantity(medication.getStockQuantity() + 1);
            oldMedicationLog.setTakenAt(null);
        }

        oldMedicationLog.setStatus(medicationLog.getStatus());

        medicationRepository.save(medication);
        medicationLogRepository.save(oldMedicationLog);
        if (stockDecreased){
            checkLowStock(medication);
        }
    }

    public void deleteMedicationLog(Integer id){

        MedicationLog medicationLog =medicationLogRepository.findMedicationLogById(id);

        if (medicationLog == null){
            throw new ApiException("didn't find the medication log");
        }

        if (!medicationLog.getStatus().equals("missed")){
            MedicationSchedule medicationSchedule = medicationScheduleRepository.findMedicationScheduleById(medicationLog.getScheduleId());
            Medication medication = medicationRepository.findMedicationById(medicationSchedule.getMedicationId());
            medication.setStockQuantity(medication.getStockQuantity() + 1);
            medicationRepository.save(medication);
        }
        medicationLogRepository.delete(medicationLog);

    }


    public String getTodayMedicationSummary(Integer patientId){

        Patient patient = patientRepository.findPatientById(patientId);

        if (patient == null){
            throw new ApiException("didn't find the patient");
        }

        LocalDate today = LocalDate.now();

        String dayOfWeek = today.getDayOfWeek().toString().toLowerCase();

        List<MedicationSchedule> schedules = medicationScheduleRepository.getTodayMedicationSchedule(patientId, dayOfWeek);

        Integer totalDoses = schedules.size();
        Integer completedDoses = 0;
        Integer recordedDoses = 0;

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime startOfNextDay = today.plusDays(1).atStartOfDay();

        for (MedicationSchedule schedule : schedules){

            Integer completed = medicationLogRepository.countCompletedDoseForToday(schedule.getId(), startOfDay, startOfNextDay);

            Integer recorded = medicationLogRepository.countRecordedDoseForToday(schedule.getId(), startOfDay, startOfNextDay);

            completedDoses += completed;
            recordedDoses += recorded;
        }

        Integer missedDoses = recordedDoses - completedDoses;
        Integer remainingDoses = totalDoses - recordedDoses;

        return "total doses: " + totalDoses + ", completed doses: " + completedDoses + ", missed doses: " + missedDoses + ", remaining doses: " + remainingDoses;
    }


    public List<MedicationLog> getTodayMedicationLogs(Integer patientId){

        Patient patient = patientRepository.findPatientById(patientId);

        if (patient == null){
            throw new ApiException("didn't find the patient");
        }

        LocalDate today = LocalDate.now();

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime startOfNextDay = today.plusDays(1).atStartOfDay();

        return medicationLogRepository.getTodayMedicationLogs(patientId, startOfDay, startOfNextDay);
    }

    //helper method
    private void checkLowStock(Medication medication){

        if (medication.getStockQuantity() <= medication.getLowStockLimit()){

            Patient patient = patientRepository.findPatientById(medication.getPatientId());

            List<Caregiver> caregivers = caregiverRepository.findCaregiversByPatientId(medication.getPatientId());

            for (Caregiver caregiver : caregivers){

                String internationalPhone = "+966" + caregiver.getPhoneNumber().substring(1);

                whatsAppService.sendMessage(
                        internationalPhone,
                        "تنبيه من يسر 💊\n\n" +
                                "مخزون دواء " + medication.getName() +
                                " للمريض " + patient.getFullName() +
                                " أصبح منخفضًا.\n\n" +
                                "الكمية المتبقية: " +
                                medication.getStockQuantity() + "\n\n" +
                                "يفضل إعادة توفير الدواء لضمان استمرار جدول الجرعات."
                );
            }
        }
    }
}
