package tourguide.trippricer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tripPricer")
public class TripPricerController {
@Autowired
TripPricer tripPricer;

    @GetMapping("/Price/apiKey/{apiKey}/attraction/{attractionId}/adults/{adults}/children/{children}/nightsStay/{nightsStay}/rewardsPoints/{rewardsPoints}" )
    public List<Provider> getPrice(@PathVariable String apiKey, @PathVariable UUID attractionId,
                                   @PathVariable int adults, @PathVariable int children, @PathVariable int nightsStay, @PathVariable int rewardsPoints) {
        return tripPricer.getPrice(apiKey, attractionId,adults,children,nightsStay,rewardsPoints);
    }

}
