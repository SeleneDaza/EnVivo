package com.edu.uptc.EnVivo.dto;

import com.edu.uptc.EnVivo.entity.Event;

public class EventReporteDTO {

    private final Event evento;
    private final long totalInteresados;

    public EventReporteDTO(Event evento, long totalInteresados) {
        this.evento = evento;
        this.totalInteresados = totalInteresados;
    }

    public Event getEvento() {
        return evento;
    }

    public long getTotalInteresados() {
        return totalInteresados;
    }
}

