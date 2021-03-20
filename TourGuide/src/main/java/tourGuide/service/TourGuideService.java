package tourGuide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Value;
import tourGuide.client.GpsUtilClient;
import tourGuide.client.RewardCentralClient;
import tourGuide.client.TripPricerClient;
import tourGuide.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import tourGuide.helper.InternalTestHelper;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserReward;

@Service
public class TourGuideService {
    private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
    private final GpsUtilClient gpsUtilClient; ////
    private final RewardsService rewardsService;
    private final TripPricerClient tripPricerClient;//00//
    public final Tracker tracker;
    private RewardCentralClient rewardCentral; // ajouté pour ajouté reward point dans le tourist attraction DTO
    boolean testMode = true;

    public TourGuideService(GpsUtilClient gpsUtilClient, TripPricerClient tripPricerClient, RewardCentralClient rewardCentralClient) {
        this.gpsUtilClient = gpsUtilClient;
        this.rewardsService = new RewardsService(gpsUtilClient, rewardCentralClient);
        this.tripPricerClient = tripPricerClient;
        this.rewardCentral = rewardCentralClient;//ajouté pour ajouté reward point dans le tourist attraction DTO

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

    public VisitedLocationDTO getUserLocation(User user) {
        VisitedLocationDTO visitedLocation = (user.getVisitedLocations().size() > 0) ?
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
    public List<ProviderDTO> getTripDeals(User user) {
        int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
        List<ProviderDTO> providers = tripPricerClient.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(),///
                user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
        user.setTripDeals(providers);
        return providers;
    }

    public VisitedLocationDTO trackUserLocation(User user) {
        VisitedLocationDTO visitedLocation = gpsUtilClient.getUserLocation(user.getUserId());
        user.addToVisitedLocations(visitedLocation);
        rewardsService.calculateRewards(user);
        return visitedLocation;
    }

    //5 attractions les plus proches par rapport au dernier emplacement de l'utilisateur **peu importe leur distance**.
    public List<AttractionDTO> getNearByAttractions(VisitedLocationDTO visitedLocation) {
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
        //la valeur :attraction  de type TreeMap pour avoir un dictionnaire trié
        //puis on va ajouter les 5 premiers valeurs de map dans une list nearByAttraction puid on fait un break.

        List<AttractionDTO> nearbyAttractions = new ArrayList<>();
        Map<Double, AttractionDTO> dictionary = new TreeMap<>();

        for (AttractionDTO attraction : gpsUtilClient.getAttractions()) {
            double distance = rewardsService.getDistance(attraction, visitedLocation.location);
            dictionary.put(distance, attraction);
        }

        for (Map.Entry<Double, AttractionDTO> entry : dictionary.entrySet()) {
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

    public List<TouristsAttractionDTO> getNearestFiveAttractions(VisitedLocationDTO visitedLocation) {
        List<AttractionDTO> attractions = getNearByAttractions(visitedLocation);

        List<TouristsAttractionDTO> result = new ArrayList<>();

        for (AttractionDTO attraction : attractions) {
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

    ////méthode qui permet de voir les emplacements des users: on aura l'id et la location(latitude et longitude)
    //on crée un map ayant id comme clé et la location comme valeur. On va louper tous les users pour extraire les id et les location
    public Map<UUID, LocationDTO> getUsersIdAndItsLocations() {

        Map<UUID, LocationDTO> dictionary = new TreeMap<>();
        List<User> allUsers = getAllUsers();

        for (User userx : allUsers) {
            UUID userId = userx.getLastVisitedLocation().userId;
            LocationDTO location = userx.getLastVisitedLocation().location;
            dictionary.put(userId, location);
        }
        return dictionary;
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
            user.addToVisitedLocations(new VisitedLocationDTO(user.getUserId(), new LocationDTO(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
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
