package no.ks.fiks.ssbAPI.metadataApi;

import java.util.List;

public class SsbMetadataVariables {
    private final String code;
    private final String text;
    private final List<String> values;
    private final List<String> valueTexts;
    private final int largestValue;
    private final int largestValueText;

    public SsbMetadataVariables(String code, String text, List<String> values, List<String> valueTexts) {
        this.code = code;
        this.text = text;
        this.values = values;
        this.valueTexts = valueTexts;
        this.largestValue = findLargestValueString(values);
        this.largestValueText = findLargestValueString(valueTexts);
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

    public int getLargestValue() {
        return largestValue;
    }

    public int getLargestValueText() {
        return largestValueText;
    }

    private int findLargestValueString(List<String> stringList) {
        return stringList.stream().mapToInt(String::length).max().orElse(-1);
    }
}
