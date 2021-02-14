package model;

public class TouristsAttractionDTO {
    private String attractionName;
    private double attractionLong;
    private double attractionLat;
    private double userLocationLong;
    private double userLocationLat;
    private double distanceBetweenLocationAndAttraction;
    private int rewardPoint;

    public TouristsAttractionDTO() {
        //
    }


    public TouristsAttractionDTO(String attractionName, double attractionLong, double attractionLat, double userLocationLong, double userLocationLat,
                                 double distanceBetweenLocationAndAttraction, int rewardPoint) {
        this.attractionName = attractionName;
        this.attractionLong = attractionLong;
        this.attractionLat = attractionLat;
        this.userLocationLong = userLocationLong;
        this.userLocationLat = userLocationLat;
        this.distanceBetweenLocationAndAttraction = distanceBetweenLocationAndAttraction;
        this.rewardPoint = rewardPoint;
    }

    public String getAttractionName() {
        return attractionName;
    }

    public void setAttractionName(String attractionName) {
        this.attractionName = attractionName;
    }

    public double getAttractionLong() {
        return attractionLong;
    }

    public void setAttractionLong(double attractionLong) {
        this.attractionLong = attractionLong;
    }

    public double getAttractionLat() {
        return attractionLat;
    }

    public void setAttractionLat(double attractionLat) {
        this.attractionLat = attractionLat;
    }

    public double getUserLocationLong() {
        return userLocationLong;
    }

    public void setUserLocationLong(double userLocationLong) {
        this.userLocationLong = userLocationLong;
    }

    public double getUserLocationLat() {
        return userLocationLat;
    }

    public void setUserLocationLat(double userLocationLat) {
        this.userLocationLat = userLocationLat;
    }

    public double getDistanceBetweenLocationAndAttraction() {
        return distanceBetweenLocationAndAttraction;
    }

    public void setDistanceBetweenLocationAndAttraction(double distanceBetweenLocationAndAttraction) {
        this.distanceBetweenLocationAndAttraction = distanceBetweenLocationAndAttraction;
    }

    public int getRewardPoint() {
        return rewardPoint;
    }

    public void setRewardPoint(int rewardPoint) {
        this.rewardPoint = rewardPoint;
    }
}
