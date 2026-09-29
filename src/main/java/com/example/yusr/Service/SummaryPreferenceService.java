package com.example.yusr.Service;

import com.example.yusr.Api.ApiException;
import com.example.yusr.Model.Caregiver;
import com.example.yusr.Model.SummaryPreference;
import com.example.yusr.Repository.CaregiverRepository;
import com.example.yusr.Repository.SummaryPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SummaryPreferenceService {

    private final SummaryPreferenceRepository summaryPreferenceRepository;
    private final CaregiverRepository caregiverRepository;


        public SummaryPreference getSummaryPreferenceByCaregiverId(Integer caregiverId) {
            SummaryPreference summaryPreference=summaryPreferenceRepository.findSummaryPreferenceByCaregiverId(caregiverId);
            if (summaryPreference == null) {
                throw new ApiException("summary preference not found");
            }
        return summaryPreference;
    }


    public void addSummaryPreference(SummaryPreference summaryPreference) {

        Caregiver caregiver = caregiverRepository.findCaregiverById(summaryPreference.getCaregiverId());

        if (caregiver == null) {
            throw new ApiException("caregiver not found");
        }

        if (caregiver.getPatientId() == null) {
            throw new ApiException("caregiver is not assigned to a patient");
        }

        SummaryPreference checkPreference = summaryPreferenceRepository.findSummaryPreferenceByCaregiverId(summaryPreference.getCaregiverId());

        if (checkPreference != null) {
            throw new ApiException("summary preference already exists, you can update it");
        }

        summaryPreference.setLastGeneratedAt(null);

        if (summaryPreference.getAutoGenerate()) {
            summaryPreference.setNextGenerationAt(calculateNextGeneration(summaryPreference.getFrequency()));
        } else {
            summaryPreference.setNextGenerationAt(null);
        }

        summaryPreferenceRepository.save(summaryPreference);

    }


    public void updateSummaryPreference(Integer id, SummaryPreference summaryPreference) {

        SummaryPreference oldPreference = summaryPreferenceRepository.findSummaryPreferenceById(id);

        if (oldPreference == null) {
            throw new ApiException("summary preference not found");
        }

        if (!oldPreference.getCaregiverId().equals(summaryPreference.getCaregiverId())) {
            throw new ApiException("caregiver id cannot be changed");
        }

        Caregiver caregiver = caregiverRepository.findCaregiverById(oldPreference.getCaregiverId());

        if (caregiver == null) {
            throw new ApiException("caregiver not found");
        }

        if (caregiver.getPatientId() == null) {
            throw new ApiException("caregiver is not assigned to a patient");
        }

        oldPreference.setFrequency(summaryPreference.getFrequency());
        oldPreference.setAutoGenerate(summaryPreference.getAutoGenerate());

        if (summaryPreference.getAutoGenerate()) {
            oldPreference.setNextGenerationAt(calculateNextGeneration(summaryPreference.getFrequency()));
        } else {
            oldPreference.setNextGenerationAt(null);
        }

        summaryPreferenceRepository.save(oldPreference);

    }


    public void deleteSummaryPreference(Integer id) {
        SummaryPreference summaryPreference = summaryPreferenceRepository.findSummaryPreferenceById(id);
        if (summaryPreference == null) {
            throw new ApiException("summary preference not found");
        }

        summaryPreferenceRepository.delete(summaryPreference);
    }



    public void updateAfterGeneration(Integer id) {

        SummaryPreference summaryPreference = summaryPreferenceRepository.findSummaryPreferenceById(id);

        if (summaryPreference == null) {
            return;
        }

        summaryPreference.setLastGeneratedAt(LocalDateTime.now());
        summaryPreference.setNextGenerationAt(calculateNextGeneration(summaryPreference.getFrequency()));

        summaryPreferenceRepository.save(summaryPreference);
    }

    public SummaryPreference changeAutoGenerate(Integer caregiverId) {

        SummaryPreference summaryPreference = summaryPreferenceRepository.findSummaryPreferenceByCaregiverId(caregiverId);

        if (summaryPreference == null) {
            throw new ApiException("summary preference not found");
        }

        if (summaryPreference.getAutoGenerate()) {
            summaryPreference.setAutoGenerate(false);
            summaryPreference.setNextGenerationAt(null);
            summaryPreferenceRepository.save(summaryPreference);
            return summaryPreference;
        }

        summaryPreference.setAutoGenerate(true);
        summaryPreference.setNextGenerationAt(calculateNextGeneration(summaryPreference.getFrequency()));

        summaryPreferenceRepository.save(summaryPreference);
        return summaryPreference;
    }

    //helper method
    private LocalDateTime calculateNextGeneration(String frequency) {

        LocalDateTime now = LocalDateTime.now();

        if (frequency.equals("daily")) {
            return now.plusDays(1);
        }

        if (frequency.equals("weekly")) {
            return now.plusWeeks(1);
        }

        return now.plusMonths(1);
    }

}