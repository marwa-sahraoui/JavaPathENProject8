package tourguide.gpsutil;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/gpsutil")
public class GpsUtilController {

    @Autowired
    GpsUtil gpsUtil;

    @GetMapping("/attractions")
    public List<Attraction> getAttractions() {
        return gpsUtil.getAttractions();
    }

    @GetMapping("/userlocation/{userId}")
    public VisitedLocation getUserLocation(@PathVariable UUID userId) {
        return gpsUtil.getUserLocation(userId);
    }
}
