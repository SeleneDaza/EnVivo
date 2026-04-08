package com.edu.uptc.EnVivo.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String newEmail;
    private String newPassword;
    private String confirmPassword;
}

