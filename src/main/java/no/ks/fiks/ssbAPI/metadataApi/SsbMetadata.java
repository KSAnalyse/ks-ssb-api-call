package no.ks.fiks.ssbAPI.metadataApi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class SsbMetadata {
    private final String metadataResult;
    private final List<SsbMetadataVariables> variables;
    private String title;

    public SsbMetadata(String metadataResult) throws JsonProcessingException {
        this.metadataResult = metadataResult;
        variables = new ArrayList<>();
        convertStringToJson();
    }

    private void convertStringToJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = mapper.readTree(metadataResult);
        title = actualObj.get("title").asText();
        for (JsonNode jn : actualObj.get("variables")) {
            String code = jn.get("code").asText();
            String text = jn.get("text").asText();
            List<String> values = new ArrayList<>();
            List<String> valueTexts = new ArrayList<>();
            for (int i = 0; i < jn.get("values").size(); i++) {
                values.add(jn.get("values").get(i).asText());
                valueTexts.add(jn.get("valueTexts").get(i).asText());
            }
            variables.add(new SsbMetadataVariables(code, text, values, valueTexts));
        }
    }

    public String getTitle() {
        return title;
    }

    public List<SsbMetadataVariables> getVariables() {
        return variables;
    }
}
