package com.taxiincome.order;

import com.taxiincome.order.dto.CreateOrderRequest;
import com.taxiincome.order.dto.DailyOrdersResponse;
import com.taxiincome.order.dto.OrderResponse;
import com.taxiincome.order.dto.PeriodOrdersResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest req) {
        return orderService.create(req);
    }

    @GetMapping("/by-date")
    public DailyOrdersResponse byDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return orderService.byDate(date);
    }

    @GetMapping("/monthly")
    public List<DailyOrdersResponse> monthly(
            @RequestParam("year") int year,
            @RequestParam("month") int month) {
        return orderService.monthly(year, month);
    }

    @GetMapping("/period/current")
    public PeriodOrdersResponse currentPeriod() {
        return orderService.currentPeriod();
    }
}
