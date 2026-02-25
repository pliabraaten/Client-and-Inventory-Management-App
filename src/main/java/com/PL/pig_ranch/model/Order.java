package com.PL.pig_ranch.model;

import jakarta.persistence.*;
import lombok.*;
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
        OPEN, COMPLETED, CANCELLED
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

    private String notes;

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

    public Double getTotalPrice() {
        Double total = 0.0;
        if (orderItems != null) {
            for (OrderItem item : orderItems) {
                if (item.getPriceAtTimeOfOrder() != null && item.getQuantity() != null) {
                    total += item.getPriceAtTimeOfOrder() * item.getQuantity();
                }
            }
        }
        // Hogs might have processing costs or a flat price, adding them here if
        // applicable
        if (hogs != null) {
            for (Hog hog : hogs) {
                if (hog.getProcessingCost() != null) {
                    total += hog.getProcessingCost();
                }
            }
        }
        return total;
    }
}
