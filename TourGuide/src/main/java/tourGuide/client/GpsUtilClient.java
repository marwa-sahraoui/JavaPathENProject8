package tourGuide.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tourGuide.model.AttractionDTO;
import tourGuide.model.VisitedLocationDTO;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class GpsUtilClient {

    @Value("${gpsutil-server-url}")
    private String gpsutilServerUrl;

    public GpsUtilClient(){
    }

    public GpsUtilClient(String gpsutilServerUrl) {
        this.gpsutilServerUrl = gpsutilServerUrl;
    }

    public List<AttractionDTO> getAttractions() {
        RestTemplate restTemplate = new RestTemplate();

        String address = "http://" + gpsutilServerUrl + ":8081/gpsutil/attractions";

        ResponseEntity<AttractionDTO[]> response = restTemplate.getForEntity(address, AttractionDTO[].class);

        return Arrays.asList(response.getBody());
    }

    public VisitedLocationDTO getUserLocation(UUID userId) {

        RestTemplate restTemplate = new RestTemplate();

        String address = "http://" + gpsutilServerUrl + ":8081/gpsutil/userlocation/";

        ResponseEntity<VisitedLocationDTO> response = restTemplate.getForEntity(address + userId, VisitedLocationDTO.class);

        return response.getBody();
    }
}
