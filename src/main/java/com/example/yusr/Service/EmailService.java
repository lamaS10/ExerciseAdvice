package com.example.yusr.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromEmail;


    // Send Care Summary Email
    public void sendSummaryEmail(String toEmail, String patientName, String summary) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("ملخص الرعاية - يسر - " + patientName);

        String formattedSummary = formatSummaryToHtml(summary);

        String emailContent =
                "<div dir='rtl' style='background:#F7F5F0; padding:30px; "
                        + "font-family:Arial,sans-serif; color:#2F3E37;'>"

                        + "<div style='max-width:650px; margin:auto; background:#FFFFFF; "
                        + "border-radius:16px; overflow:hidden; border:1px solid #E8E4DE;'>"

                        // Header
                        + "<div style='background:#40584D; color:#FFFFFF; "
                        + "padding:24px 28px; text-align:center;'>"

                        + "<img src='cid:yusrLogo' alt='يسر' "
                        + "style='width:90px; max-width:90px; height:auto; "
                        + "display:block; margin:0 auto 14px;'>"

                        + "<h2 style='margin:0 0 7px; font-size:22px;'>"
                        + "ملخص الرعاية"
                        + "</h2>"

                        + "<p style='margin:0; opacity:.8; font-size:13px;'>"
                        + "ملخص ذكي لأهم بيانات الرعاية المسجلة في يسر"
                        + "</p>"

                        + "</div>"

                        // Patient
                        + "<div style='padding:20px 28px; background:#F1F5F2; "
                        + "border-bottom:1px solid #E4EAE5;'>"

                        + "<span style='font-size:11px; color:#82998A;'>"
                        + "المريض"
                        + "</span>"

                        + "<h3 style='margin:5px 0 0; color:#40584D;'>"
                        + escapeHtml(patientName)
                        + "</h3>"

                        + "</div>"

                        // Summary
                        + "<div style='padding:26px 28px; font-size:14px; "
                        + "line-height:1.9;'>"
                        + formattedSummary
                        + "</div>"

                        // Notice
                        + "<div style='margin:0 28px 24px; padding:15px; "
                        + "background:#F2EEE7; border-radius:10px; color:#756B60; "
                        + "font-size:12px; line-height:1.8;'>"

                        + "<strong>تنبيه:</strong> "
                        + "تم إنشاء هذا الملخص بناءً على بيانات الرعاية المسجلة في يسر، "
                        + "ولا يمثل تشخيصًا أو توصية طبية."

                        + "</div>"

                        // Footer
                        + "<div style='padding:20px 28px; text-align:center; "
                        + "border-top:1px solid #E8E4DE; color:#82998A;'>"

                        + "<strong style='color:#40584D;'>يسر</strong>"

                        + "<p style='margin:6px 0 0; font-size:11px;'>"
                        + "رعاية أسهل... لحياة أفضل"
                        + "</p>"

                        + "</div>"

                        + "</div>"
                        + "</div>";

        helper.setText(emailContent, true);

        addYusrLogo(helper);

        mailSender.send(message);
    }



    // Send Patient Created Email
    public void sendPatientCreatedEmail(String toEmail, String caregiverName, String patientName, String patientCode) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("تم إضافة المريض إلى يسر - " + patientName);

        String emailContent =
                "<div dir='rtl' style='background:#F7F5F0; padding:30px; "
                        + "font-family:Arial,sans-serif; color:#2F3E37;'>"

                        + "<div style='max-width:650px; margin:auto; background:#FFFFFF; "
                        + "border-radius:16px; overflow:hidden; border:1px solid #E8E4DE;'>"

                        // Header
                        + "<div style='background:#40584D; color:#FFFFFF; "
                        + "padding:24px 28px; text-align:center;'>"

                        + "<img src='cid:yusrLogo' alt='يسر' "
                        + "style='width:90px; max-width:90px; height:auto; "
                        + "display:block; margin:0 auto 14px;'>"

                        + "<h2 style='margin:0 0 7px; font-size:22px;'>"
                        + "تم إضافة المريض بنجاح"
                        + "</h2>"

                        + "<p style='margin:0; opacity:.8; font-size:13px;'>"
                        + "مرحبًا بك في يسر"
                        + "</p>"

                        + "</div>"

                        // Content
                        + "<div style='padding:26px 28px;'>"

                        + "<p style='margin:0 0 12px; line-height:1.8;'>"
                        + "مرحبًا "
                        + "<strong style='color:#40584D;'>"
                        + escapeHtml(caregiverName)
                        + "</strong>،"
                        + "</p>"

                        + "<p style='margin:0 0 20px; line-height:1.8;'>"
                        + "تم إضافة المريض "
                        + "<strong style='color:#40584D;'>"
                        + escapeHtml(patientName)
                        + "</strong> إلى يسر بنجاح."
                        + "</p>"

                        // Patient Code
                        + "<div style='padding:18px; margin:18px 0; "
                        + "background:#F1F5F2; border-radius:11px; "
                        + "border:1px solid #DDE6DF;'>"

                        + "<span style='display:block; margin-bottom:7px; "
                        + "font-size:11px; color:#82998A;'>"
                        + "رقم المريض"
                        + "</span>"

                        + "<strong style='display:block; color:#40584D; "
                        + "font-size:20px; letter-spacing:1px;'>"
                        + escapeHtml(patientCode)
                        + "</strong>"

                        + "</div>"

                        + "<p style='margin:18px 0 0; color:#65756B; "
                        + "font-size:13px; line-height:1.8;'>"
                        + "احتفظ برقم المريض، فقد تحتاجه لربط سجل المريض "
                        + "مرة أخرى مستقبلًا."
                        + "</p>"

                        + "</div>"

                        // Footer
                        + "<div style='padding:20px 28px; text-align:center; "
                        + "border-top:1px solid #E8E4DE; color:#82998A;'>"

                        + "<strong style='color:#40584D;'>يسر</strong>"

                        + "<p style='margin:6px 0 0; font-size:11px;'>"
                        + "رعاية أسهل... لحياة أفضل"
                        + "</p>"

                        + "</div>"

                        + "</div>"
                        + "</div>";

        helper.setText(emailContent, true);

        addYusrLogo(helper);

        mailSender.send(message);
    }


    // Add Yusr Logo
    private void addYusrLogo(MimeMessageHelper helper) throws MessagingException {

        ClassPathResource logo = new ClassPathResource("static/images/yusr-logo.png");

        helper.addInline("yusrLogo", logo, "image/png");
    }



    // Format AI Summary
    private String formatSummaryToHtml(String summary) {

        if (summary == null || summary.isBlank()) {
            return "<p>لا يوجد محتوى للملخص.</p>";
        }

        String safeText = escapeHtml(summary);

        StringBuilder html = new StringBuilder();

        boolean listOpen = false;

        String[] lines = safeText.split("\\R");

        for (String rawLine : lines) {

            String line = rawLine.trim();

            if (line.isEmpty()) {

                if (listOpen) {
                    html.append("</ul>");
                    listOpen = false;
                }

                continue;
            }


            // ### Heading

            if (line.startsWith("### ")) {

                if (listOpen) {
                    html.append("</ul>");
                    listOpen = false;
                }

                html.append("<h4 style='color:#40584D; margin:18px 0 8px;'>");

                html.append(formatInlineMarkdown(line.substring(4)));

                html.append("</h4>");

                continue;
            }


            // ## Heading

            if (line.startsWith("## ")) {

                if (listOpen) {
                    html.append("</ul>");
                    listOpen = false;
                }

                html.append("<h3 style='color:#40584D; margin:20px 0 9px;'>");

                html.append(formatInlineMarkdown(line.substring(3)));

                html.append("</h3>");

                continue;
            }


            // # Heading

            if (line.startsWith("# ")) {

                if (listOpen) {
                    html.append("</ul>");
                    listOpen = false;
                }

                html.append("<h2 style='color:#40584D; margin:22px 0 10px;'>");

                html.append(formatInlineMarkdown(line.substring(2)));

                html.append("</h2>");

                continue;
            }


            // List

            if (line.startsWith("- ") || line.startsWith("* "))
            {

                if (!listOpen) {

                    html.append("<ul style='padding-right:22px; " + "margin:8px 0 15px;'>");

                    listOpen = true;
                }

                html.append("<li style='margin-bottom:6px;'>");

                html.append(formatInlineMarkdown(line.substring(2)));

                html.append("</li>");

                continue;
            }


            if (listOpen) {
                html.append("</ul>");
                listOpen = false;
            }

            html.append("<p style='margin:0 0 10px;'>");

            html.append(formatInlineMarkdown(line));

            html.append("</p>");
        }


        if (listOpen) {
            html.append("</ul>");
        }

        return html.toString();
    }


    // Format Bold Markdown


    private String formatInlineMarkdown(String text) {
        return text.replaceAll("\\*\\*(.*?)\\*\\*", "<strong style='color:#40584D;'>$1</strong>");
    }



    // Escape HTML
    private String escapeHtml(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#039;");
    }
}