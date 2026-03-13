package com.PL.pig_ranch.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders") // "order" is often a reserved word in SQL
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Order {

    public enum OrderStatus {
        OPEN, PENDING, FULFILLED, CANCELLED
    }

    public enum OrderType {
        STANDARD, HOG
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.OPEN;

    @Enumerated(EnumType.STRING)
    private OrderType type = OrderType.STANDARD;

    private boolean paid = false;

    private boolean shipped = false;

    @Transient
    private boolean wasFulfilled = false;

    private String notes;
    private BigDecimal discount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Hog> hogs = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
    }

    public BigDecimal getTotalPrice() {
        BigDecimal total = BigDecimal.ZERO;
        if (orderItems != null) {
            for (OrderItem item : orderItems) {
                if (item.getPriceAtTimeOfOrder() != null && item.getQuantity() != null) {
                    BigDecimal lineTotal = item.getPriceAtTimeOfOrder()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    BigDecimal itemDiscount = item.getDiscount() != null ? item.getDiscount() : BigDecimal.ZERO;
                    total = total.add(lineTotal).subtract(itemDiscount);
                }
            }
        }
        // Hogs might have processing costs or a flat price, adding them here if
        // applicable
        if (hogs != null) {
            for (Hog hog : hogs) {
                if (hog.getProcessingCost() != null) {
                    total = total.add(hog.getProcessingCost());
                }
            }
        }

        BigDecimal globalDiscount = this.discount != null ? this.discount : BigDecimal.ZERO;
        return total.subtract(globalDiscount);
    }

    public void evaluateStatus() {
        if (paid && shipped) {
            this.status = OrderStatus.FULFILLED;
        } else if (paid || shipped) {
            this.status = OrderStatus.PENDING;
        } else {
            this.status = OrderStatus.OPEN;
        }
    }

    @PostLoad
    private void onLoad() {
        this.wasFulfilled = (status == OrderStatus.FULFILLED);
    }

    public boolean isWasFulfilled() {
        return wasFulfilled;
    }

    public void setWasFulfilled(boolean wasFulfilled) {
        this.wasFulfilled = wasFulfilled;
    }
}
