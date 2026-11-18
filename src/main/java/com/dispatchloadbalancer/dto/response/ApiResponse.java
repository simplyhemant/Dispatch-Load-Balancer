package com.dispatchloadbalancer.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Builder @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API response envelope")
public class ApiResponse<T> {

    @Schema(description = "Status indicator ('success' or 'error')", example = "success")
    private final String status;      // "success" or "error"

    @Schema(description = "Descriptive outcome message", example = "Delivery orders accepted.")
    private final String message;

    @Schema(description = "Optional response payload")
    private final T data;

    @Schema(description = "Timestamp of response generation")
    private final LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>("success", message, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("success", message, data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", message, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>("error", message, data, LocalDateTime.now());
    }
}