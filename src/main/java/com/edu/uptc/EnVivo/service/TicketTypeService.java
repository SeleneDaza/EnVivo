package com.edu.uptc.EnVivo.service;

import com.edu.uptc.EnVivo.dto.CreateTicketTypeDTO;
import com.edu.uptc.EnVivo.entity.TicketType;
import com.edu.uptc.EnVivo.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;

    public TicketType saveTicketType(CreateTicketTypeDTO dto) {
        validateName(dto.getName(), dto.getId());
        TicketType ticketType = mapToEntity(dto);
        return ticketTypeRepository.save(ticketType);
    }

    public CreateTicketTypeDTO getTicketTypeDTO(Long id) {
        TicketType ticketType = getById(id);
        return mapToDTO(ticketType);
    }

    public List<TicketType> getTicketTypes() {
        return ticketTypeRepository.findAll();
    }

    public TicketType getById(Long id) {
        return ticketTypeRepository.findById(id).orElse(new TicketType());
    }

    public void deleteTicketType(Long id) {
        ticketTypeRepository.deleteById(id);
    }

    private TicketType mapToEntity(CreateTicketTypeDTO dto) {
        TicketType ticketType = new TicketType();
        ticketType.setId(dto.getId());
        ticketType.setName(dto.getName().trim());
        return ticketType;
    }

    private CreateTicketTypeDTO mapToDTO(TicketType ticketType) {
        CreateTicketTypeDTO dto = new CreateTicketTypeDTO();
        dto.setId(ticketType.getId());
        dto.setName(ticketType.getName());
        return dto;
    }

    private void validateName(String name, Long id) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del tipo de entrada es obligatorio");
        }

        ticketTypeRepository.findByNameIgnoreCase(name.trim()).ifPresent(existing -> {
            boolean isAnotherRecord = !existing.getId().equals(id);
            if (isAnotherRecord) {
                throw new IllegalArgumentException("Ya existe un tipo de entrada con ese nombre");
            }
        });
    }
}


