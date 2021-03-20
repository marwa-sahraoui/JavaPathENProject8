package tourGuide.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;

import tourGuide.client.GpsUtilClient;
import tourGuide.client.RewardCentralClient;
import tourGuide.model.AttractionDTO;
import tourGuide.model.LocationDTO;
import tourGuide.model.VisitedLocationDTO;
import tourGuide.user.User;
import tourGuide.user.UserReward;

@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    // proximity in miles
    private int defaultProximityBuffer = 10;
    private int proximityBuffer = defaultProximityBuffer;
    private int attractionProximityRange = 200;
    private final GpsUtilClient gpsUtilClient;
    private final RewardCentralClient rewardsCentralClient;

    public RewardsService(GpsUtilClient gpsUtilClient, RewardCentralClient rewardCentralClient) {
        this.gpsUtilClient = gpsUtilClient;
        this.rewardsCentralClient = rewardCentralClient;
    }

    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
    }

    public void setDefaultProximityBuffer() {
        proximityBuffer = defaultProximityBuffer;
    }

    public void calculateRewards(User user) {
		/* userLocations sera de type CopyOnWriteArrayList ce qui permet un thread safe ce qui permet d'avoir une fraiche copie de data
		même avec des modifications instantannées comme ça on evite les concurentModifications excep*/

        CopyOnWriteArrayList<VisitedLocationDTO> userLocations = new CopyOnWriteArrayList<>();
        userLocations.addAll(user.getVisitedLocations());

        List<AttractionDTO> attractions = gpsUtilClient.getAttractions();

        for (VisitedLocationDTO visitedLocation : userLocations) {
            for (AttractionDTO attraction : attractions) {
                if (user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
                    if (nearAttraction(visitedLocation, attraction)) {
                        user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
                    }
                }
            }
        }

    }
    //5 attractions les plus proches par rapport au dernier emplacement de l'utilisateur **peu importe leur distance**.

    public boolean isWithinAttractionProximity(AttractionDTO attraction, LocationDTO location) {
        return getDistance(attraction, location) > attractionProximityRange ? false : true;
        //distance>200 donc ce n'est pas à proximity
    }

    private boolean nearAttraction(VisitedLocationDTO visitedLocation, AttractionDTO attraction) {

        return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
        //distance location > 10 par rapport à l attraction donc on n'est pas prés
    }

    private int getRewardPoints(AttractionDTO attraction, User user) {
        return rewardsCentralClient.getAttractionRewardPoints(attraction.attractionId, user.getUserId());//a changer
    }

    public double getDistance(LocationDTO loc1, LocationDTO loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
    }

}
