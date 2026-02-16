package com.PL.pig_ranch.service;

import com.PL.pig_ranch.model.Household;
import java.util.List;
import java.util.Optional;

public interface HouseholdService {

    Household saveHousehold(Household household);

    Optional<Household> getHouseholdById(Long id);

    List<Household> getAllHouseholds();

    void deleteHousehold(Long id);
}
