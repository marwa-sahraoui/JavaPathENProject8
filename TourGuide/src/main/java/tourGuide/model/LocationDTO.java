package tourGuide.model;


public class LocationDTO {
    public final double longitude;
    public final double latitude;

    public LocationDTO(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LocationDTO() {
        this(0, 0);
    }
}
