package com.jieluote.spilib;

import com.google.auto.service.AutoService;

@AutoService(IWebLanguage.class)
public class JavaScript implements IWebLanguage {
    @Override
    public String name() {
        return "JavaScript";
    }
}
