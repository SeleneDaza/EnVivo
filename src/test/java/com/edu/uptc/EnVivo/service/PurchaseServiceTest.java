package com.edu.uptc.EnVivo.service;

import com.edu.uptc.EnVivo.dto.BuyerInfoDTO;
import com.edu.uptc.EnVivo.dto.PaymentInfoDTO;
import com.edu.uptc.EnVivo.dto.PurchaseCheckoutRequestDTO;
import com.edu.uptc.EnVivo.dto.PurchaseConfirmationDTO;
import com.edu.uptc.EnVivo.entity.Event;
import com.edu.uptc.EnVivo.entity.Purchase;
import com.edu.uptc.EnVivo.entity.Ticket;
import com.edu.uptc.EnVivo.entity.TicketType;
import com.edu.uptc.EnVivo.entity.User;
import com.edu.uptc.EnVivo.repository.PurchaseRepository;
import com.edu.uptc.EnVivo.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;
    
    @Mock
    private TicketRepository ticketRepository;
    
    @Mock
    private UserService userService;
    
    @Mock
    private PdfTicketService pdfTicketService;

    @InjectMocks
    private PurchaseService purchaseService;

    private User mockUser;
    private Ticket mockTicket;
    private PurchaseCheckoutRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@correo.com");
        mockUser.setFullName("Juan Perez");

        Event mockEvent = new Event();
        mockEvent.setEvent_id(100L);
        mockEvent.setName("Concierto Rock");

        TicketType mockTicketType = new TicketType();
        mockTicketType.setName("VIP");

        mockTicket = new Ticket();
        mockTicket.setId(10L);
        mockTicket.setEvent(mockEvent);
        mockTicket.setTicketType(mockTicketType);
        mockTicket.setPrice(50000);
        mockTicket.setAvailableQuantity(50); 

        BuyerInfoDTO buyer = new BuyerInfoDTO();
        buyer.setFullName("Juan Perez");
        buyer.setDocument("12345678");
        buyer.setEmail("test@correo.com");
        buyer.setPhone("3001234567");

        PaymentInfoDTO payment = new PaymentInfoDTO();
        payment.setCardHolder("Juan Perez");
        payment.setCardNumber("1234567812345678");
        payment.setExpiry("12/25");
        payment.setCvv("123");

        validRequest = new PurchaseCheckoutRequestDTO();
        validRequest.setEventId(100L);
        validRequest.setTicketId(10L);
        validRequest.setQuantity(2);
        validRequest.setBuyer(buyer);
        validRequest.setPayment(payment);
    }

    @Test
    void checkout_Exitoso() {
        when(userService.findByUserName("test@correo.com")).thenReturn(Optional.of(mockUser));
        when(ticketRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(mockTicket));
        
        Purchase savedPurchase = new Purchase();
        savedPurchase.setId(999L); 
        savedPurchase.setPurchaseDate(java.time.LocalDateTime.now()); 
        savedPurchase.setBuyerFullName(validRequest.getBuyer().getFullName());
        savedPurchase.setBuyerEmail(validRequest.getBuyer().getEmail());
        savedPurchase.setBuyerDocument(validRequest.getBuyer().getDocument());
        
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(savedPurchase);

        PurchaseConfirmationDTO result = purchaseService.checkout("test@correo.com", validRequest);

        assertNotNull(result);
        assertEquals(999L, result.getPurchaseId());
        assertEquals("Concierto Rock", result.getEventName());
        assertEquals(1000, result.getTotal()); 
        assertEquals(48, mockTicket.getAvailableQuantity());
        
        verify(purchaseRepository, times(1)).save(any(Purchase.class));
    }

    @Test
    void checkout_FallaPorCantidadInsuficiente() {
        validRequest.setQuantity(100);
        
        when(userService.findByUserName("test@correo.com")).thenReturn(Optional.of(mockUser));
        when(ticketRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(mockTicket));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            purchaseService.checkout("test@correo.com", validRequest);
        });

        assertEquals("No hay disponibilidad suficiente para la cantidad solicitada.", exception.getMessage());
        
        verify(purchaseRepository, never()).save(any(Purchase.class));
    }
}