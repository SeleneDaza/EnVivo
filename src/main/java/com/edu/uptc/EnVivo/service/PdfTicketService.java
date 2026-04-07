package com.edu.uptc.EnVivo.service;

import com.edu.uptc.EnVivo.entity.Purchase;
import com.edu.uptc.EnVivo.entity.PurchaseDetail;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PdfTicketService {

    private final QrCodeService qrCodeService;

    public byte[] generateTicketsPdf(Purchase purchase) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            Document document = new Document(PageSize.A5);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            boolean isFirstPage = true;

            for (PurchaseDetail detail : purchase.getDetails()) {
                
                for (int i = 1; i <= detail.getQuantity(); i++) {
                    
                    if (!isFirstPage) {
                        document.newPage(); 
                    }
                    isFirstPage = false;

                    String uniqueTicketId = String.format("P%d-D%d-T%d", 
                            purchase.getId(), detail.getId(), i);
                    
                    String qrData = String.format("BOLETO:%s|EVENTO:%d|COMPRADOR:%s", 
                            uniqueTicketId, 
                            detail.getTicket().getEvent().getEvent_id(), 
                            purchase.getBuyerDocument());

                    byte[] qrBytes = qrCodeService.generateQrCodeImage(qrData, 250, 250);
                    Image qrImage = Image.getInstance(qrBytes);
                    qrImage.setAlignment(Element.ALIGN_CENTER);

                    Paragraph header = new Paragraph("EnVivo - Entrada Oficial", titleFont);
                    header.setAlignment(Element.ALIGN_CENTER);
                    document.add(header);
                    document.add(Chunk.NEWLINE);

                    document.add(new Paragraph("Evento: " + detail.getTicket().getEvent().getName(), titleFont));
                    
                    String fechaEvento = detail.getTicket().getEvent().getDate() != null ? 
                            detail.getTicket().getEvent().getDate().toString() : "Fecha por definir";
                    document.add(new Paragraph("Fecha: " + fechaEvento, normalFont));
                    document.add(new Paragraph("Tipo de Entrada: " + detail.getTicket().getTicketType().getName(), normalFont));
                    document.add(Chunk.NEWLINE);

                    document.add(new Paragraph("Comprador: " + purchase.getBuyerFullName(), normalFont));
                    document.add(new Paragraph("Documento: " + purchase.getBuyerDocument(), normalFont));
                    document.add(Chunk.NEWLINE);

                    document.add(qrImage);
                    
                    Paragraph footer = new Paragraph("ID Boleto: " + uniqueTicketId, smallFont);
                    footer.setAlignment(Element.ALIGN_CENTER);
                    document.add(footer);
                }
            }

            document.close();
            return baos.toByteArray();

        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Error crítico al generar el PDF de las entradas", e);
        }
    }
}