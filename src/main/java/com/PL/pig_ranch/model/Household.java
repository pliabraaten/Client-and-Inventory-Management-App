package com.PL.pig_ranch.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Household {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String surname;
    private String address;
    private String city;
    private String state;
    private String zipCode;

    @ToString.Exclude
    @OneToMany(mappedBy = "household", cascade = { CascadeType.PERSIST, CascadeType.MERGE }) // Cascade saves but
                                                                                             // doesn't delete clients
    private List<Client> members = new ArrayList<>();

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "referred_by_id")
    private Client referredBy;
}
