package com.ai;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context=new AnnotationConfigApplicationContext(ProjConfig.class);

        Car car=context.getBean("createMercedes", Car.class);
        System.out.println(car.getInfo());
        Car trash=context.getBean("createBMW", Car.class);
        System.out.println(trash.getInfo());
    }
}