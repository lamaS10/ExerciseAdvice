package com.example.yusr.Controller;

import com.example.yusr.Api.ApiResponse;
import com.example.yusr.Model.SummaryPreference;
import com.example.yusr.Service.SummaryPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/summary-preference")
@RequiredArgsConstructor
public class SummaryPreferenceController {

    private final SummaryPreferenceService summaryPreferenceService;


    @GetMapping("/get-summary-preference/{caregiverId}")
    public ResponseEntity<?> getSummaryPreferenceByCaregiverId(@PathVariable Integer caregiverId) {
        SummaryPreference summaryPreference = summaryPreferenceService.getSummaryPreferenceByCaregiverId(caregiverId);
        return ResponseEntity.status(200).body(summaryPreference);
    }


    @PostMapping("/add-summary-preference")
    public ResponseEntity<?> addSummaryPreference(@RequestBody @Valid SummaryPreference summaryPreference) {
        summaryPreferenceService.addSummaryPreference(summaryPreference);
        return ResponseEntity.status(200).body(new ApiResponse("summary preference added successfully"));
    }


    @PutMapping("/update-summary-preference/{id}")
    public ResponseEntity<?> updateSummaryPreference(@PathVariable Integer id, @RequestBody @Valid SummaryPreference summaryPreference) {
        summaryPreferenceService.updateSummaryPreference(id, summaryPreference);
        return ResponseEntity.status(200).body(new ApiResponse("summary preference updated successfully"));
    }


    @DeleteMapping("/delete-summary-preference/{id}")
    public ResponseEntity<?> deleteSummaryPreference(@PathVariable Integer id) {
        summaryPreferenceService.deleteSummaryPreference(id);
        return ResponseEntity.status(200).body(new ApiResponse("summary preference deleted successfully"));
    }

    @PutMapping("/change-auto-generate/{caregiverId}")
    public ResponseEntity<?> changeAutoGenerate(@PathVariable Integer caregiverId) {
        SummaryPreference summaryPreference = summaryPreferenceService.changeAutoGenerate(caregiverId);
        return ResponseEntity.status(200).body(summaryPreference);
    }


}