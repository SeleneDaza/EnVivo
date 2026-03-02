package com.edu.uptc.EnVivo.dto;

import lombok.Data;

@Data
public class CreateCategoryDTO {
    private Long categoryId; // Importante para la edición
    private String name;
}