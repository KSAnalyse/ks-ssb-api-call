package no.ks.fiks.ssbAPI.metadataApi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * <h1>SsbMetdata</h1>
 * This class deserializes the metadata JSON query and creates a list of objects.
 * It also filters the metadata if the a filter is provided.
 */
public class SsbMetadata {
    private final String metadataResult;
    private final List<SsbMetadataVariables> variables;
    private String title;
    private Map<String, List<String>> metadataFilter;
    private boolean removeAllBut;

    /**
     * This constructor is used if no filter is provided when initializing the object.
     *
     * @param metadataResult This is the metadata query result.
     * @throws JsonProcessingException
     */
    public SsbMetadata(String metadataResult) throws JsonProcessingException {
        this.metadataResult = metadataResult;
        variables = new ArrayList<>();
        convertStringToJson();
    }

    /**
     * This constructor is ued if a metadata filter is provided.
     *
     * @param metadataResult This is the metadata query result.
     * @param metadataFilter This is the Map of the metadata which will be filtered.
     * @throws JsonProcessingException
     */
    public SsbMetadata(String metadataResult, Map<String, List<String>> metadataFilter) throws JsonProcessingException {
        this.metadataResult = metadataResult;
        this.metadataFilter = metadataFilter;
        variables = new ArrayList<>();
        convertStringToJson();
        filterMetadata();
    }

    /**
     * <h1>convertStringToJson</h1>
     * <p>
     * This method deserializes the JSON string from the metadata query and adds them to an object list.
     *
     * @throws JsonProcessingException
     */

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

    /**
     * <h1>getTitle</h1>
     *
     * @return Returns the title of the table you queried.
     */
    public String getTitle() {
        return title;
    }

    /**
     * <h1>getVariables</h1>
     *
     * @return Returns the list of objects.
     */
    public List<SsbMetadataVariables> getVariables() {
        return variables;
    }

    /**
     * <h1>filterMetadata</h1>
     * <p>
     * This method calls on the filter method in SsbMetadataVariables to filter out or only use the filters in the list.
     */
    private void filterMetadata() {
        for (String key : metadataFilter.keySet()) {
            String strippedKey;
            if (key.contains("!"))
                strippedKey = key.replace("!", "");
            else {
                strippedKey = key;
            }
            String finalStrippedKey = strippedKey;
            Optional<SsbMetadataVariables> metadataVariables =  variables.stream()
                    .filter(var -> var.getCode().equals(finalStrippedKey))
                    .findFirst();

            metadataVariables.ifPresent(ssbMetadataVariables -> ssbMetadataVariables.filterValuesAndValueTexts(key, metadataFilter.get(key)));
        }
    }
}
