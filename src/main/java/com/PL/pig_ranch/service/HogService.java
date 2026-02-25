package com.PL.pig_ranch.service;

import com.PL.pig_ranch.model.Hog;
import java.util.List;
import java.util.Optional;

public interface HogService {

    List<Hog> getAllHogs();

    Optional<Hog> getHogById(Long id);

    Hog saveHog(Hog hog);

    void deleteHog(Long id);
}
