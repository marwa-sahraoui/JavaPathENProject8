package tourguide.rewardcentral;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rewardCentral.RewardCentral;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rewardCentral")
public class RewardCentralController {
    @Autowired
    RewardCentral rewardCentral;

    @GetMapping("/attractionRewardPoints/attraction/{attractionId}/user/{userId}")
    public Integer getAttractionRewardPoints(@PathVariable UUID attractionId, @PathVariable UUID userId) {
        return rewardCentral.getAttractionRewardPoints(attractionId, userId);
    }
}