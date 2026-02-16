package com.PL.pig_ranch.service.impl;

import com.PL.pig_ranch.model.Household;
import com.PL.pig_ranch.repository.HouseholdRepository;
import com.PL.pig_ranch.service.HouseholdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HouseholdServiceImpl implements HouseholdService {

    private final HouseholdRepository householdRepository;

    @Autowired
    public HouseholdServiceImpl(HouseholdRepository householdRepository) {
        this.householdRepository = householdRepository;
    }

    @Override
    public Household saveHousehold(Household household) {
        return householdRepository.save(household);
    }

    @Override
    public Optional<Household> getHouseholdById(Long id) {
        return householdRepository.findById(id);
    }

    @Override
    public List<Household> getAllHouseholds() {
        return householdRepository.findAll();
    }

    @Override
    public void deleteHousehold(Long id) {
        householdRepository.deleteById(id);
    }
}
