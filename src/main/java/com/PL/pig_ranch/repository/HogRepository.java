package com.PL.pig_ranch.repository;

import com.PL.pig_ranch.model.HogInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HogRepository extends JpaRepository<HogInventory, Long> {
}
