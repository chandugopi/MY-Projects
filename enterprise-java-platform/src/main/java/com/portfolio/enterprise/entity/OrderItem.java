package com.portfolio.enterprise.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * OrderItem entity representing individual items in an order.
 * Demonstrates many-to-one relationship with Order.
 */
@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, length = 100)
    private String productName;

    @Column(name = "product_code", length = 50)
    private String productCode;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    /**
     * Calculates the subtotal for this item.
     * Subtotal = (unitPrice * quantity) - discount
     */
    public BigDecimal getSubtotal() {
        BigDecimal gross = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return gross.subtract(discount != null ? discount : BigDecimal.ZERO);
    }
}
