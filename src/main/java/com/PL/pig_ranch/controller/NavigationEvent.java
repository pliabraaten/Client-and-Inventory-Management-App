package com.PL.pig_ranch.controller;

import org.springframework.context.ApplicationEvent;

public class NavigationEvent extends ApplicationEvent {
    private final String viewName;

    public NavigationEvent(Object source, String viewName) {
        super(source);
        this.viewName = viewName;
    }

    public String getViewName() {
        return viewName;
    }
}
