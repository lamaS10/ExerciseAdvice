package com.example.yusr.Service;

import com.example.yusr.Api.ApiException;
import com.example.yusr.Model.CareTask;
import com.example.yusr.Model.Handover;
import com.example.yusr.Model.Caregiver;
import com.example.yusr.Model.Patient;
import com.example.yusr.Repository.CareTaskRepository;
import com.example.yusr.Repository.HandoverRepository;
import com.example.yusr.Repository.CaregiverRepository;
import com.example.yusr.Repository.PatientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class HandoverService {

    private final HandoverRepository handoverRepository;
    private final PatientRepository patientRepository;
    private final CaregiverRepository caregiverRepository;
    private final CareTaskRepository careTaskRepository;
    private final WhatsAppService whatsAppService;


    public List<Handover> getHandovers(){
        return handoverRepository.findAll();
    }

    public void addHandover(Handover handover){

        Patient patient = patientRepository.findPatientById(handover.getPatientId());

        if (patient == null){
            throw new ApiException("didn't find the patient");
        }

        Caregiver fromCaregiver = caregiverRepository.findCaregiverById(handover.getFromCaregiverId());
        Caregiver toCaregiver = caregiverRepository.findCaregiverById(handover.getToCaregiverId());

        if (fromCaregiver == null || toCaregiver == null){
            throw new ApiException("didn't find the caregiver");
        }

        if (fromCaregiver.getId().equals(toCaregiver.getId())){
            throw new ApiException("cannot handover to yourself");
        }

        if (fromCaregiver.getPatientId() == null ||toCaregiver.getPatientId() == null || !fromCaregiver.getPatientId().equals(handover.getPatientId()) ||!toCaregiver.getPatientId().equals(handover.getPatientId())){
            throw new ApiException("caregivers do not belong to patient");
        }

        if (!fromCaregiver.getIsCurrentCaregiver()){
            throw new ApiException("sender is not current caregiver");
        }

        if (!fromCaregiver.getStatus().equals("active") || !toCaregiver.getStatus().equals("active")){
            throw new ApiException("caregiver is inactive");
        }

        if (handoverRepository.existsHandoverByPatientIdAndStatus(handover.getPatientId(), "pending")){
            throw new ApiException("patient already has pending handover");
        }

        if (!handover.getExpectedAcceptanceAt().isAfter(LocalDateTime.now())){
            throw new ApiException("expected acceptance must be in the future");
        }

        handover.setCreatedAt(LocalDateTime.now());
        handover.setAcceptedAt(null);
        handover.setRejectedAt(null);
        handover.setStatus("pending");
        handover.setOverdueAlertSent(false);

        handoverRepository.save(handover);
        String internationalPhone = "+966" + toCaregiver.getPhoneNumber().substring(1);

        whatsAppService.sendMessage(internationalPhone,
                "طلب استلام رعاية جديد من يسر 🌿\n\n" +
                        "تم إرسال طلب لك لاستلام مسؤولية رعاية المريض " +
                        patient.getFullName() + ".\n\n" +
                        "يمكنك الدخول إلى يسر للاطلاع على تفاصيل التسليم وقبول الطلب أو رفضه.");
    }


    public void updateHandover(Integer id, Handover handover){

        Handover oldHandover = handoverRepository.findHandoverById(id);

        if (oldHandover == null){
            throw new ApiException("didn't find the handover");
        }

        if (!oldHandover.getStatus().equals("pending")){
            throw new ApiException("only pending handover can be updated");

        }

        if (!oldHandover.getPatientId().equals(handover.getPatientId()) ||!oldHandover.getFromCaregiverId().equals(handover.getFromCaregiverId()) || !oldHandover.getToCaregiverId().equals(handover.getToCaregiverId())){
            throw new ApiException("handover relationships cannot be changed");
        }

        if (!handover.getExpectedAcceptanceAt().isAfter(LocalDateTime.now())){
            throw new ApiException("expected acceptance must be in the future\"");
        }

        oldHandover.setSummary(handover.getSummary());
        oldHandover.setExpectedAcceptanceAt(handover.getExpectedAcceptanceAt());
        oldHandover.setOverdueAlertSent(false);
        handoverRepository.save(oldHandover);
    }



    public void deleteHandover(Integer id){

        Handover handover = handoverRepository.findHandoverById(id);

        if (handover == null){
            throw new ApiException("didn't find the handover");
        }

        if (handover.getStatus().equals("accepted")){
            throw new ApiException("accepted handover cannot be deleted");
        }

        handoverRepository.delete(handover);

    }

    @Transactional
    public void acceptHandover(Integer handoverId, Integer caregiverId){

        Handover handover = handoverRepository.findHandoverById(handoverId);

        if (handover == null){
            throw new ApiException("didn't find the handover");
        }

        if (!handover.getStatus().equals("pending")){
            throw new ApiException("handover is not pending");
        }

        if (!handover.getToCaregiverId().equals(caregiverId)){
            throw new ApiException("only receiving caregiver can accept handover");
        }

        Caregiver fromCaregiver = caregiverRepository.findCaregiverById(handover.getFromCaregiverId());

        Caregiver toCaregiver = caregiverRepository.findCaregiverById(handover.getToCaregiverId());

        if (fromCaregiver == null || toCaregiver == null){
            throw new ApiException("didn't find the caregiver");
        }

        if (fromCaregiver.getPatientId() == null || toCaregiver.getPatientId() == null
                || !fromCaregiver.getPatientId().equals(handover.getPatientId())
                || !toCaregiver.getPatientId().equals(handover.getPatientId())){
            throw new ApiException("caregivers do not belong to patient");
        }

        if (!fromCaregiver.getIsCurrentCaregiver()){
            throw new ApiException("sender is no longer current caregiver");
        }

        if (!fromCaregiver.getStatus().equals("active") || !toCaregiver.getStatus().equals("active")){
            throw new ApiException("caregiver is inactive");
        }

        fromCaregiver.setIsCurrentCaregiver(false);
        toCaregiver.setIsCurrentCaregiver(true);

        List<CareTask> pendingTasks = careTaskRepository.findCareTasksByPatientIdAndCaregiverIdAndStatus(handover.getPatientId(), fromCaregiver.getId(), "pending");

        for (CareTask careTask : pendingTasks){
            careTask.setCaregiverId(toCaregiver.getId());
        }

        handover.setStatus("accepted");
        handover.setAcceptedAt(LocalDateTime.now());
        handover.setRejectedAt(null);

        caregiverRepository.save(fromCaregiver);
        caregiverRepository.save(toCaregiver);
        careTaskRepository.saveAll(pendingTasks);
        handoverRepository.save(handover);
        String internationalPhone = "+966" + fromCaregiver.getPhoneNumber().substring(1);

        whatsAppService.sendMessage(internationalPhone,
                "تم استلام الرعاية بنجاح 🤍\n\n" +
                        "وافق " + toCaregiver.getFullName() +
                        " على طلب تسليم الرعاية.\n\n" +
                        "تم نقل مسؤولية الرعاية والمهام المعلقة إليه بنجاح في يسر.");
    }


    public void rejectHandover(Integer handoverId, Integer caregiverId){

        Handover handover = handoverRepository.findHandoverById(handoverId);

        if (handover == null){
            throw new ApiException("didn't find the handover");
        }

        if (!handover.getStatus().equals("pending")){
            throw new ApiException("handover is not pending");
        }

        if (!handover.getToCaregiverId().equals(caregiverId)){
            throw new ApiException("only receiving caregiver can reject handover");
        }

        Caregiver fromCaregiver = caregiverRepository.findCaregiverById(handover.getFromCaregiverId());

        Caregiver toCaregiver = caregiverRepository.findCaregiverById(handover.getToCaregiverId());

        if (fromCaregiver == null || toCaregiver == null){
            throw new ApiException("didn't find the caregiver");
        }

        if (fromCaregiver.getPatientId() == null || toCaregiver.getPatientId() == null
                || !fromCaregiver.getPatientId().equals(handover.getPatientId()) || !toCaregiver.getPatientId().equals(handover.getPatientId())){
            throw new ApiException("caregivers do not belong to patient");

        }
        if (!fromCaregiver.getStatus().equals("active") || !toCaregiver.getStatus().equals("active")){
            throw new ApiException("caregiver is inactive");
        }

        handover.setStatus("rejected");
        handover.setRejectedAt(LocalDateTime.now());
        handover.setAcceptedAt(null);

        handoverRepository.save(handover);

        String internationalPhone = "+966" + fromCaregiver.getPhoneNumber().substring(1);

        whatsAppService.sendMessage(
                internationalPhone,
                "تحديث من يسر 🌿\n\n" +
                        "لم يتم قبول طلب تسليم الرعاية المرسل إلى " +
                        toCaregiver.getFullName() + ".\n\n" +
                        "يمكنك الدخول إلى يسر لمراجعة التفاصيل واختيار الإجراء المناسب.");
    }

    public Handover getPendingHandover(Integer caregiverId){

        Caregiver caregiver = caregiverRepository.findCaregiverById(caregiverId);

        if (caregiver == null){
            throw new ApiException("didn't find the caregiver");
        }

        return handoverRepository.findHandoverByToCaregiverIdAndStatus(caregiverId, "pending");
    }


    public List<Handover> getSentHandovers(Integer caregiverId){

        Caregiver caregiver = caregiverRepository.findCaregiverById(caregiverId);

        if (caregiver == null){
            throw new ApiException("didn't find the caregiver");
        }

        return handoverRepository.findHandoversByFromCaregiverIdOrderByCreatedAtDesc(caregiverId);
    }

    @Scheduled(fixedRate = 15, timeUnit = TimeUnit.MINUTES)
    public void checkOverdueHandovers(){

        List<Handover> overdueHandovers = handoverRepository.findHandoversByStatusAndExpectedAcceptanceAtBeforeAndOverdueAlertSent("pending", LocalDateTime.now(), false);

        for (Handover handover : overdueHandovers){

            Caregiver fromCaregiver = caregiverRepository.findCaregiverById(handover.getFromCaregiverId());

            Caregiver toCaregiver = caregiverRepository.findCaregiverById(handover.getToCaregiverId());

            if (fromCaregiver == null || toCaregiver == null){
                continue;
            }

            String fromPhone = "+966" + fromCaregiver.getPhoneNumber().substring(1);

            String toPhone = "+966" + toCaregiver.getPhoneNumber().substring(1);

            whatsAppService.sendMessage(toPhone,
                    "تذكير لطيف من يسر 🌿\n\n" +
                            "لديك طلب لاستلام مسؤولية الرعاية من " +
                            fromCaregiver.getFullName() +
                            " وما زال بانتظار ردك بعد انتهاء وقت الاستجابة المتوقع.\n\n" +
                            "يمكنك الدخول إلى يسر لقبول الطلب أو رفضه."
            );

            whatsAppService.sendMessage(fromPhone,
                    "تحديث من يسر 🌿\n\n" +
                            "طلب تسليم الرعاية المرسل إلى " +
                            toCaregiver.getFullName() +
                            " ما زال بانتظار الرد بعد انتهاء الوقت المتوقع.\n\n" +
                            "ستبقى مسؤول الرعاية الحالي حتى يتم قبول التسليم، ويمكنك إلغاء الطلب واختيار مقدم رعاية آخر.");

            handover.setOverdueAlertSent(true);
            handoverRepository.save(handover);
        }
    }
}
