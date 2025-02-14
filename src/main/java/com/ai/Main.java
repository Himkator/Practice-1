package com.ai;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context=new AnnotationConfigApplicationContext(ProjConfig.class);

        Car car=context.getBean("createMercedes", Car.class);
        System.out.println(car.getInfo());
        Car trash=context.getBean("createBMW", Car.class);
        System.out.println(trash.getInfo());

        Driver driver=context.getBean(Driver.class);
        driver.setName("Nurym");
        driver.setAge(19);
        driver.setCar(car);
        System.out.println(driver.getInfo());

        Driver trashDriver=context.getBean(Driver.class);
        driver.setName("Dias");
        driver.setAge(18);
        driver.setCar(trash);
        System.out.println(trashDriver.getInfo());
    }
}