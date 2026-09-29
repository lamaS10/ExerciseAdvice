package com.example.yusr.Controller;


import com.example.yusr.Api.ApiResponse;
import com.example.yusr.Model.ChronicCondition;
import com.example.yusr.Service.ChronicConditionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/chronic-condition")
@RequiredArgsConstructor
public class ChronicConditionController {

    private final ChronicConditionService chronicConditionService;

    @GetMapping("/get-chronic-conditions")
    public ResponseEntity<?>getChronicConditions(){
        return ResponseEntity.status(200).body(chronicConditionService.getChronicConditions());
    }

    @PostMapping("/add-chronic-condition")
    public ResponseEntity<?>addChronicCondition(@RequestBody @Valid ChronicCondition chronicCondition){
        chronicConditionService.addChronicCondition(chronicCondition);
        return ResponseEntity.status(200).body(new ApiResponse("Chronic Condition is added successfully"));
    }


    @PutMapping("/update-chronic-condition/{id}")
    public ResponseEntity<?>updateChronicCondition(@PathVariable Integer id,@RequestBody @Valid ChronicCondition chronicCondition){
        chronicConditionService.updateChronicCondition(id, chronicCondition);
        return ResponseEntity.status(200).body(new ApiResponse("Chronic Condition is updated successfully"));

    }

    @DeleteMapping("/delete-chronic-condition/{id}")
    public ResponseEntity<?>deleteChronicCondition(@PathVariable Integer id){
        chronicConditionService.deleteChronicCondition(id);
        return ResponseEntity.status(200).body(new ApiResponse("Chronic Condition is deleted successfully"));
    }

}
