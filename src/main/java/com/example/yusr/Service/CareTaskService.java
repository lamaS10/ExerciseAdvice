package com.example.yusr.Service;


import com.example.yusr.Api.ApiException;
import com.example.yusr.Model.CareTask;
import com.example.yusr.Model.Caregiver;
import com.example.yusr.Model.Patient;
import com.example.yusr.Repository.CareTaskRepository;
import com.example.yusr.Repository.CaregiverRepository;
import com.example.yusr.Repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CareTaskService {
    private final CareTaskRepository careTaskRepository;
    private final PatientRepository patientRepository;
    private final CaregiverRepository caregiverRepository;


    public List<CareTask> getCareTasks(){
        return careTaskRepository.findAll();
    }

    public void addCareTask(CareTask careTask){

        Patient patient = patientRepository.findPatientById(careTask.getPatientId());

        if (patient == null){
            throw new ApiException("didn't find the patient");
        }

        Caregiver caregiver = caregiverRepository.findCaregiverById(careTask.getCaregiverId());

        if (caregiver == null){
            throw new ApiException("didn't find the caregiver");
        }

        if (caregiver.getPatientId() == null ||!caregiver.getPatientId().equals(careTask.getPatientId())){
            throw new ApiException("caregiver does not belong to patient");
        }

        careTask.setStatus("pending");
        careTask.setCompletedAt(null);

        careTaskRepository.save(careTask);
    }

    public void updateCareTask(Integer id, CareTask careTask){

        CareTask oldCareTask = careTaskRepository.findCareTaskById(id);

        if (oldCareTask == null){
            throw new ApiException("didn't find the care task");
        }

        if (!oldCareTask.getPatientId().equals(careTask.getPatientId())){
            throw new ApiException("patient id cannot be changed");
        }

        if (!oldCareTask.getCaregiverId().equals(careTask.getCaregiverId())){
            throw new ApiException("caregiver id cannot be changed");
        }

        oldCareTask.setTitle(careTask.getTitle());
        oldCareTask.setDescription(careTask.getDescription());
        oldCareTask.setDueDateTime(careTask.getDueDateTime());
        oldCareTask.setPriority(careTask.getPriority());

        careTaskRepository.save(oldCareTask);
    }

    public void deleteCareTask(Integer id){

        CareTask careTask = careTaskRepository.findCareTaskById(id);

        if (careTask == null){
            throw new ApiException("didn't find the care task");
        }

        careTaskRepository.delete(careTask);
    }

    public void completeCareTask(Integer id, LocalDateTime completedAt){

        CareTask careTask = careTaskRepository.findCareTaskById(id);

        if (careTask == null){
            throw new ApiException("didn't find the care task");
        }

        if (careTask.getStatus().equals("completed")){
            throw new ApiException("care task already completed");
        }

        if (careTask.getStatus().equals("cancelled")){
            throw new ApiException("cancelled care task cannot be completed");
        }

        if (completedAt == null){
            completedAt = LocalDateTime.now();
        }

        if (completedAt.isAfter(LocalDateTime.now())){
            throw new ApiException("completed time cannot be in the future");
        }
        if (careTask.getDueDateTime().isAfter(LocalDateTime.now())){
            throw new ApiException("care task cannot be completed before due date");
        }

        careTask.setStatus("completed");
        careTask.setCompletedAt(completedAt);

        careTaskRepository.save(careTask);
    }

    public void cancelCareTask(Integer id){

        CareTask careTask = careTaskRepository.findCareTaskById(id);

        if (careTask == null){
            throw new ApiException("didn't find the care task");
        }

        if (careTask.getStatus().equals("cancelled")){
            throw new ApiException("care task already cancelled");
        }

        if (careTask.getStatus().equals("completed")){
            throw new ApiException("completed care task cannot be cancelled");
        }
        careTask.setStatus("cancelled");
        careTask.setCompletedAt(null);
        careTaskRepository.save(careTask);
    }

    public List<CareTask> getPatientCareTasks(Integer patientId){

        Patient patient = patientRepository.findPatientById(patientId);

        if (patient == null){
            throw new ApiException("didn't find the patient");
        }

        return careTaskRepository.findCareTasksByPatientId(patientId);
    }
}
