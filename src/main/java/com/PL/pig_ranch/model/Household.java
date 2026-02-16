package com.PL.pig_ranch.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Household {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String surname;
    private String address;
    private String city;
    private String state;
    private String zipCode;

    @ToString.Exclude
    @OneToMany(mappedBy = "household", cascade = CascadeType.ALL)
    private List<Client> members = new ArrayList<>();

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "referred_by_id")
    private Client referredBy;
}
