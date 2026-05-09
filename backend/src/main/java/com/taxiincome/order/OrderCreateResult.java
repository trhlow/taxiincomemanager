package com.taxiincome.order;

import com.taxiincome.order.dto.OrderResponse;

/**
 * Outcome of {@link OrderCommandService#create} so HTTP layer can return
 * {@code 201 Created} for a new row vs {@code 200 OK} for an idempotent replay.
 */
public record OrderCreateResult(boolean newlyCreated, OrderResponse response) {
}
