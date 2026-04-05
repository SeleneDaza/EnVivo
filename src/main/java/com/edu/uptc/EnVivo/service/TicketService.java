package com.edu.uptc.EnVivo.service;

import com.edu.uptc.EnVivo.dto.CreateTicketDTO;
import com.edu.uptc.EnVivo.dto.TicketDTO;
import com.edu.uptc.EnVivo.entity.Event;
import com.edu.uptc.EnVivo.entity.Ticket;
import com.edu.uptc.EnVivo.entity.TicketType;
import com.edu.uptc.EnVivo.repository.TicketRepository;
import com.edu.uptc.EnVivo.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketTypeRepository ticketTypeRepository;

    /**
     * Crea un ticket individual asociado a un evento y un tipo de entrada
     *
     * @param event El evento al cual pertenece el ticket
     * @param dto Datos del ticket (tipo, precio, cantidad)
     * @return El ticket creado
     */
    public Ticket createTicket(Event event, CreateTicketDTO dto) {
        validateTicketData(dto);

        TicketType ticketType = ticketTypeRepository.findById(dto.getTicketTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Tipo de entrada no encontrado: " + dto.getTicketTypeId()));

        Ticket ticket = new Ticket();
        ticket.setEvent(event);
        ticket.setTicketType(ticketType);
        ticket.setPrice(dto.getPrice());
        ticket.setAvailableQuantity(dto.getAvailableQuantity());

        return ticketRepository.save(ticket);
    }

    /**
     * Crea múltiples tickets para un evento
     *
     * @param event El evento al cual pertenecen los tickets
     * @param ticketDTOs Lista de datos de tickets
     * @return Set de tickets creados
     */
    @Transactional
    public Set<Ticket> createTicketsForEvent(Event event, List<CreateTicketDTO> ticketDTOs) {
        Set<Ticket> tickets = new HashSet<>();
        
        if (ticketDTOs != null && !ticketDTOs.isEmpty()) {
            for (CreateTicketDTO dto : ticketDTOs) {
                Ticket ticket = createTicket(event, dto);
                tickets.add(ticket);
            }
        }
        
        return tickets;
    }

    /**
     * Obtiene todos los tickets de un evento
     *
     * @param eventId Id del evento
     * @return Lista de tickets
     */
    public List<Ticket> getTicketsByEvent(Long eventId) {
        return ticketRepository.findByEventId(eventId);
    }

    /**
     * Actualiza un ticket
     *
     * @param ticketId Id del ticket a actualizar
     * @param dto Nuevos datos del ticket
     * @return El ticket actualizado
     */
    public Ticket updateTicket(Long ticketId, CreateTicketDTO dto) {
        validateTicketData(dto);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado: " + ticketId));

        TicketType ticketType = ticketTypeRepository.findById(dto.getTicketTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Tipo de entrada no encontrado: " + dto.getTicketTypeId()));

        ticket.setTicketType(ticketType);
        ticket.setPrice(dto.getPrice());
        ticket.setAvailableQuantity(dto.getAvailableQuantity());

        return ticketRepository.save(ticket);
    }

    /**
     * Elimina un ticket
     *
     * @param ticketId Id del ticket a eliminar
     */
    public void deleteTicket(Long ticketId) {
        ticketRepository.deleteById(ticketId);
    }

    /**
     * Elimina todos los tickets de un evento
     *
     * @param eventId Id del evento
     */
    @Transactional
    public void deleteTicketsByEvent(Long eventId) {
        List<Ticket> tickets = ticketRepository.findByEventId(eventId);
        ticketRepository.deleteAll(tickets);
    }

    /**
     * Obtiene un ticket por su ID
     *
     * @param ticketId Id del ticket
     * @return El ticket encontrado
     */
    public Ticket getTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado: " + ticketId));
    }

    /**
     * Valida que los datos del ticket sean correctos
     *
     * @param dto Datos del ticket a validar
     */
    private void validateTicketData(CreateTicketDTO dto) {
        if (dto.getTicketTypeId() == null || dto.getTicketTypeId() <= 0) {
            throw new IllegalArgumentException("El tipo de entrada es requerido");
        }
        
        if (dto.getPrice() == null || dto.getPrice() < 0) {
            throw new IllegalArgumentException("El precio debe ser mayor o igual a 0");
        }
        
        if (dto.getAvailableQuantity() == null || dto.getAvailableQuantity() <= 0) {
            throw new IllegalArgumentException("La cantidad disponible debe ser mayor a 0");
        }
    }

    /**
     * Convierte una entidad Ticket a TicketDTO
     *
     * @param ticket El ticket a convertir
     * @return TicketDTO con la información del ticket
     */
    public TicketDTO convertToDTO(Ticket ticket) {
        TicketDTO dto = new TicketDTO();
        dto.setId(ticket.getId());
        dto.setTicketTypeId(ticket.getTicketType().getId());
        dto.setTicketTypeName(ticket.getTicketType().getName());
        dto.setPrice(ticket.getPrice());
        dto.setAvailableQuantity(ticket.getAvailableQuantity());
        return dto;
    }

    /**
     * Obtiene todos los tickets de un evento como DTOs
     *
     * @param eventId Id del evento
     * @return Lista de TicketDTOs
     */
    public List<TicketDTO> getTicketsAsDTO(Long eventId) {
        return ticketRepository.findByEventId(eventId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}



