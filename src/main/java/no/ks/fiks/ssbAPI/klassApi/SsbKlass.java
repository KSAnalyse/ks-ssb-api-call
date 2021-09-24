package no.ks.fiks.ssbAPI.klassApi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SsbKlass {
    private final Map<String, SsbKlassCodes> klassCodesResultJson;

    public SsbKlass() {
        this.klassCodesResultJson = new LinkedHashMap<>();
    }

    public void convertStringToJson(List<String> klassCodes) throws JsonProcessingException {
        for (String codes : klassCodes) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj = mapper.readTree(codes);
            for (JsonNode klassCode : actualObj.get("codes")) {
                String regionKode = klassCode.get("code").asText();
                String regionNavn = klassCode.get("name").asText();
                LocalDate validFromInRequestedRange = LocalDate.parse(klassCode.get("validFromInRequestedRange").asText());
                LocalDate validToInRequestedRange = LocalDate.parse(klassCode.get("validToInRequestedRange").asText());

                klassCodesResultJson.put(regionKode, new SsbKlassCodes(regionKode, regionNavn, validFromInRequestedRange, validToInRequestedRange));
            }
        }
    }

    public Map<String, SsbKlassCodes> getKlassCodesResultJson() {
        return klassCodesResultJson;
    }
}
