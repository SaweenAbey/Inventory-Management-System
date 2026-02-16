package com.Inventory.Backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteInventoryItemRequest {

    @NotNull(message = "Item ID is required")
    private Long itemId;

    private String reason;
}
