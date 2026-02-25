package com.PL.pig_ranch.service.impl;

import com.PL.pig_ranch.model.HogInventory;
import com.PL.pig_ranch.repository.HogRepository;
import com.PL.pig_ranch.service.HogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class HogServiceImpl implements HogService {

    private final HogRepository hogRepository;

    @Autowired
    public HogServiceImpl(HogRepository hogRepository) {
        this.hogRepository = hogRepository;
    }

    @Override
    public List<HogInventory> getAllHogs() {
        return hogRepository.findAll();
    }

    @Override
    public Optional<HogInventory> getHogById(Long id) {
        return hogRepository.findById(id);
    }

    @Override
    @Transactional
    public HogInventory saveHog(HogInventory hog) {
        return hogRepository.save(hog);
    }

    @Override
    @Transactional
    public void deleteHog(Long id) {
        hogRepository.deleteById(id);
    }
}
