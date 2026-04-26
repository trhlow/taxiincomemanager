package com.taxiincome.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "order_amount", nullable = false)
    private long orderAmount;

    @Column(name = "fee_rate", nullable = false, precision = 4, scale = 3)
    private BigDecimal feeRate;

    @Column(name = "fee_amount", nullable = false)
    private long feeAmount;

    @Column(name = "tip_amount", nullable = false)
    private long tipAmount;

    @Column(name = "taxi_count", nullable = false)
    private short taxiCount;

    @Column(name = "subtotal", nullable = false)
    private long subtotal;

    @Column(name = "net_amount", nullable = false)
    private long netAmount;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "order_time", nullable = false)
    private LocalTime orderTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 16)
    private OrderSourceType sourceType;

    @Column(name = "note")
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public long getOrderAmount() { return orderAmount; }
    public void setOrderAmount(long orderAmount) { this.orderAmount = orderAmount; }

    public BigDecimal getFeeRate() { return feeRate; }
    public void setFeeRate(BigDecimal feeRate) { this.feeRate = feeRate; }

    public long getFeeAmount() { return feeAmount; }
    public void setFeeAmount(long feeAmount) { this.feeAmount = feeAmount; }

    public long getTipAmount() { return tipAmount; }
    public void setTipAmount(long tipAmount) { this.tipAmount = tipAmount; }

    public short getTaxiCount() { return taxiCount; }
    public void setTaxiCount(short taxiCount) { this.taxiCount = taxiCount; }

    public long getSubtotal() { return subtotal; }
    public void setSubtotal(long subtotal) { this.subtotal = subtotal; }

    public long getNetAmount() { return netAmount; }
    public void setNetAmount(long netAmount) { this.netAmount = netAmount; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

    public LocalTime getOrderTime() { return orderTime; }
    public void setOrderTime(LocalTime orderTime) { this.orderTime = orderTime; }

    public OrderSourceType getSourceType() { return sourceType; }
    public void setSourceType(OrderSourceType sourceType) { this.sourceType = sourceType; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
