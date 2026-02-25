package com.PL.pig_ranch.service;

import com.PL.pig_ranch.model.HogInventory;
import java.util.List;
import java.util.Optional;

public interface HogService {

    List<HogInventory> getAllHogs();

    Optional<HogInventory> getHogById(Long id);

    HogInventory saveHog(HogInventory hog);

    void deleteHog(Long id);
}
