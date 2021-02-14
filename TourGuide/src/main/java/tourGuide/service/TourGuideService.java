package tourGuide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import model.TouristsAttractionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
    private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
    private final GpsUtil gpsUtil;
    private final RewardsService rewardsService;
    private final TripPricer tripPricer = new TripPricer();
    public final Tracker tracker;
    private RewardCentral rewardCentral; // ajouté pour ajouté reward point dans le tourist attraction DTO
    boolean testMode = true;

    public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
        this.gpsUtil = gpsUtil;
        this.rewardsService = rewardsService;
        this.rewardCentral = new RewardCentral();//ajouté pour ajouté reward point dans le tourist attraction DTO

        if (testMode) {
            logger.info("TestMode enabled");
            logger.debug("Initializing users");
            initializeInternalUsers();
            logger.debug("Finished initializing users");
        }
        tracker = new Tracker(this);
        addShutDownHook();
    }

    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }

    public VisitedLocation getUserLocation(User user) {
        VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ?
                user.getLastVisitedLocation() :
                trackUserLocation(user);
        return visitedLocation;
    }

    public User getUser(String userName) {
        return internalUserMap.get(userName);
    }

    public List<User> getAllUsers() {
        return internalUserMap.values().stream().collect(Collectors.toList());
    }

    public void addUser(User user) {
        if (!internalUserMap.containsKey(user.getUserName())) {
            internalUserMap.put(user.getUserName(), user);
        }
    }

    ///
    public List<Provider> getTripDeals(User user) {
        int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
        List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(),
                user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
        user.setTripDeals(providers);
        return providers;
    }

    public VisitedLocation trackUserLocation(User user) {
        VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
        user.addToVisitedLocations(visitedLocation);
        rewardsService.calculateRewards(user);
        return visitedLocation;
    }

    //5 attractions les plus proches par rapport au dernier emplacement de l'utilisateur **peu importe leur distance**.
    public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
//		List<Attraction> nearbyAttractions = new ArrayList<>();
//
//		for(Attraction attraction : gpsUtil.getAttractions()) {
//			if(rewardsService.isWithinAttractionProximity(attraction, visitedLocation.location)) {
//				nearbyAttractions.add(attraction);
//			}
//		}
//		return nearbyAttractions;

        // en rapport avec le testTourGuideService :getNearbyAttractions()	en effet c'est inutil d'appeler la methode isWithAttractionProximity vue
        // que la distance max prise en considération est 200, nous on cherche les 5 attractions les plus proches de localisation peu importe leur distance
        // on va trier ces attraction selon leur distance par rapport à la localisation de l'utilisateur , on utilise le dictionnaire dont la clé :distance et
        //la valeur :attraction  de type TreeMap pour avoir un dictionnaire trié ;
        //puis on va ajouter les 5 premiers valeurs de map dans une list nearByAttraction puid on fait un break.

        List<Attraction> nearbyAttractions = new ArrayList<>();
        Map<Double, Attraction> dictionary = new TreeMap<>();

        for (Attraction attraction : gpsUtil.getAttractions()) {
            double distance = rewardsService.getDistance(attraction, visitedLocation.location);
            dictionary.put(distance, attraction);
        }

        for (Map.Entry<Double, Attraction> entry : dictionary.entrySet()) {
            nearbyAttractions.add(entry.getValue());
            if (nearbyAttractions.size() == 5) {
                break;
            }
        }

        return nearbyAttractions;
    }
     /*méthode ajoutée pour répondre au TourGuideController getattraction pour avoir une objet json ayant les 5
     TouristAttractionDTO constuit ayant comme attribut :attractionName/attractionLAT/attractionLONG/
    locationLAt/locationLONG/distanceBetweenlocationAndAttraction/rewardPoint*/

    public List<TouristsAttractionDTO> getNearestFiveAttractions(VisitedLocation visitedLocation) {
        List<Attraction> attractions = getNearByAttractions(visitedLocation);

        List<TouristsAttractionDTO> result = new ArrayList<>();

        for (Attraction attraction : attractions) {
            TouristsAttractionDTO touristsAttractionDTO = new TouristsAttractionDTO();
            touristsAttractionDTO.setAttractionName(attraction.attractionName);
            touristsAttractionDTO.setAttractionLat(attraction.latitude);
            touristsAttractionDTO.setAttractionLong(attraction.longitude);
            touristsAttractionDTO.setUserLocationLat(visitedLocation.location.latitude);
            touristsAttractionDTO.setUserLocationLong(visitedLocation.location.longitude);
            touristsAttractionDTO.setDistanceBetweenLocationAndAttraction(rewardsService.getDistance(attraction, visitedLocation.location));
            touristsAttractionDTO.setRewardPoint(rewardCentral.getAttractionRewardPoints(attraction.attractionId, visitedLocation.userId));

            result.add(touristsAttractionDTO);
        }

        return result;
    }


    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                tracker.stopTracking();
            }
        });
    }

    /**********************************************************************************
     *
     * Methods Below: For Internal Testing
     *
     **********************************************************************************/
    private static final String tripPricerApiKey = "test-server-api-key";
    // Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
    private final Map<String, User> internalUserMap = new HashMap<>();

    private void initializeInternalUsers() {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
            String userName = "internalUser" + i;
            String phone = "000";
            String email = userName + "@tourGuide.com";
            User user = new User(UUID.randomUUID(), userName, phone, email);
            generateUserLocationHistory(user);

            internalUserMap.put(userName, user);
        });
        logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
    }

    private void generateUserLocationHistory(User user) {
        IntStream.range(0, 3).forEach(i -> {
            user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
        });
    }

    private double generateRandomLongitude() {
        double leftLimit = -180;
        double rightLimit = 180;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private double generateRandomLatitude() {
        double leftLimit = -85.05112878;
        double rightLimit = 85.05112878;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private Date getRandomTime() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }

}
