package com.PL.pig_ranch.repository;

import com.PL.pig_ranch.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    boolean existsByNameIgnoreCaseAndPhoneNumber(String name, String phoneNumber);
}
