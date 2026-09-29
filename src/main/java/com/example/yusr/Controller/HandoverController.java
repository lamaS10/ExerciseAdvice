package com.example.yusr.Controller;


import com.example.yusr.Api.ApiResponse;
import com.example.yusr.Model.Handover;
import com.example.yusr.Service.HandoverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/handover")
@RequiredArgsConstructor
public class HandoverController {

    private final HandoverService handoverService;

    @GetMapping("/get-handovers")
    public ResponseEntity<?> getHandovers(){
        return ResponseEntity.status(200).body(handoverService.getHandovers());
    }

    @PostMapping("/add-handover")
    public ResponseEntity<?> addHandover(@RequestBody @Valid Handover handover){
        handoverService.addHandover(handover);
        return ResponseEntity.status(200).body(new ApiResponse("handover is added successfully"));
    }

    @PutMapping("/update-handover/{id}")
    public ResponseEntity<?> updateHandover(@PathVariable Integer id, @RequestBody @Valid Handover handover){
        handoverService.updateHandover(id, handover);
        return ResponseEntity.status(200).body(new ApiResponse("handover is updated successfully"));
    }

    @DeleteMapping("/delete-handover/{id}")
    public ResponseEntity<?> deleteHandover(@PathVariable Integer id){
        handoverService.deleteHandover(id);
        return ResponseEntity.status(200).body(new ApiResponse("handover is deleted successfully"));
    }

    @PutMapping("/accept-handover/{handoverId}/{caregiverId}")
    public ResponseEntity<?> acceptHandover(@PathVariable Integer handoverId, @PathVariable Integer caregiverId){
        handoverService.acceptHandover(handoverId, caregiverId);
        return ResponseEntity.status(200).body(new ApiResponse("handover is accepted successfully"));
    }


    @PutMapping("/reject-handover/{handoverId}/{caregiverId}")
    public ResponseEntity<?> rejectHandover(@PathVariable Integer handoverId, @PathVariable Integer caregiverId){
        handoverService.rejectHandover(handoverId, caregiverId);
        return ResponseEntity.status(200).body(new ApiResponse("handover is rejected successfully"));
    }

    @GetMapping("/get-pending-handover/{caregiverId}")
    public ResponseEntity<?> getPendingHandover(@PathVariable Integer caregiverId){
        Handover handover = handoverService.getPendingHandover(caregiverId);
        return ResponseEntity.status(200).body(handover);
    }


    @GetMapping("/get-sent-handovers/{caregiverId}")
    public ResponseEntity<?> getSentHandovers(@PathVariable Integer caregiverId){
        List<Handover> handovers = handoverService.getSentHandovers(caregiverId);
        return ResponseEntity.status(200).body(handovers);
    }
}
