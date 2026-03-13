package com.PL.pig_ranch.exception;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String entity, Long id) {
        super(entity + " with ID " + id + " not found");
    }
}
