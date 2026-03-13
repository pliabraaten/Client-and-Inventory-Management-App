package com.PL.pig_ranch.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Hog {

    public enum HogType {
        WHOLE, HALF
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String hogNumber;

    @Enumerated(EnumType.STRING)
    private HogType hogType;

    private Boolean inspected = false;

    private String processor;

    private BigDecimal liveWeight;

    private BigDecimal hangingWeight;

    private BigDecimal processingCost;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    /**
     * Computed field — not stored in DB.
     * Returns hanging weight as a percentage of live weight.
     */
    @Transient
    public BigDecimal getPercentHanging() {
        if (liveWeight == null || liveWeight.compareTo(BigDecimal.ZERO) == 0 || hangingWeight == null) {
            return null;
        }
        return hangingWeight.divide(liveWeight, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
