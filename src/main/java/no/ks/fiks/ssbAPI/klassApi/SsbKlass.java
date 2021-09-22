package no.ks.fiks.ssbAPI.klassApi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SsbKlass {
    private final List<String> klassCodes;
    private final List<SsbKlassCodes> klassCodesResultJson;

    public SsbKlass(List<String> klassCodes) throws JsonProcessingException {
        this.klassCodes = klassCodes;
        this.klassCodesResultJson = new ArrayList<>();
        convertStringToJson();
    }

    private void convertStringToJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = mapper.readTree(klassCodes.get(0));
        for (JsonNode klassCode : actualObj.get("codes")) {
            String regionKode = klassCode.get("code").asText();
            String regionNavn = klassCode.get("name").asText();
            LocalDate validFromInRequestedRange = LocalDate.parse(klassCode.get("validFromInRequestedRange").asText());
            LocalDate validToInRequestedRange = LocalDate.parse(klassCode.get("validToInRequestedRange").asText());

            klassCodesResultJson.add(new SsbKlassCodes(regionKode, regionNavn, validFromInRequestedRange, validToInRequestedRange));
        }
    }

    public List<SsbKlassCodes> getKlassCodesResultJson() {
        return klassCodesResultJson;
    }
}
