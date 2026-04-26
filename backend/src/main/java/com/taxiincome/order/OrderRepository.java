package com.taxiincome.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByUserIdAndOrderDateOrderByOrderTimeAsc(UUID userId, LocalDate orderDate);

    List<Order> findByUserIdAndOrderDateBetweenOrderByOrderDateAscOrderTimeAsc(
            UUID userId, LocalDate start, LocalDate endInclusive);

    @Query("""
            SELECT new com.taxiincome.order.OrderAggregate(
                COUNT(o),
                COALESCE(SUM(o.orderAmount), 0),
                COALESCE(SUM(o.feeAmount), 0),
                COALESCE(SUM(o.tipAmount), 0),
                COALESCE(SUM(o.subtotal), 0),
                COALESCE(SUM(o.netAmount), 0),
                COUNT(DISTINCT o.orderDate))
            FROM Order o
            WHERE o.userId = :userId
              AND o.orderDate BETWEEN :start AND :endInclusive
            """)
    OrderAggregate aggregate(@Param("userId") UUID userId,
                             @Param("start") LocalDate start,
                             @Param("endInclusive") LocalDate endInclusive);
}
