package com.ai;


public class Car {
    private String carName;
    private String carModel;
    private double carVolume;

    public String getInfo(){
        return carModel+" "+carName+"'s engine volume is "+carVolume;
    }

    public Car(String carName, String carModel, double carVolume) {
        this.carName = carName;
        this.carModel = carModel;
        this.carVolume = carVolume;
    }

    public Car() {
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public String getCarModel() {
        return carModel;
    }

    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    public double getCarVolume() {
        return carVolume;
    }

    public void setCarVolume(double carVolume) {
        this.carVolume = carVolume;
    }
}
