package com.taxiincome.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InitUserRequest(
        @NotBlank(message = "Tên không được để trống")
        @Size(max = 100, message = "Tên tối đa 100 ký tự")
        String displayName) {
}
