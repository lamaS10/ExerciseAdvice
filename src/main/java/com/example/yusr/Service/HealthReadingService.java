package com.example.yusr.Service;


import com.example.yusr.Api.ApiException;
import com.example.yusr.Model.HealthReading;
import com.example.yusr.Model.Patient;
import com.example.yusr.Repository.HealthReadingRepository;
import com.example.yusr.Repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HealthReadingService {
    private final HealthReadingRepository healthReadingRepository;
    private final PatientRepository patientRepository;


    public List<HealthReading> getHealthReadings(){
        return healthReadingRepository.findAll();
    }


    public void addHealthReading(HealthReading healthReading){

        Patient patient = patientRepository.findPatientById(healthReading.getPatientId());

        if (patient == null){
            throw new ApiException("didn't find the patient");
        }

        if (healthReading.getReadingType().equals("blood_pressure")){

            if (healthReading.getSystolicValue() == null || healthReading.getDiastolicValue() == null){
                throw new ApiException("blood pressure requires systolic and diastolic values");
            }

            if (healthReading.getReadingValue() != null){
                throw new ApiException("reading value must be empty for blood pressure");
            }

            healthReading.setGlucoseMeasurementType(null);
            healthReading.setUnit("mmHg");
        }

        if (!healthReading.getReadingType().equals("blood_pressure")){

            if (healthReading.getReadingValue() == null){
                throw new ApiException("reading value is required");
            }

            if (healthReading.getSystolicValue() != null || healthReading.getDiastolicValue() != null){
                throw new ApiException("systolic and diastolic values must be empty");
            }

            if (healthReading.getReadingType().equals("glucose")){

                if (healthReading.getGlucoseMeasurementType() == null){
                    throw new ApiException("glucose measurement type is required");
                }

                healthReading.setUnit("mg/dL");
            } else {
                healthReading.setGlucoseMeasurementType(null);
            }

            if (healthReading.getReadingType().equals("temperature")){
                healthReading.setUnit("C");
            }

            if (healthReading.getReadingType().equals("weight")){
                healthReading.setUnit("kg");
            }

            if (healthReading.getReadingType().equals("heart_rate")){
                healthReading.setUnit("bpm");
            }
        }

        if (healthReading.getMeasuredAt() == null){
            healthReading.setMeasuredAt(LocalDateTime.now());
        }

        healthReading.setRecordedAt(LocalDateTime.now());

        healthReading.setReadingStatus(classifyReading(healthReading));

        healthReadingRepository.save(healthReading);

    }

    public void updateHealthReading(Integer id, HealthReading healthReading){

        HealthReading oldHealthReading = healthReadingRepository.findHealthReadingById(id);

        if (oldHealthReading == null){
            throw new ApiException("didn't find the health reading");
        }

        if (!oldHealthReading.getPatientId().equals(healthReading.getPatientId())){
            throw new ApiException("patient id cannot be changed");
        }

        if (healthReading.getReadingType().equals("blood_pressure")){

            if (healthReading.getSystolicValue() == null || healthReading.getDiastolicValue() == null){
                throw new ApiException("blood pressure requires systolic and diastolic values");
            }

            if (healthReading.getReadingValue() != null){
                throw new ApiException("reading value must be empty for blood pressure");
            }

            oldHealthReading.setUnit("mmHg");
            oldHealthReading.setGlucoseMeasurementType(null);
        }

        if (!healthReading.getReadingType().equals("blood_pressure")){

            if (healthReading.getReadingValue() == null){
                throw new ApiException("reading value is required");
            }

            if (healthReading.getSystolicValue() != null || healthReading.getDiastolicValue() != null){
                throw new ApiException("systolic and diastolic values must be empty");
            }

            if (healthReading.getReadingType().equals("glucose")){

                if (healthReading.getGlucoseMeasurementType() == null){
                    throw new ApiException("glucose measurement type is required");
                }

                oldHealthReading.setUnit("mg/dL");
                oldHealthReading.setGlucoseMeasurementType(healthReading.getGlucoseMeasurementType());

            } else {

                oldHealthReading.setGlucoseMeasurementType(null);
            }

            if (healthReading.getReadingType().equals("temperature")){
                oldHealthReading.setUnit("C");
            }

            if (healthReading.getReadingType().equals("weight")){
                oldHealthReading.setUnit("kg");
            }

            if (healthReading.getReadingType().equals("heart_rate")){
                oldHealthReading.setUnit("bpm");
            }
        }

        oldHealthReading.setReadingType(healthReading.getReadingType());
        oldHealthReading.setReadingValue(healthReading.getReadingValue());
        oldHealthReading.setSystolicValue(healthReading.getSystolicValue());
        oldHealthReading.setDiastolicValue(healthReading.getDiastolicValue());
        oldHealthReading.setNotes(healthReading.getNotes());

        if (healthReading.getMeasuredAt() != null){
            oldHealthReading.setMeasuredAt(healthReading.getMeasuredAt());
        }

        oldHealthReading.setReadingStatus(classifyReading(oldHealthReading));

        healthReadingRepository.save(oldHealthReading);
    }

    public void deleteHealthReading(Integer id){

        HealthReading healthReading = healthReadingRepository.findHealthReadingById(id);

        if (healthReading == null){
            throw new ApiException("didn't find the health reading");
        }

        healthReadingRepository.delete(healthReading);
    }

    public List<HealthReading> getPatientHealthReadings(Integer patientId){

        Patient patient = patientRepository.findPatientById(patientId);

        if (patient == null){
            throw new ApiException("didn't find the patent");
        }

        return healthReadingRepository.findHealthReadingsByPatientId(patientId);
    }

    public List<HealthReading> getLatestHealthReadings(Integer patientId){

        Patient patient = patientRepository.findPatientById(patientId);

        if (patient == null){
            throw new ApiException("didn't find the patent");
        }

        List<HealthReading> latestReadings = new ArrayList<>();

        String[] readingTypes = {"blood_pressure", "glucose", "temperature", "weight", "heart_rate"};

        for (String readingType : readingTypes){

            HealthReading healthReading = healthReadingRepository.findTopByPatientIdAndReadingTypeOrderByMeasuredAtDesc(patientId, readingType);

            if (healthReading != null){
                latestReadings.add(healthReading);
            }
        }

        return latestReadings;
    }


    public List<HealthReading> getAbnormalHealthReadings(Integer patientId){

        Patient patient = patientRepository.findPatientById(patientId);

        if (patient == null){
            throw new ApiException("didn't find the patent");
        }

        List<HealthReading> healthReadings = healthReadingRepository.findHealthReadingsByPatientId(patientId);

        List<HealthReading> abnormalReadings = new ArrayList<>();

        for (HealthReading healthReading : healthReadings){

            if (healthReading.getReadingStatus().equals("elevated") || healthReading.getReadingStatus().equals("low")
                    || healthReading.getReadingStatus().equals("high") || healthReading.getReadingStatus().equals("critical")){

                abnormalReadings.add(healthReading);
            }
        }

        return abnormalReadings;
    }


    //helper method

    private String classifyReading(HealthReading healthReading){

        if (healthReading.getReadingType().equals("blood_pressure")){

            double systolic = healthReading.getSystolicValue();
            double diastolic = healthReading.getDiastolicValue();

            if (systolic > 180 || diastolic > 120){
                return "critical";
            }

            if (systolic >= 130 || diastolic >= 80){
                return "high";
            }

            if (systolic >= 120 && systolic <= 129 && diastolic < 80){
                return "elevated";
            }

            return "normal";
        }


        if (healthReading.getReadingType().equals("glucose")){

            double glucose = healthReading.getReadingValue();

            if (healthReading.getGlucoseMeasurementType().equals("fasting")){

                if (glucose < 70){
                    return "low";
                }

                if (glucose <= 130){
                    return "normal";
                }

                return "high";
            }


            if (healthReading.getGlucoseMeasurementType().equals("after_meal")){

                if (glucose < 70){
                    return "low";
                }

                if (glucose < 180){
                    return "normal";
                }

                return "high";
            }
        }


        return "not_classified";
    }
}
