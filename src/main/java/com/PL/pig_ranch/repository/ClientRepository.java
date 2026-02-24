package com.PL.pig_ranch.repository;

import com.PL.pig_ranch.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    boolean existsByNameIgnoreCaseAndPhoneNumber(String name, String phoneNumber);

    long countByHouseholdId(Long householdId);

    @Query(value = "SELECT household_id FROM client WHERE id = :clientId", nativeQuery = true)
    Long findHouseholdIdByClientId(@Param("clientId") Long clientId);
}
