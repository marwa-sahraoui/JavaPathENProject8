package tourGuide.model;

import java.util.UUID;

public class ProviderDTO {
    public final UUID tripId;
    public final String name;
    public final double price;

    public ProviderDTO() {
        this(null, 0, null);
    }

    public ProviderDTO(UUID tripId, double price, String name) {
        this.tripId = tripId;
        this.name = name;
        this.price = price;
    }
}
