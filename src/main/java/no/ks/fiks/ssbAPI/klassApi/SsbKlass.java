package no.ks.fiks.ssbAPI.klassApi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <h1>SsbKlass</h1>
 * <p>
 * SsbKlass is a simple class that deserializes the JSON we get back from querying for the classification codes.
 * It also keep track of all the codes as an object.
 */

public class SsbKlass {
    private final Map<String, SsbKlassCodes> klassCodesResultJson;

    /**
     * Simple constructor that initializes the LinkedHashmap
     */
    public SsbKlass() {
        this.klassCodesResultJson = new LinkedHashMap<>();
    }

    /**
     * <h1>convertStringToJson</h1>
     * <p>
     * This method gets a list of classification code query results, deserializes them and adds them to a LinkedHashmap.
     * It also makes sure that the codes that show up several times only gets their dates updated instead of it being
     * completely removed.
     *
     * @param klassCodes This is a list of classification codes query results.
     * @throws JsonProcessingException
     */
    public void convertStringToJson(List<String> klassCodes) throws JsonProcessingException {
        for (String codes : klassCodes) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj = mapper.readTree(codes);
            for (JsonNode klassCode : actualObj.get("codes")) {
                String regionKode = klassCode.get("code").asText();
                String regionNavn = klassCode.get("name").asText();
                LocalDate validFromInRequestedRange = LocalDate.parse(klassCode.get("validFromInRequestedRange").asText());
                LocalDate validToInRequestedRange = LocalDate.parse(klassCode.get("validToInRequestedRange").asText());
                if (klassCodesResultJson.containsKey(regionKode)) {
                    if (validFromInRequestedRange.isBefore(klassCodesResultJson.get(regionKode).getValidFromInRequestedRange())) {
                        klassCodesResultJson.get(regionKode).setValidFromInRequestedRange(validFromInRequestedRange);
                    }
                    if (validToInRequestedRange.isAfter(klassCodesResultJson.get(regionKode).getValidToInRequestedRange())) {
                        klassCodesResultJson.get(regionKode).setValidToInRequestedRange(validToInRequestedRange);
                    }
                } else {
                    klassCodesResultJson.put(regionKode, new SsbKlassCodes(regionKode, regionNavn, validFromInRequestedRange, validToInRequestedRange));
                }
            }
        }
    }

    /**
     * Returns the linkedHashmap
     *
     * @return Returns the LinkedHashMap of classification region codes objects.
     */

    public Map<String, SsbKlassCodes> getKlassCodesResultJson() {
        return klassCodesResultJson;
    }
}
