package tourGuide;

import org.junit.Test;
import tourGuide.client.GpsUtilClient;
import tourGuide.client.RewardCentralClient;
import tourGuide.client.TripPricerClient;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.AttractionDTO;
import tourGuide.model.VisitedLocationDTO;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestRewardsService {

    @Test
    public void userGetRewards() {
        GpsUtilClient gpsUtilClient = new GpsUtilClient("localhost");

        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtilClient, new TripPricerClient("localhost"), new RewardCentralClient("localhost"));


        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        AttractionDTO attraction = gpsUtilClient.getAttractions().get(0);

        user.addToVisitedLocations(new VisitedLocationDTO(user.getUserId(), attraction, new Date()));
        tourGuideService.trackUserLocation(user);
        List<UserReward> userRewards = user.getUserRewards();
        tourGuideService.tracker.stopTracking();
        assertTrue(userRewards.size() == 1);
    }

    @Test
    public void isWithinAttractionProximity() {
        GpsUtilClient gpsUtilClient = new GpsUtilClient("localhost");
        RewardsService rewardsService = new RewardsService(gpsUtilClient, new RewardCentralClient("localhost"));
        AttractionDTO attraction = gpsUtilClient.getAttractions().get(0);
        assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
    }

    //	@Ignore // Needs fixed - can throw ConcurrentModificationException
    @Test
    public void nearAllAttractions() {
        GpsUtilClient gpsUtilClient = new GpsUtilClient("localhost");
        RewardsService rewardsService = new RewardsService(gpsUtilClient, new RewardCentralClient("localhost"));
        rewardsService.setProximityBuffer(Integer.MAX_VALUE);


        InternalTestHelper.setInternalUserNumber(1);
        TourGuideService tourGuideService = new TourGuideService(gpsUtilClient, new TripPricerClient("localhost"), new RewardCentralClient("localhost"));

        rewardsService.calculateRewards(tourGuideService.getAllUsers().get(0));
        List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0));

        tourGuideService.tracker.stopTracking();

        assertEquals(gpsUtilClient.getAttractions().size(), userRewards.size());
    }

}
