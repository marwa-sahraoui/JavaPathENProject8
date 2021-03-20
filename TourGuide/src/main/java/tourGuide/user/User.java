package tourGuide.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import tourGuide.model.ProviderDTO;
import tourGuide.model.VisitedLocationDTO;

public class User {
	private final UUID userId;
	private final String userName;
	private String phoneNumber;
	private String emailAddress;
	private Date latestLocationTimestamp;
	private List<VisitedLocationDTO> visitedLocations = new ArrayList<>();

	private List<UserReward> userRewards = new ArrayList<>();  //recompenses
	private UserPreferences userPreferences = new UserPreferences();
	private List<ProviderDTO> tripDeals = new ArrayList<>();


	public User(UUID userId, String userName, String phoneNumber, String emailAddress) {
		this.userId = userId;
		this.userName = userName;
		this.phoneNumber = phoneNumber;
		this.emailAddress = emailAddress;
	}
	
	public UUID getUserId() {
		return userId;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	
	public String getEmailAddress() {
		return emailAddress;
	}
	
	public void setLatestLocationTimestamp(Date latestLocationTimestamp) {
		this.latestLocationTimestamp = latestLocationTimestamp;
	}
	
	public Date getLatestLocationTimestamp() {
		return latestLocationTimestamp;
	}
	
	public void addToVisitedLocations(VisitedLocationDTO visitedLocation) { //ajout nvelles visited locations
		visitedLocations.add(visitedLocation);
	}
	
	public List<VisitedLocationDTO> getVisitedLocations() {
		return visitedLocations;
	}
	
	public void clearVisitedLocations() {    //clear =remove/effacer
		visitedLocations.clear();
	}
	/*userReward.attraction pour faire la comparaison, on doit comparer les attractions name
	 donc on modifie userReward.attraction?attractionName, pour ajouter un userReward il faut que les attraction names soient
	égaux donc on enléve l'"!"   lié au testRewardServive */
	public void  addUserReward(UserReward userReward) {
		/*if(userRewards.stream().filter(r -> !r.attraction.attractionName.equals(userReward.attraction)).count() == 0) {
			userRewards.add(userReward);*/
			if(userRewards.stream().filter(r -> r.attraction.attractionName.equals(userReward.attraction.attractionName)).count() == 0) {
			userRewards.add(userReward);

		}
	}
	
	public List<UserReward> getUserRewards() {
		return userRewards;
	}
	
	public UserPreferences getUserPreferences() {
		return userPreferences;
	}
	
	public void setUserPreferences(UserPreferences userPreferences) {
		this.userPreferences = userPreferences;
	}

	public VisitedLocationDTO getLastVisitedLocation() {
		return visitedLocations.get(visitedLocations.size() - 1);
	}
	
	public void setTripDeals(List<ProviderDTO> tripDeals) {
		this.tripDeals = tripDeals;
	}
	
	public List<ProviderDTO> getTripDeals() {
		return tripDeals;
	}

}
