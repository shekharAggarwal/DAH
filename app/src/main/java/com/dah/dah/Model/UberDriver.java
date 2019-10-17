package com.dah.dah.Model;

public class UberDriver {
    private String Phone,Name,avatarUrl,rates,carType;
      public UberDriver(){

    }

    public UberDriver(String phone, String name, String avatarUrl, String rates, String carType) {
        Phone = phone;
        Name = name;
        this.avatarUrl = avatarUrl;
        this.rates = rates;
        this.carType = carType;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getRates() {
        return rates;
    }

    public void setRates(String rates) {
        this.rates = rates;
    }

    public String getCarType() {
        return carType;
    }

    public void setCarType(String carType) {
        this.carType = carType;
    }
}
