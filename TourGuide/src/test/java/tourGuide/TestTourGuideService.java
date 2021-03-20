package tourGuide;

import org.junit.Test;
import tourGuide.client.GpsUtilClient;
import tourGuide.client.RewardCentralClient;
import tourGuide.client.TripPricerClient;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.*;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

public class TestTourGuideService {

    @Test
    public void getUserLocation() {
        GpsUtilClient gpsUtil = new GpsUtilClient("localhost");
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, new TripPricerClient("localhost"), new RewardCentralClient("localhost"));

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocationDTO visitedLocation = tourGuideService.trackUserLocation(user);
        tourGuideService.tracker.stopTracking();
        assertTrue(visitedLocation.userId.equals(user.getUserId()));
    }

    @Test
    public void addUser() {
        GpsUtilClient gpsUtil = new GpsUtilClient("localhost");

        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, new TripPricerClient("localhost"), new RewardCentralClient("localhost"));

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);

        User retrivedUser = tourGuideService.getUser(user.getUserName());
        User retrivedUser2 = tourGuideService.getUser(user2.getUserName());

        tourGuideService.tracker.stopTracking();

        assertEquals(user, retrivedUser);
        assertEquals(user2, retrivedUser2);
    }

    @Test
    public void getAllUsers() {
        GpsUtilClient gpsUtil = new GpsUtilClient("localhost");

        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, new TripPricerClient("localhost"), new RewardCentralClient("localhost"));

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);

        List<User> allUsers = tourGuideService.getAllUsers();

        tourGuideService.tracker.stopTracking();

        assertTrue(allUsers.contains(user));
        assertTrue(allUsers.contains(user2));
    }

    @Test
    public void trackUser() {
        GpsUtilClient gpsUtil = new GpsUtilClient("localhost");

        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, new TripPricerClient("localhost"), new RewardCentralClient("localhost"));

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocationDTO visitedLocation = tourGuideService.trackUserLocation(user);

        tourGuideService.tracker.stopTracking();

        assertEquals(user.getUserId(), visitedLocation.userId);
    }

    //@Ignore // Not yet implemented
    @Test
    public void getNearbyAttractions() {
        GpsUtilClient gpsUtil = new GpsUtilClient("localhost");

        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, new TripPricerClient("localhost"), new RewardCentralClient("localhost"));

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocationDTO visitedLocation = tourGuideService.trackUserLocation(user);
        System.out.println(visitedLocation);

        List<AttractionDTO> attractions = tourGuideService.getNearByAttractions(visitedLocation);

        tourGuideService.tracker.stopTracking();

        assertEquals(5, attractions.size());
    }

    //Test ajouté en rapport avec la méthode qui permet de retourner un json contenant les 5 touristAttractionDTO
    @Test
    public void getNearbyTouristAttractionsDTO() {
        GpsUtilClient gpsUtil = new GpsUtilClient("localhost");

        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, new TripPricerClient("localhost"), new RewardCentralClient("localhost"));

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocationDTO visitedLocation = tourGuideService.trackUserLocation(user);
        System.out.println(visitedLocation);

        List<TouristsAttractionDTO> attractions = tourGuideService.getNearestFiveAttractions(visitedLocation);

        tourGuideService.tracker.stopTracking();

        assertEquals(5, attractions.size());
    }
   /* pour tester la methode qui permet de retourner un map contenant les utilisateurs ainsi que leurs
    locations actuelles, on crée des utilisateurs avec des locations données (càd longitude et latitude données),
    on vérifie qu' en total qu'on a 3 Maps donc 3 usersid ayant 3 locations
    puis on teste pour un id donnée on aura location's latitude et location's longitude donnée*/
    @Test
    public void getUserIdAndLocation() {
        GpsUtilClient gpsUtil = new GpsUtilClient("localhost");

        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, new TripPricerClient("localhost"), new RewardCentralClient("localhost"));

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        user.getVisitedLocations().add(new VisitedLocationDTO(user.getUserId(), new LocationDTO(5.00, 6), new Date()));
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon@tourGuide2.com");
        user2.getVisitedLocations().add(new VisitedLocationDTO(user2.getUserId(), new LocationDTO(15, 20), new Date()));
        User user3 = new User(UUID.randomUUID(), "jon3", "000", "jon@tourGuide3.com");
        user3.getVisitedLocations().add(new VisitedLocationDTO(user3.getUserId(), new LocationDTO(10, 7), new Date()));

        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);
        tourGuideService.addUser(user3);

        Map<UUID, LocationDTO> dictionary   =  tourGuideService.getUsersIdAndItsLocations();

        assertEquals(3,dictionary.size());

        assertEquals(6, dictionary.get(user.getUserId()).longitude, 0);
        assertEquals(5, dictionary.get(user.getUserId()).latitude, 0);

        assertEquals(20, dictionary.get(user2.getUserId()).longitude, 0);
        assertEquals(15, dictionary.get(user2.getUserId()).latitude, 0);

        assertEquals(7, dictionary.get(user3.getUserId()).longitude, 0);
        assertEquals(10, dictionary.get(user3.getUserId()).latitude, 0);

    }

    /*Test en rapport avec tripPricer pour vérifier qu'il tient en compte le nombre d 'enfants ainsi que la durée de séjour
    On vérife que pour deux clients ayant la même durée de séjour mais pas le même nombre d'enfants
    n'auront pas le même prix pour la même offre*/
      @Test
    public void getTripDealsRelatedOnNumberOfChildren() {
        GpsUtilClient gpsUtil = new GpsUtilClient("localhost");

        InternalTestHelper.setInternalUserNumber(0);
          TourGuideService tourGuideService = new TourGuideService(gpsUtil, new TripPricerClient("localhost"), new RewardCentralClient("localhost"));

        User jon= new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        jon.getUserPreferences().setNumberOfAdults(1);
        jon.getUserPreferences().setNumberOfChildren(5);
        jon.getUserPreferences().setTripDuration(2);
        List<ProviderDTO> providersJon = tourGuideService.getTripDeals(jon);

        tourGuideService.tracker.stopTracking();

        assertEquals(5, providersJon.size());

        User james = new User(UUID.randomUUID(), "james", "999", "james@tourGuide.com");
        james.getUserPreferences().setNumberOfChildren(0);
        james.getUserPreferences().setNumberOfAdults(1);
        jon.getUserPreferences().setTripDuration(2);
        List<ProviderDTO> providersJames = tourGuideService.getTripDeals(james);

        assertEquals(5, providersJames.size());

          for(ProviderDTO providerJon : providersJon){
              for(ProviderDTO providerJames: providersJames){
                  if(providerJames.name.equals(providerJon.name)){
                      assertNotEquals(providerJames.price, providerJon.price, 0);
                  }
              }
          }

          tourGuideService.tracker.stopTracking();
      }
    /*On vérifie que pour deux clients n'ayant pas la même durée de séjour mais ayant même nombre d'enfants
   n'auront pas le même prix pour la même offre*/
    @Test
    public void getTripDealsRelatedOnTripDuration() {
        GpsUtilClient gpsUtil = new GpsUtilClient("localhost");

        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, new TripPricerClient("localhost"), new RewardCentralClient("localhost"));

        User jon= new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        jon.getUserPreferences().setTripDuration(1);
        List<ProviderDTO> providersJon = tourGuideService.getTripDeals(jon);

        tourGuideService.tracker.stopTracking();

        assertEquals(5, providersJon.size());

        User james = new User(UUID.randomUUID(), "james", "999", "james@tourGuide.com");
        james.getUserPreferences().setTripDuration(10);

        List<ProviderDTO> providersJames = tourGuideService.getTripDeals(james);

        assertEquals(5, providersJames.size());

        for(ProviderDTO providerJon : providersJon){
            for(ProviderDTO providerJames: providersJames){
                if(providerJames.name.equals(providerJon.name)){
                    assertNotEquals(providerJames.price, providerJon.price, 0);
                }
            }
        }

        tourGuideService.tracker.stopTracking();
    }
}
