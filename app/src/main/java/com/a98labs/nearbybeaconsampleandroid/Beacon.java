package com.a98labs.nearbybeaconsampleandroid;

import android.util.Base64;

import com.google.gson.Gson;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.lang.Double.NaN;

public class Beacon {
    private final String advertisedId;
    private final String advertisedType;
    private final String placeName;
    private final double latitude;
    private final double longitude;
    private final String description;

    private double distance = NaN;

    public static Beacon fromDeviceInfoAttachmentType(byte[] content) {
        String jsonContent = new String(Base64.decode(content, Base64.DEFAULT));
        return new Gson().fromJson(jsonContent, Beacon.class);
    }

    private Beacon(String advertisedId,
                   String advertisedType,
                   String placeName,
                   double latitude,
                   double longitude,
                   String description) {
        this.advertisedId = advertisedId;
        this.advertisedType = advertisedType;
        this.placeName = placeName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
    }

    public String getAdvertisedId() {
        return this.advertisedId;
    }

    public String getAdvertisedType() {
        return this.advertisedType;
    }

    public String getPlaceName() {
        return this.placeName;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDistance() {
        BigDecimal bd = new BigDecimal(this.distance);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Beacon)) {
            return false;
        }

        Beacon that = (Beacon) obj;
        if (!that.getAdvertisedId().equals(this.advertisedId)) {
            return false;
        }

        return true;
    }
}
