package com.PL.pig_ranch.model;

import jakarta.persistence.*;
import lombok.*;

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

    private Double liveWeight;

    private Double hangingWeight;

    private Double processingCost;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    /**
     * Computed field — not stored in DB.
     * Returns hanging weight as a percentage of live weight.
     */
    @Transient
    public Double getPercentHanging() {
        if (liveWeight == null || liveWeight == 0 || hangingWeight == null) {
            return null;
        }
        return (hangingWeight / liveWeight) * 100.0;
    }
}
