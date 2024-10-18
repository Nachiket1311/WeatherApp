package com.example.myapplication;

import com.google.gson.annotations.SerializedName;

public class WeatherResponse {
    @SerializedName("wind_u-surface")
    private float windSpeedU; // East-West component of wind

    @SerializedName("wind_v-surface")
    private float windSpeedV; // North-South component of wind

    @SerializedName("temp-surface")
    private float temperature; // Temperature at surface level

    @SerializedName("past3hprecip-surface")
    private float precipitation; // Precipitation over the past 3 hours

    @SerializedName("lclouds-surface")
    private float lowCloudCover; // Low cloud cover at surface level

    @SerializedName("mclouds-surface")
    private float mediumCloudCover; // Medium cloud cover at surface level

    @SerializedName("hclouds-surface")
    private float highCloudCover; // High cloud cover at surface level

    // Getters
    public float getWindSpeedU() {
        return windSpeedU;
    }

    public float getWindSpeedV() {
        return windSpeedV;
    }

    public float getWindSpeedMagnitude() {
        return (float) Math.sqrt(windSpeedU * windSpeedU + windSpeedV * windSpeedV); // Calculate wind speed magnitude
    }

    public float getTemperature() {
        return temperature;
    }

    public float getPrecipitation() {
        return precipitation;
    }

    public float getLowCloudCover() {
        return lowCloudCover;
    }

    public float getMediumCloudCover() {
        return mediumCloudCover;
    }

    public float getHighCloudCover() {
        return highCloudCover;
    }


}
