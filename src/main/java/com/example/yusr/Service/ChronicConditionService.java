package com.example.yusr.Service;


import com.example.yusr.Api.ApiException;
import com.example.yusr.Model.ChronicCondition;
import com.example.yusr.Model.Patient;
import com.example.yusr.Repository.ChronicConditionRepository;
import com.example.yusr.Repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChronicConditionService {

    private final ChronicConditionRepository chronicConditionRepository;
    private final PatientRepository patientRepository;

    public List<ChronicCondition>getChronicConditions(){
        return chronicConditionRepository.findAll();
    }

    public void addChronicCondition(ChronicCondition chronicCondition){
        Patient checkPatient=patientRepository.findPatientById(chronicCondition.getPatientId());

        if (checkPatient==null){
            throw new ApiException("didn't find patient with requested id");
        }

        chronicConditionRepository.save(chronicCondition);
    }



    public void updateChronicCondition(Integer id, ChronicCondition chronicCondition){

        ChronicCondition oldChronicCondition =chronicConditionRepository.findChronicConditionById(id);

        if (oldChronicCondition == null){
            throw new ApiException("didn't find Chronic Condition with requested id");
        }

        if (!oldChronicCondition.getPatientId().equals(chronicCondition.getPatientId())){
            throw new ApiException("patient id cannot be changed");
        }
        oldChronicCondition.setConditionName(chronicCondition.getConditionName());
        oldChronicCondition.setDiagnosisDate(chronicCondition.getDiagnosisDate());
        oldChronicCondition.setNotes(chronicCondition.getNotes());

        chronicConditionRepository.save(oldChronicCondition);
    }

    public void deleteChronicCondition(Integer id){

        ChronicCondition chronicCondition = chronicConditionRepository.findChronicConditionById(id);

        if (chronicCondition == null){
            throw new ApiException("didn't find Chronic Condition with requested id");
        }

        chronicConditionRepository.delete(chronicCondition);
    }

}
