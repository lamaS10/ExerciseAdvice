package com.example.yusr.Controller;

import com.example.yusr.Api.ApiResponse;
import com.example.yusr.Model.CareSummary;
import com.example.yusr.Service.CareSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/care-summary")
@RequiredArgsConstructor
public class CareSummaryController {

    private final CareSummaryService careSummaryService;


    @GetMapping("/get-latest-summary/{caregiverId}")
    public ResponseEntity<?> getLatestSummary(@PathVariable Integer caregiverId) {
        CareSummary careSummary = careSummaryService.getLatestCareSummary(caregiverId);
        return ResponseEntity.status(200).body(careSummary);
    }


    @PostMapping("/generate-on-demand/{caregiverId}")
    public ResponseEntity<?> generateOnDemandSummary(@PathVariable Integer caregiverId, @RequestParam LocalDate fromDate, @RequestParam LocalDate toDate) {
        careSummaryService.generateOnDemandSummary(caregiverId, fromDate, toDate);
        return ResponseEntity.status(200).body(new ApiResponse("care summery generated successfully"));
    }


    @PostMapping("/send-latest-summary-email/{caregiverId}")
    public ResponseEntity<?> sendLatestSummaryEmail(@PathVariable Integer caregiverId) {
        careSummaryService.sendLatestSummaryEmail(caregiverId);
        return ResponseEntity.status(200).body(new ApiResponse("sent summary successfully"));
    }
}