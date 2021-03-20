package tourGuide;

import org.apache.commons.lang3.time.StopWatch;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class TestPerformance {

    /*
     * A note on performance improvements:
     *
     *     The number of users generated for the high volume tests can be easily adjusted via this method:
     *
     *     		InternalTestHelper.setInternalUserNumber(100000);
     *
     *
     *     These tests can be modified to suit new solutions, just as long as the performance metrics
     *     at the end of the tests remains consistent.
     *
     *     These are performance metrics that we are trying to hit:
     *
     *     highVolumeTrackLocation: 100,000 users within 15 minutes:
     *     		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     *
     *     highVolumeGetRewards: 100,000 users within 20 minutes:
     *          assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     */

    //@Ignore
    @Test
    public void highVolumeTrackLocation() {

        GpsUtilClient gpsUtil = new GpsUtilClient("localhost");
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentralClient("localhost"));
        // Users should be incremented up to 100,000, and test finishes within 15 minutes
        InternalTestHelper.setInternalUserNumber(100000);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, new TripPricerClient("localhost"), new RewardCentralClient("localhost"));

        List<User> allUsers = new ArrayList<>();
        allUsers = tourGuideService.getAllUsers();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
			/*on crée un grand nombre de thread (threadPool)qui seront exécutés simultanément gràce
			 à la methode newFixedThreadpool*/
            ExecutorService executorService = Executors.newFixedThreadPool(44);
            for (User user : allUsers) {
                //trackUserLocation sera executé dans un thread runnable par l'instance executorService
                Runnable runnable = () -> {
                    tourGuideService.trackUserLocation(user);
                };
                executorService.execute(runnable);

            }
			/*manuellemnt on arréte l'executorService tout en laissant une marge du temps pour
			 terminer les thread qui sont encours d'exécuter*/
            executorService.shutdown();
            executorService.awaitTermination(15, TimeUnit.MINUTES);
        } catch (InterruptedException interruptedException) {

        }


        stopWatch.stop();
        tourGuideService.tracker.stopTracking();

        System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }

    //@Ignore
    @Test
    public void highVolumeGetRewards() {

        {
            GpsUtilClient gpsUtil = new GpsUtilClient("localhost");
            RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentralClient("localhost"));

            // Users should be incremented up to 100,000, and test finishes within 20 minutes
            InternalTestHelper.setInternalUserNumber(100000);
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            TourGuideService tourGuideService = new TourGuideService(gpsUtil, new TripPricerClient("localhost"), new RewardCentralClient("localhost"));

            AttractionDTO attraction = gpsUtil.getAttractions().get(0);
            List<User> allUsers = new ArrayList<>();
            allUsers = tourGuideService.getAllUsers();

            try {
                ExecutorService executorService = Executors.newFixedThreadPool(60);
                //même principe, on met les méthodes à effectuer dans un runnable puis on execute à travers le
                //l'executorService un threadpool pour executer n fois ce runnable
                for (User user : allUsers) {
                    Runnable runnable = () -> {
                        user.addToVisitedLocations(new VisitedLocationDTO(user.getUserId(), attraction, new Date()));
                        rewardsService.calculateRewards(user);
                        assertTrue(user.getUserRewards().size() > 0);
                    };
                    executorService.execute(runnable);
                }
                executorService.shutdown();
                executorService.awaitTermination(20, TimeUnit.MINUTES);

            } catch (InterruptedException interruptedException) {

            }

            stopWatch.stop();
            tourGuideService.tracker.stopTracking();


            System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
            assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
        }
    }
}
