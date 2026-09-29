package com.example.yusr.Service;


import com.example.yusr.Api.ApiException;
import com.example.yusr.Model.CareTask;
import com.example.yusr.Model.Caregiver;
import com.example.yusr.Model.CaregiverInvitation;
import com.example.yusr.Model.Patient;
import com.example.yusr.Repository.CareTaskRepository;
import com.example.yusr.Repository.CaregiverInvitationRepository;
import com.example.yusr.Repository.CaregiverRepository;
import com.example.yusr.Repository.PatientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CaregiverService {

    private final CaregiverRepository caregiverRepository;
    private final PatientRepository patientRepository;
    private final CaregiverInvitationRepository caregiverInvitationRepository;
    private final WhatsAppService whatsAppService;
    private final CareTaskRepository careTaskRepository;

    public List<Caregiver>getCaregivers(){
        return caregiverRepository.findAll();
    }

    public Caregiver getCaregiverById(Integer id){
        Caregiver caregiver = caregiverRepository.findCaregiverById(id);

        if (caregiver == null){
            throw new ApiException("caregiver not found");
        }

        return caregiver;    }

    public void addCaregiver(Caregiver caregiver){

        Caregiver checkEmail = caregiverRepository.findCaregiverByEmail(caregiver.getEmail());

        if (checkEmail != null){
            throw new ApiException("email already exists");
        }

        Caregiver checkPhone = caregiverRepository.findCaregiverByPhoneNumber(caregiver.getPhoneNumber());

        if (checkPhone != null){
            throw new ApiException("phone number already exists");
        }

        caregiver.setPatientId(null);
        caregiver.setRole("unassigned");
        caregiver.setAssignedAt(null);
        caregiver.setStatus("active");
        caregiver.setIsCurrentCaregiver(false);

        caregiverRepository.save(caregiver);

        // If the caregiver accepted an invitation before registration,
        // automatically link them to the patient after registration.
        CaregiverInvitation invitation = caregiverInvitationRepository.findCaregiverInvitationByEmailAndStatus(caregiver.getEmail(), "accepted");

        if (invitation != null){

            linkCaregiverToPatient(caregiver, invitation);

            String internationalPhone = "+966" + caregiver.getPhoneNumber().substring(1);

            whatsAppService.sendMessage(
                    internationalPhone,
                    "أهلًا بك في يسر 🌿\n\n" +
                            "تم إنشاء حسابك وربطك بالمريض بنجاح 🤍\n\n" +
                            "يمكنك الآن الدخول إلى يسر والبدء بمتابعة الرعاية."
            );
        }

    }

    public void updateCaregiver(Integer id, Caregiver caregiver){

        Caregiver oldCaregiver = caregiverRepository.findCaregiverById(id);

        if (oldCaregiver == null){
            throw new ApiException("didn't find the caregiver");
        }

        Caregiver checkEmail =
                caregiverRepository.findCaregiverByEmail(caregiver.getEmail());

        if (checkEmail != null && !checkEmail.getId().equals(id)){
            throw new ApiException("email already exists");
        }

        Caregiver checkPhone = caregiverRepository.findCaregiverByPhoneNumber(caregiver.getPhoneNumber());

        if (checkPhone != null && !checkPhone.getId().equals(id)){
            throw new ApiException("phone number already exists");
        }

        oldCaregiver.setFullName(caregiver.getFullName());
        oldCaregiver.setEmail(caregiver.getEmail());
        oldCaregiver.setPassword(caregiver.getPassword());
        oldCaregiver.setPhoneNumber(caregiver.getPhoneNumber());

        caregiverRepository.save(oldCaregiver);

    }



    public void deleteCaregiver(Integer id){

        Caregiver caregiver = caregiverRepository.findCaregiverById(id);

        if (caregiver == null){
            throw new ApiException("didn't find the caregiver");
        }

        if (caregiver.getPatientId() != null){
            throw new ApiException("caregiver is assigned to a patient");
        }

        caregiverRepository.delete(caregiver);

    }

    // Sends a WhatsApp invitation to a caregiver to join a patient.
    // Only the primary caregiver can send the invitation.
    // The caregiver will be assigned only after accepting the invitation.
    public void assignCaregiverToPatient(Integer primaryCaregiverId, Integer patientId, String email, String phoneNumber){

        Caregiver primaryCaregiver = caregiverRepository.findCaregiverById(primaryCaregiverId);

        if (primaryCaregiver == null) {
            throw new ApiException("didn't find primary caregiver");
        }

        Patient patient = patientRepository.findPatientById(patientId);

        if (patient == null) {
            throw new ApiException("didn't find patient");

        }

        if (primaryCaregiver.getPatientId() == null || !primaryCaregiver.getPatientId().equals(patientId)) {
            throw new ApiException("caregiver does not belong to this patient");
        }

        if (!primaryCaregiver.getRole().equals("primary")) {
            throw new ApiException("only primary caregiver can add another caregiver");
        }



        Caregiver caregiver = caregiverRepository.findCaregiverByEmail(email);

        if (caregiver != null) {
            if (caregiver.getPatientId() != null) {
                throw new ApiException("caregiver already assigned to a patient");
            }
            phoneNumber = caregiver.getPhoneNumber();
        }

        CaregiverInvitation checkInvitation = caregiverInvitationRepository.findCaregiverInvitationByPatientIdAndEmailAndStatus(patientId, email, "pending");

        if (checkInvitation != null) {
            throw new ApiException("caregiver already has a pending invitation");
        }


        List<Caregiver> patientCaregivers = caregiverRepository.findCaregiversByPatientId(patientId);

        String role;

        if (patientCaregivers.size() == 1) {
            role = "secondary";
        } else {
            role = "backup";
        }
        CaregiverInvitation invitation = new CaregiverInvitation();

        invitation.setPatientId(patientId);
        invitation.setEmail(email);
        invitation.setPhoneNumber(phoneNumber);
        invitation.setRole(role);
        invitation.setStatus("pending");
        invitation.setCreatedAt(LocalDateTime.now());


        caregiverInvitationRepository.save(invitation);

        String internationalPhone = "+966" + phoneNumber.substring(1);

        whatsAppService.sendCaregiverInvitation(
                internationalPhone,
                role,
                patient.getFullName()
        );
    }





    // Handles the caregiver response from WhatsApp.
    // Accept: saves the invitation as accepted and assigns the caregiver if registered.
    // Reject: saves the invitation as rejected.
    public Object respondToCaregiverInvitation(String phoneNumber, String response) {

        CaregiverInvitation invitation = caregiverInvitationRepository.findCaregiverInvitationByPhoneNumberAndStatus(phoneNumber, "pending");

        if (invitation == null) {
            throw new ApiException("pending invitation not found");
        }

        if (!response.equalsIgnoreCase("accept") && !response.equalsIgnoreCase("reject")) {
            throw new ApiException("response must be accept or reject");
        }

        String internationalPhone = "+966" + phoneNumber.substring(1);

        if (response.equalsIgnoreCase("reject")) {

            invitation.setStatus("rejected");
            caregiverInvitationRepository.save(invitation);

            whatsAppService.sendMessage(
                    internationalPhone,
                    "تم تحديث الدعوة 🌿\n\n" +
                            "تم تسجيل اعتذارك عن الانضمام إلى فريق الرعاية في يسر.\n\n" +
                            "شكرًا لتفاعلك 🤍"
            );
            return "invitation rejected";
        }

        invitation.setStatus("accepted");
        caregiverInvitationRepository.save(invitation);

        Caregiver caregiver = caregiverRepository.findCaregiverByEmail(invitation.getEmail());

        if (caregiver == null) {

            whatsAppService.sendMessage(
                    internationalPhone,
                    "تم قبول دعوتك بنجاح 🤍\n\n" +
                            "باقي خطوة بسيطة لإكمال انضمامك لفريق الرعاية 🌿\n" +
                            "سجّل حسابك في يسر، وسيتم ربطك بالمريض تلقائيًا."
            );

            return "invitation accepted, caregiver must register";
        }

        if (caregiver.getPatientId() != null) {
            throw new ApiException("caregiver already assigned to a patient");
        }

        linkCaregiverToPatient(caregiver, invitation);

        whatsAppService.sendMessage(
                internationalPhone,
                "أهلًا بك في فريق الرعاية 🤍\n\n" +
                        "تم قبول دعوتك وربط حسابك بالمريض بنجاح في يسر 🌿\n\n" +
                        "يمكنك الآن الدخول إلى يسر ومتابعة تفاصيل الرعاية."
        );

        return "invitation accepted and caregiver assigned successfully";
    }


    // Gets the caregiver's latest WhatsApp response and processes the invitation.
    public Object checkCaregiverInvitationResponse(String phoneNumber) {

        CaregiverInvitation invitation = caregiverInvitationRepository.findCaregiverInvitationByPhoneNumberAndStatus(phoneNumber, "pending");

        if (invitation == null) {
            throw new ApiException("pending invitation not found");
        }

        String response = whatsAppService.getLatestCaregiverResponse(
                phoneNumber,
                invitation.getCreatedAt()
        );

        if (response == null) {
            throw new ApiException("no response found");
        }

        response = response.trim();

        if (response.equals("قبول")) {
            return respondToCaregiverInvitation(phoneNumber, "accept");
        }

        if (response.equals("رفض")) {
            return respondToCaregiverInvitation(phoneNumber, "reject");
        }

        return "caregiver has not responded to the invitation";
    }


    // Unlinks a secondary or backup caregiver from the patient without deleting the caregiver account
    @Transactional
    public void unlinkCaregiverFromPatient(Integer primaryCaregiverId, Integer caregiverId) {

        Caregiver primaryCaregiver = caregiverRepository.findCaregiverById(primaryCaregiverId);

        if (primaryCaregiver == null) {
            throw new ApiException("primary caregiver not found");
        }

        Caregiver caregiver = caregiverRepository.findCaregiverById(caregiverId);

        if (caregiver == null) {
            throw new ApiException("caregiver not found");
        }

        if (primaryCaregiver.getPatientId() == null) {
            throw new ApiException("primary caregiver is not assigned to a patient");
        }

        if (!primaryCaregiver.getRole().equals("primary")) {
            throw new ApiException("only primary caregiver can unlink caregiver");

        }

        if (caregiver.getPatientId() == null) {
            throw new ApiException("caregiver is not assigned to a patient");
        }

        if (!primaryCaregiver.getPatientId().equals(caregiver.getPatientId())) {
            throw new ApiException("caregivers do not belong to the same patient");
        }

        if (caregiver.getRole().equals("primary")) {
            throw new ApiException("primary caregiver cannot be unlinked");
        }

        Integer patientId = caregiver.getPatientId();

        // If the caregiver being removed is currently responsible,
        // responsibility returns to the primary caregiver.
        if (Boolean.TRUE.equals(caregiver.getIsCurrentCaregiver())) {

            primaryCaregiver.setIsCurrentCaregiver(true);

            List<CareTask> pendingTasks = careTaskRepository.findCareTasksByPatientIdAndCaregiverIdAndStatus(patientId, caregiver.getId(), "pending");

            for (CareTask task : pendingTasks) {
                task.setCaregiverId(primaryCaregiver.getId());
            }

            careTaskRepository.saveAll(pendingTasks);
            caregiverRepository.save(primaryCaregiver);
        }

        caregiver.setPatientId(null);
        caregiver.setRole("unassigned");
        caregiver.setAssignedAt(null);
        caregiver.setIsCurrentCaregiver(false);

        caregiverRepository.save(caregiver);

    }

    public List<Caregiver> getPatientCaregivers(Integer patientId){

        Patient patient = patientRepository.findPatientById(patientId);

        if (patient == null){
            throw new ApiException("didn't find the patent");
        }

        List<Caregiver>caregivers=caregiverRepository.findCaregiversByPatientId(patientId);
        if (caregivers.isEmpty()){
            throw new ApiException("patient has no caregivers");
        }
        return caregivers;
    }

    @Transactional
    public void transferPrimaryRole(Integer primaryCaregiverId, Integer newPrimaryCaregiverId){

        Caregiver primaryCaregiver = caregiverRepository.findCaregiverById(primaryCaregiverId);

        if (primaryCaregiver == null){
            throw new ApiException("primary caregiver not found");
        }

        Caregiver newPrimaryCaregiver = caregiverRepository.findCaregiverById(newPrimaryCaregiverId);

        if (newPrimaryCaregiver == null){
            throw new ApiException("new primary caregiver not found");
        }

        // Current caregiver must actually be the primary
        if (!primaryCaregiver.getRole().equals("primary")){
            throw new ApiException("only primary caregiver can transfer primary role");
        }

        // Both caregivers must be assigned to a patient
        if (primaryCaregiver.getPatientId() == null || newPrimaryCaregiver.getPatientId() == null){
            throw new ApiException("caregiver is not assigned to a patient");
        }

        // Both caregivers must belong to the same patient
        if (!primaryCaregiver.getPatientId().equals(newPrimaryCaregiver.getPatientId())){
            throw new ApiException("caregivers do not belong to the same patient");

        }

        // New primary must be secondary or backup
        if (!newPrimaryCaregiver.getRole().equals("secondary") && !newPrimaryCaregiver.getRole().equals("backup")){
            throw new ApiException("new primary caregiver must be secondary or backup");
        }

        // Old primary stays with the patient as secondary
        primaryCaregiver.setRole("secondary");

        // Selected caregiver becomes the new primary
        newPrimaryCaregiver.setRole("primary");

        caregiverRepository.save(primaryCaregiver);
        caregiverRepository.save(newPrimaryCaregiver);
    }


    @Transactional
    public Object leavePatient(Integer caregiverId, Integer newPrimaryCaregiverId) {

        Caregiver caregiver = caregiverRepository.findCaregiverById(caregiverId);

        if (caregiver == null) {
            throw new ApiException("caregiver not found");
        }

        if (caregiver.getPatientId() == null) {
            throw new ApiException("caregiver is not assigned to a patient");
        }

        Integer patientId = caregiver.getPatientId();

        Patient patient = patientRepository.findPatientById(patientId);

        if (patient == null) {
            throw new ApiException("patient not found");
        }

        Boolean hasOtherCaregiver = caregiverRepository.existsCaregiverByPatientIdAndIdNot(patientId, caregiverId);

        // Last caregiver leaves
        if (!hasOtherCaregiver) {

            caregiver.setPatientId(null);
            caregiver.setRole("unassigned");
            caregiver.setAssignedAt(null);
            caregiver.setIsCurrentCaregiver(false);

            caregiverRepository.save(caregiver);

            patient.setStatus("inactive");
            patientRepository.save(patient);

            return "caregiver left and patient became inactive";
        }

        // Secondary / Backup leaves
        if (!caregiver.getRole().equals("primary")) {

            if (Boolean.TRUE.equals(caregiver.getIsCurrentCaregiver())) {

                Caregiver primaryCaregiver =
                        caregiverRepository
                                .findCaregiversByPatientId(patientId)
                                .stream()
                                .filter(c ->
                                        c.getRole().equals("primary"))
                                .findFirst()
                                .orElse(null);

                if (primaryCaregiver == null) {
                    throw new ApiException("primary caregiver not found");

                }

                primaryCaregiver.setIsCurrentCaregiver(true);

                List<CareTask> pendingTasks = careTaskRepository.findCareTasksByPatientIdAndCaregiverIdAndStatus(patientId, caregiver.getId(), "pending");

                for (CareTask task : pendingTasks) {
                    task.setCaregiverId(primaryCaregiver.getId());
                }

                careTaskRepository.saveAll(pendingTasks);
                caregiverRepository.save(primaryCaregiver);
            }

            caregiver.setPatientId(null);
            caregiver.setRole("unassigned");
            caregiver.setAssignedAt(null);
            caregiver.setIsCurrentCaregiver(false);

            caregiverRepository.save(caregiver);

            return "caregiver left patient successfully";
        }

        // Primary leaves while others remain
        if (newPrimaryCaregiverId == null) {
            throw new ApiException("new primary caregiver is required");
        }

        Caregiver newPrimaryCaregiver = caregiverRepository.findCaregiverById(newPrimaryCaregiverId);

        if (newPrimaryCaregiver == null) {
            throw new ApiException("new primary caregiver not found");
        }

        if (newPrimaryCaregiver.getPatientId() == null || !newPrimaryCaregiver.getPatientId().equals(patientId)) {
            throw new ApiException("new primary caregiver does not belong to this patient");

        }

        if (!newPrimaryCaregiver.getRole().equals("secondary") && !newPrimaryCaregiver.getRole().equals("backup")) {
            throw new ApiException("new primary caregiver must be secondary or backup");
        }

        newPrimaryCaregiver.setRole("primary");

        // If leaving primary currently has responsibility,
        // move responsibility and pending tasks too.
        if (Boolean.TRUE.equals(caregiver.getIsCurrentCaregiver())) {

            newPrimaryCaregiver.setIsCurrentCaregiver(true);

            List<CareTask> pendingTasks = careTaskRepository.findCareTasksByPatientIdAndCaregiverIdAndStatus(patientId, caregiver.getId(), "pending");

            for (CareTask task : pendingTasks) {
                task.setCaregiverId(newPrimaryCaregiver.getId());
            }

            careTaskRepository.saveAll(pendingTasks);
        }

        caregiverRepository.save(newPrimaryCaregiver);

        caregiver.setPatientId(null);
        caregiver.setRole("unassigned");
        caregiver.setAssignedAt(null);
        caregiver.setIsCurrentCaregiver(false);

        caregiverRepository.save(caregiver);

        return "primary caregiver left and primary role transferred successfully";
    }

    @Transactional
    public void acceptCaregiverInvitation(Integer invitationId, Integer caregiverId){

        CaregiverInvitation invitation = caregiverInvitationRepository.findCaregiverInvitationById(invitationId);

        if (invitation == null){
            throw new ApiException("Invitation not found");
        }

        Caregiver caregiver = caregiverRepository.findCaregiverById(caregiverId);

        if (caregiver == null){
            throw new ApiException("caregiver not found");
        }

        if (!invitation.getStatus().equals("pending")){
            throw new ApiException("invitation is not pending");
        }

        if (!invitation.getEmail().equals(caregiver.getEmail())){
            throw new ApiException("invitation does not belong to caregiver");
        }

        if (caregiver.getPatientId() != null){
            throw new ApiException("caregiver already assigned to a patient");
        }

        Patient patient = patientRepository.findPatientById(invitation.getPatientId());

        if (patient == null){
            throw new ApiException("patient not found");
        }

        invitation.setStatus("accepted");
        caregiverInvitationRepository.save(invitation);

        linkCaregiverToPatient(caregiver, invitation);
    }


    public void rejectCaregiverInvitation(Integer invitationId, Integer caregiverId){

        CaregiverInvitation invitation = caregiverInvitationRepository.findCaregiverInvitationById(invitationId);

        if (invitation == null){
            throw new ApiException("Invitation not found");
        }

        Caregiver caregiver = caregiverRepository.findCaregiverById(caregiverId);

        if (caregiver == null){
            throw new ApiException("caregiver not found");
        }

        if (!invitation.getStatus().equals("pending")){
            throw new ApiException("invitation is not pending");
        }

        if (!invitation.getEmail().equals(caregiver.getEmail())){
            throw new ApiException("invitation does not belong to caregiver");
        }

        invitation.setStatus("rejected");
        caregiverInvitationRepository.save(invitation);

    }

    public CaregiverInvitation getPendingCaregiverInvitation(Integer caregiverId){

        Caregiver caregiver = caregiverRepository.findCaregiverById(caregiverId);

        if (caregiver == null){
            throw new ApiException("caregiver not found");
        }

        CaregiverInvitation invitation = caregiverInvitationRepository.findCaregiverInvitationByEmailAndStatus(caregiver.getEmail(), "pending");

        if (invitation == null){
            throw new ApiException("invitation not found");

        }

        checkCaregiverInvitationResponse(caregiver.getPhoneNumber());

        return caregiverInvitationRepository.findCaregiverInvitationByEmailAndStatus(caregiver.getEmail(), "pending");
    }

    public Caregiver login(String email, String password){

        Caregiver caregiver = caregiverRepository.findCaregiverByEmail(email);

        if (caregiver == null){
            throw new ApiException("caregiver not found");

        }

        if (!caregiver.getPassword().equals(password)){
            throw new ApiException("email or password is incorrect");
        }

        return caregiver;
    }

    public List<CaregiverInvitation> getPendingPatientInvitations(Integer patientId) {

        Patient patient = patientRepository.findPatientById(patientId);

        if (patient == null) {
            throw new ApiException("patient not found");

        }

        return caregiverInvitationRepository.findCaregiverInvitationsByPatientIdAndStatus(patientId, "pending");
    }

    public void cancelCaregiverInvitation(Integer invitationId, Integer primaryCaregiverId) {

        Caregiver primaryCaregiver = caregiverRepository.findCaregiverById(primaryCaregiverId);

        if (primaryCaregiver == null) {
            throw new ApiException("primary caregiver not found");
        }

        CaregiverInvitation invitation = caregiverInvitationRepository.findCaregiverInvitationById(invitationId);

        if (invitation == null) {
            throw new ApiException("invitation not found");
        }

        if (!primaryCaregiver.getRole().equals("primary")) {
            throw new ApiException("only primary caregiver can cancel invitation");

        }

        if (primaryCaregiver.getPatientId() == null || !primaryCaregiver.getPatientId().equals(invitation.getPatientId())) {
            throw new ApiException("invitation does not belong to this patient");
        }

        if (!invitation.getStatus().equals("pending")) {
            throw new ApiException("invitation is not pending");
        }

        invitation.setStatus("cancelled");

        caregiverInvitationRepository.save(invitation);

    }


    //helper method
    // Links the caregiver to the patient after the invitation is accepted.
    private void linkCaregiverToPatient(Caregiver caregiver, CaregiverInvitation invitation) {

        caregiver.setPatientId(invitation.getPatientId());
        caregiver.setRole(invitation.getRole());
        caregiver.setAssignedAt(LocalDateTime.now());
        caregiver.setIsCurrentCaregiver(false);

        caregiverRepository.save(caregiver);
    }
}
