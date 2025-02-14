package com.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class Driver {
    private String name;
    private int age;
    @Autowired
    @Qualifier("createMercedes")
    private Car car;

    public String getInfo(){
        return
                name+" is "+age+" and is driving "+
                        (car.getCarModel().contains("BMW") ? "trash":car.getCarModel());
    }

    public Driver(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public Driver() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }
}
