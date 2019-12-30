package com.imple.spi.service.impl;

import com.imple.spi.service.SPIService;

public class SpiImpl1 implements SPIService {
    @Override
    public void execute() {
        System.out.println("SpiImpl1.execute()");
    }
}
