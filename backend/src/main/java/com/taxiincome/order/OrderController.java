package com.taxiincome.order;

import com.taxiincome.order.dto.CreateOrderRequest;
import com.taxiincome.order.dto.DailyOrdersResponse;
import com.taxiincome.order.dto.OrderResponse;
import com.taxiincome.order.dto.PeriodOrdersResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@Validated
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;

    public OrderController(OrderCommandService orderCommandService,
                           OrderQueryService orderQueryService) {
        this.orderCommandService = orderCommandService;
        this.orderQueryService = orderQueryService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @Valid @RequestBody CreateOrderRequest req,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        OrderCreateResult result = orderCommandService.create(req, Optional.ofNullable(idempotencyKey));
        return ResponseEntity
                .status(result.newlyCreated() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(result.response());
    }

    @GetMapping("/by-date")
    public DailyOrdersResponse byDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return orderQueryService.byDate(date);
    }

    @GetMapping("/monthly")
    public List<DailyOrdersResponse> monthly(
            @RequestParam("year") @Min(2020) @Max(2100) int year,
            @RequestParam("month") @Min(1) @Max(12) int month) {
        return orderQueryService.monthly(year, month);
    }

    @GetMapping("/period/current")
    public PeriodOrdersResponse currentPeriod() {
        return orderQueryService.currentPeriod();
    }
}
