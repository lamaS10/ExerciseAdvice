package com.example.yusr.Service;


import com.example.yusr.Api.ApiException;
import com.example.yusr.Model.Medication;
import com.example.yusr.Model.MedicationSchedule;
import com.example.yusr.Model.Patient;
import com.example.yusr.Repository.MedicationLogRepository;
import com.example.yusr.Repository.MedicationRepository;
import com.example.yusr.Repository.MedicationScheduleRepository;
import com.example.yusr.Repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicationScheduleService {
    private final MedicationScheduleRepository medicationScheduleRepository;
    private final MedicationRepository medicationRepository;
    private final MedicationLogRepository medicationLogRepository;
    private final PatientRepository patientRepository;

    public List<MedicationSchedule>getMedicationSchedules(){
        return medicationScheduleRepository.findAll();
    }

    public void addMedicationSchedule(MedicationSchedule medicationSchedule){

        Medication medication =medicationRepository.findMedicationById(medicationSchedule.getMedicationId());

        if (medication == null){
            throw new ApiException("didn't find the requested medication");
        }

        if (medicationSchedule.getScheduleType().equals("daily") && medicationSchedule.getDayOfWeek() != null){
            throw new ApiException("day of week must be empty for daily schedule");
        }

        if (medicationSchedule.getScheduleType().equals("weekly") && medicationSchedule.getDayOfWeek() == null){
            throw new ApiException("day of week is required for weekly schedule");
        }

        medicationScheduleRepository.save(medicationSchedule);
    }

    public void updateMedicationSchedule(Integer id, MedicationSchedule medicationSchedule){

        MedicationSchedule oldMedicationSchedule = medicationScheduleRepository.findMedicationScheduleById(id);

        if (oldMedicationSchedule == null){
            throw new ApiException("didn't find medication schedule");
        }

        if (!oldMedicationSchedule.getMedicationId().equals(medicationSchedule.getMedicationId())){
            throw new ApiException("medication id cannot be changed");
        }

        if (medicationSchedule.getScheduleType().equals("daily") && medicationSchedule.getDayOfWeek() != null){
            throw new ApiException("day of week must be empty for daily schedule");
        }

        if (medicationSchedule.getScheduleType().equals("weekly") && medicationSchedule.getDayOfWeek() == null){
            throw new ApiException("day of week is required for weekly schedule");
        }

        oldMedicationSchedule.setScheduleType(medicationSchedule.getScheduleType());
        oldMedicationSchedule.setDayOfWeek(medicationSchedule.getDayOfWeek());
        oldMedicationSchedule.setScheduledTime(medicationSchedule.getScheduledTime());

        medicationScheduleRepository.save(oldMedicationSchedule);
    }

    public void deleteMedicationSchedule(Integer id){

        MedicationSchedule medicationSchedule = medicationScheduleRepository.findMedicationScheduleById(id);

        if (medicationSchedule == null){
            throw new ApiException("didn't find medication schedule");
        }

        Boolean hasLogs = medicationLogRepository.existsMedicationLogByScheduleId(id);

        if (hasLogs){
            throw new ApiException("medication schedule has logs");
        }

        medicationScheduleRepository.delete(medicationSchedule);

    }


    public List<MedicationSchedule> getTodayMedicationSchedule(Integer patientId){
        Patient patient = patientRepository.findPatientById(patientId);
        if (patient == null){
            throw new ApiException("didn't find the requested patient");
        }
        String dayOfWeek = LocalDate.now().getDayOfWeek().toString().toLowerCase();
        return medicationScheduleRepository.getTodayMedicationSchedule(patientId, dayOfWeek);
    }


}
