package com.imple.spi.service;

import sun.misc.Service;

import java.util.Iterator;
import java.util.ServiceLoader;

public class TestSPI {

    public static void main(String[] args) {
        //Service.providers包位于sun.misc.Service
        Iterator<SPIService> providers = Service.providers(SPIService.class);
        //ServiceLoader.load包位于java.util.ServiceLoader
        ServiceLoader<SPIService> load = ServiceLoader.load(SPIService.class);

        while(providers.hasNext()){
            SPIService ser = providers.next();
            ser.execute();
        }
        System.out.println("---------------");
        Iterator<SPIService> iterator = load.iterator();
        while(iterator.hasNext()){
            SPIService ser = iterator.next();
            ser.execute();
        }
    }
}
