package com.parshuramKund.Service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.parshuramKund.DTO.ApplicantDTO;
import com.parshuramKund.DTO.CoApplicant;
import com.parshuramKund.Entity.Applicant;
import com.parshuramKund.Repository.ApplicantRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class PdfService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PdfService.class);
	
	 @Autowired
		private  ApplicantRepository applicantRepository ;

    public byte[] generatePdf(String id) throws Exception {
    	Optional<Applicant> applicantOptional=applicantRepository.findById(id);
    	if (!applicantOptional.isPresent()) {
            throw new RuntimeException("Applicant not found");
        }
    	Applicant applicant=applicantOptional.get();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, out);

        document.open();

        // ===== Logo =====
        ClassPathResource resource = new ClassPathResource("static/Parshuramlogo.png");
        Image logo = Image.getInstance(resource.getURL());
        logo.scaleToFit(65, 65);

        // ===== Header Table =====
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1.5f, 6.5f});
        
        PdfPCell logoCell = new PdfPCell(logo);
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(logoCell);

        PdfPCell textCell = new PdfPCell();
        textCell.setBorder(Rectangle.NO_BORDER);
        textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        textCell.setPaddingLeft(10);
        
        Paragraph title = new Paragraph("PARSHURAM KUND MAKAR SANKRANTI MELA 2027", 
            new Font(Font.FontFamily.HELVETICA, 15, Font.BOLD, new BaseColor(26, 42, 64))); // #1a2a40
        title.setSpacingAfter(2f);
        textCell.addElement(title);
        
        Paragraph subtitle = new Paragraph("Government of Arunachal Pradesh | Pilgrim Entry Pass", 
            new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, new BaseColor(100, 110, 120)));
        textCell.addElement(subtitle);
        
        headerTable.addCell(textCell);
        document.add(headerTable);

        // ===== Divider Line =====
        PdfPTable divider = new PdfPTable(1);
        divider.setWidthPercentage(100);
        PdfPCell divCell = new PdfPCell();
        divCell.setBackgroundColor(new BaseColor(217, 119, 6)); // Accent Orange/Gold
        divCell.setFixedHeight(2f);
        divCell.setBorder(Rectangle.NO_BORDER);
        divider.addCell(divCell);
        divider.setSpacingBefore(10f);
        divider.setSpacingAfter(15f);
        document.add(divider);

        // ===== Section 1: Registration Status =====
        Font statusFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(22, 163, 74)); // Green
        Paragraph statusPara = new Paragraph("REGISTRATION CONFIRMED", statusFont);
        statusPara.setAlignment(Element.ALIGN_CENTER);
        statusPara.setSpacingAfter(10f);
        document.add(statusPara);

        // ===== Section 2: Pilgrim Details Header =====
        Paragraph secHeader = new Paragraph("PRIMARY PILGRIM DETAILS", 
            new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, new BaseColor(26, 42, 64)));
        secHeader.setSpacingAfter(6f);
        document.add(secHeader);

        // ===== Pilgrim Details Table =====
        PdfPTable detailsTable = new PdfPTable(4);
        detailsTable.setWidthPercentage(100);
        detailsTable.setWidths(new float[]{3f, 3f, 3f, 3f});
        detailsTable.setSpacingAfter(15f);

        BaseColor labelBg = new BaseColor(248, 250, 252); // Very light gray/blue slate
        BaseColor borderColor = new BaseColor(226, 232, 240);
        Font labelFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, new BaseColor(71, 85, 105));
        Font valueFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.BLACK);

        // Aadhaar Decryption & Masking
        String maskedAadhar = "Not Provided";
        if (applicant.getAadharNumber() != null && !applicant.getAadharNumber().trim().isEmpty()) {
            try {
                String decrypted = com.parshuramKund.Util.AadharEncryptionUtil.decrypt(applicant.getAadharNumber());
                maskedAadhar = com.parshuramKund.Util.AadharEncryptionUtil.mask(decrypted);
            } catch (Exception e) {
                maskedAadhar = "XXXX-XXXX-XXXX";
            }
        }

        // Add details to table
        addDetailCell(detailsTable, "Registration ID:", applicant.getId(), labelBg, borderColor, labelFont, valueFont);
        addDetailCell(detailsTable, "Booking Date:", applicant.getBookingDate(), labelBg, borderColor, labelFont, valueFont);
        
        addDetailCell(detailsTable, "Pilgrim Name:", applicant.getFullName(), labelBg, borderColor, labelFont, valueFont);
        addDetailCell(detailsTable, "Age / Gender:", applicant.getAge() + " / " + applicant.getGender(), labelBg, borderColor, labelFont, valueFont);
        
        addDetailCell(detailsTable, "Holy Dip Date:", applicant.getDateOfHoliDipDate(), labelBg, borderColor, labelFont, valueFont);
        addDetailCell(detailsTable, "Contact Phone:", applicant.getPhone(), labelBg, borderColor, labelFont, valueFont);
        
        addDetailCell(detailsTable, "Aadhaar Number:", maskedAadhar, labelBg, borderColor, labelFont, valueFont);
        addDetailCell(detailsTable, "Email Address:", applicant.getEmail() != null ? applicant.getEmail() : "Not Provided", labelBg, borderColor, labelFont, valueFont);

        // Present Address
        String presentAddressStr = applicant.getPresentAddress() + " , " + applicant.getPresentDistrict() + " , " + applicant.getPresentState() + " - " + applicant.getPresentPinCode();
        addDetailRow(detailsTable, "Present Address:", presentAddressStr, labelBg, borderColor, labelFont, valueFont);

        // Permanent Address
        String permanentAddressStr = applicant.getPermanentAddress() + " , " + applicant.getPermanentDistrict() + " , " + applicant.getPermanentState() + " - " + applicant.getPermanentPinCode();
        addDetailRow(detailsTable, "Permanent Address:", permanentAddressStr, labelBg, borderColor, labelFont, valueFont);

        document.add(detailsTable);

        // ===== Section 3: Co-Applicants =====
        if (Boolean.TRUE.equals(applicant.getIsPresentCoApplicant()) && applicant.getCoApplicant() != null && !applicant.getCoApplicant().trim().isEmpty()) {
            Paragraph coAppHeader = new Paragraph("CO-TRAVELERS DETAILS", 
                new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, new BaseColor(26, 42, 64)));
            coAppHeader.setSpacingBefore(10f);
            coAppHeader.setSpacingAfter(6f);
            document.add(coAppHeader);

            ObjectMapper mapper = new ObjectMapper();
            CoApplicant[] coApplicants = mapper.readValue(applicant.getCoApplicant(), CoApplicant[].class);

            PdfPTable coAppTable = new PdfPTable(4);
            coAppTable.setWidthPercentage(100);
            coAppTable.setSpacingAfter(15f);
            coAppTable.setWidths(new float[]{5f, 1.5f, 2f, 3.5f});

            // Co-App Headers
            String[] headers = {"Name", "Age", "Gender", "Co-morbidities"};
            BaseColor headerBg = new BaseColor(26, 42, 64);
            Font tableHeaderFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE);

            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, tableHeaderFont));
                cell.setBackgroundColor(headerBg);
                cell.setPadding(6);
                cell.setBorderColor(borderColor);
                coAppTable.addCell(cell);
            }

            // Co-App Rows
            for (CoApplicant p : coApplicants) {
                PdfPCell nameCell = new PdfPCell(new Phrase(p.getName(), valueFont));
                nameCell.setPadding(6);
                nameCell.setBorderColor(borderColor);
                coAppTable.addCell(nameCell);

                PdfPCell ageCell = new PdfPCell(new Phrase(String.valueOf(p.getAge()), valueFont));
                ageCell.setPadding(6);
                ageCell.setBorderColor(borderColor);
                coAppTable.addCell(ageCell);

                PdfPCell genderCell = new PdfPCell(new Phrase(p.getGender(), valueFont));
                genderCell.setPadding(6);
                genderCell.setBorderColor(borderColor);
                coAppTable.addCell(genderCell);

                String comorbidities = p.getComorbidities() != null && !p.getComorbidities().trim().isEmpty() ? p.getComorbidities() : "None";
                PdfPCell cmCell = new PdfPCell(new Phrase(comorbidities, valueFont));
                cmCell.setPadding(6);
                cmCell.setBorderColor(borderColor);
                coAppTable.addCell(cmCell);
            }
            document.add(coAppTable);
        }

        // ===== Section 4: Instructions & QR Code =====
        PdfPTable bottomTable = new PdfPTable(2);
        bottomTable.setWidthPercentage(100);
        bottomTable.setWidths(new float[]{6.5f, 3.5f});
        bottomTable.setSpacingBefore(10f);

        // Left: Instructions
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setPaddingRight(10);
        
        Paragraph instHeader = new Paragraph("IMPORTANT INSTRUCTIONS:", 
            new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, new BaseColor(26, 42, 64)));
        instHeader.setSpacingAfter(4f);
        leftCell.addElement(instHeader);

        Font instFont = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, new BaseColor(74, 85, 104));
        
        Paragraph inst1 = new Paragraph("1. Please carry a printed copy of this pass and a valid original photo identity proof (Aadhaar Card, Voter ID, etc.) for verification at entry checkpoints.", instFont);
        inst1.setSpacingAfter(2f);
        leftCell.addElement(inst1);
        
        Paragraph inst2 = new Paragraph("2. Present this pass (either physical or digital copy on mobile) to the scanning officials upon arrival.", instFont);
        inst2.setSpacingAfter(2f);
        leftCell.addElement(inst2);
        
        Paragraph inst3 = new Paragraph("3. Adhere strictly to the security, health guidelines, and sanitization protocols established by the Mela administration.", instFont);
        inst3.setSpacingAfter(2f);
        leftCell.addElement(inst3);
        
        Paragraph inst4 = new Paragraph("4. Holy dip is permitted only at the designated bathing ghats under the surveillance of lifeguards and disaster management teams.", instFont);
        inst4.setSpacingAfter(2f);
        leftCell.addElement(inst4);

        Paragraph inst5 = new Paragraph("5. In case of medical or security assistance, contact the nearest transit camp or call the emergency response cell.", instFont);
        inst5.setSpacingAfter(2f);
        leftCell.addElement(inst5);

        Paragraph inst6 = new Paragraph("6. Along with the registration, applicant needs to apply for ILP through https://www.eilp.arunachal.gov.in/preTuristEIlpKYC or ILP will be available at check-in gates of Arunachal Pradesh.", instFont);
        leftCell.addElement(inst6);

        bottomTable.addCell(leftCell);

        // Right: QR Code
        ApplicantDTO applicantDTO = new ApplicantDTO();
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(
            qrCodeWriter.encode(
                applicant.getId(),
                BarcodeFormat.QR_CODE,
                200,
                200
            )
        );

        ByteArrayOutputStream qrBaos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "png", qrBaos);
        Image qr = Image.getInstance(qrBaos.toByteArray());
        qr.scaleToFit(150, 150);
        qr.setAlignment(Element.ALIGN_CENTER);

        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.BOX);
        rightCell.setBorderColor(borderColor);
        rightCell.setBackgroundColor(new BaseColor(250, 250, 250));
        rightCell.setPadding(4);
        rightCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        rightCell.addElement(qr);
        
        Paragraph scanPrompt = new Paragraph("Scan to Verify", new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, new BaseColor(26, 42, 64)));
        scanPrompt.setAlignment(Element.ALIGN_CENTER);
        scanPrompt.setSpacingBefore(4f);
        rightCell.addElement(scanPrompt);

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        String formattedDate = now.format(formatter);
        Paragraph dateStr = new Paragraph("Issued: " + formattedDate, new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL, BaseColor.GRAY));
        dateStr.setAlignment(Element.ALIGN_CENTER);
        rightCell.addElement(dateStr);

        bottomTable.addCell(rightCell);
        document.add(bottomTable);

        // ===== Section 5: Electronic Stamp / Signature Disclaimer =====
        document.add(Chunk.NEWLINE);
        Paragraph disclaimer = new Paragraph("This is a system-generated Pilgrim Entry Pass. No physical signature is required.", 
            new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY));
        disclaimer.setAlignment(Element.ALIGN_CENTER);
        disclaimer.setSpacingBefore(15f);
        document.add(disclaimer);

        document.close();

        return out.toByteArray();
    }

    private void addDetailCell(PdfPTable table, String label, String value, BaseColor labelBg, BaseColor borderColor, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBackgroundColor(labelBg);
        labelCell.setPadding(6);
        labelCell.setBorderColor(borderColor);
        table.addCell(labelCell);

        PdfPCell valCell = new PdfPCell(new Phrase(value != null ? value : "", valueFont));
        valCell.setPadding(6);
        valCell.setBorderColor(borderColor);
        table.addCell(valCell);
    }

    private void addDetailRow(PdfPTable table, String label, String value, BaseColor labelBg, BaseColor borderColor, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBackgroundColor(labelBg);
        labelCell.setPadding(6);
        labelCell.setBorderColor(borderColor);
        table.addCell(labelCell);

        PdfPCell valCell = new PdfPCell(new Phrase(value != null ? value : "", valueFont));
        valCell.setColspan(3);
        valCell.setPadding(6);
        valCell.setBorderColor(borderColor);
        table.addCell(valCell);
    }
    
    
}

