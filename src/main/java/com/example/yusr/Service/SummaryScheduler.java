package com.example.yusr.Service;

import com.example.yusr.Model.SummaryPreference;
import com.example.yusr.Repository.SummaryPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SummaryScheduler {

    private final SummaryPreferenceRepository summaryPreferenceRepository;
    private final SummaryPreferenceService summaryPreferenceService;
    private final CareSummaryService careSummaryService;


    @Scheduled(fixedRate = 60000)
    public void generateScheduledSummaries() {

        List<SummaryPreference> preferences = summaryPreferenceRepository.findSummaryPreferencesByAutoGenerateTrueAndNextGenerationAtLessThanEqual(LocalDateTime.now());

        for (SummaryPreference preference : preferences) {
            careSummaryService.generateAutomaticSummary(preference.getCaregiverId(), preference.getFrequency());
            summaryPreferenceService.updateAfterGeneration(preference.getId());

        }
    }
}