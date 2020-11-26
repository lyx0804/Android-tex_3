package com.example.weather;

public class city {
    private String cityname;

    public city(String cityname) {
        this.cityname = cityname;
    }

    public city(){}

    public String getCityname() {
        return cityname;
    }

    public void setCityname(String cityname) {
        this.cityname = cityname;
    }
}
