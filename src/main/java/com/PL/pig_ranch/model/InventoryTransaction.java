package com.PL.pig_ranch.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransaction {

    public enum TransactionType {
        ADJUSTMENT, ARRIVAL, ORDER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private InventoryItem item;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private Integer changeAmount;

    private LocalDateTime timestamp;

    private String notes;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
