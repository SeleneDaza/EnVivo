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

            boolean isFirstPage = true;
            for (PurchaseDetail detail : purchase.getDetails()) {
                for (int i = 1; i <= detail.getQuantity(); i++) {
                    isFirstPage = addTicketPage(document, purchase, detail, i, isFirstPage);
                }
            }

            document.close();
            return baos.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Error crítico al generar el PDF de las entradas", e);
        }
    }

    private boolean addTicketPage(Document doc, Purchase purchase, PurchaseDetail detail, int index, boolean isFirst)
            throws DocumentException, IOException {
        if (!isFirst) {
            doc.newPage();
        }

        String ticketId = String.format("P%d-D%d-T%d", purchase.getId(), detail.getId(), index);
        Image qrImage = createQrImage(purchase, detail, ticketId);
        writeTicketContent(doc, purchase, detail, ticketId, qrImage);

        return false;
    }

    private Image createQrImage(Purchase purchase, PurchaseDetail detail, String ticketId)
            throws IOException, DocumentException {
        String qrData = String.format("BOLETO:%s|EVENTO:%d|COMPRADOR:%s",
                ticketId, detail.getTicket().getEvent().getEvent_id(), purchase.getBuyerDocument());

        byte[] qrBytes = qrCodeService.generateQrCodeImage(qrData, 250, 250);
        Image image = Image.getInstance(qrBytes);
        image.setAlignment(Element.ALIGN_CENTER);

        return image;
    }

    private void writeTicketContent(Document doc, Purchase purchase, PurchaseDetail detail, String ticketId, Image qr)
            throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);

        addCenteredText(doc, "EnVivo - Entrada Oficial", titleFont);
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph("Evento: " + detail.getTicket().getEvent().getName(), titleFont));

        writeEventAndBuyerInfo(doc, purchase, detail);
        doc.add(qr);

        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        addCenteredText(doc, "ID Boleto: " + ticketId, smallFont);
    }

    private void writeEventAndBuyerInfo(Document doc, Purchase purchase, PurchaseDetail detail)
            throws DocumentException {
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        String date = detail.getTicket().getEvent().getDate() != null ?
                detail.getTicket().getEvent().getDate().toString() : "Fecha por definir";

        doc.add(new Paragraph("Fecha: " + date, normalFont));
        doc.add(new Paragraph("Tipo de Entrada: " + detail.getTicket().getTicketType().getName(), normalFont));
        doc.add(Chunk.NEWLINE);

        doc.add(new Paragraph("Comprador: " + purchase.getBuyerFullName(), normalFont));
        doc.add(new Paragraph("Documento: " + purchase.getBuyerDocument(), normalFont));
        doc.add(Chunk.NEWLINE);
    }

    private void addCenteredText(Document doc, String text, Font font) throws DocumentException {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        doc.add(paragraph);
    }
}