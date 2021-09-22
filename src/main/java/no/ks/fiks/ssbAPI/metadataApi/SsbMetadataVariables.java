package no.ks.fiks.ssbAPI.metadataApi;

import java.util.List;

public class SsbMetadataVariables {
    private String code;
    private String text;
    private List<String> values;
    private List<String> valueTexts;

    public SsbMetadataVariables(String code, String text, List<String> values, List<String> valueTexts) {
        this.code = code;
        this.text = text;
        this.values = values;
        this.valueTexts = valueTexts;
    }

    public String getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public List<String> getValues() {
        return values;
    }

    public List<String> getValueTexts() {
        return valueTexts;
    }
}
