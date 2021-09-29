package no.ks.fiks.ssbAPI.metadataApi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SsbMetadataVariables {
    private final String code;
    private final String text;
    private List<String> values;
    private List<String> valueTexts;
    private int largestValue;
    private int largestValueText;

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

    public void filterValuesAndValueTexts(List<String> filterList, boolean removeAllBut) {
        List<Integer> valueTextPositions;
        List<String> valueTextFilter;
        valueTextPositions = filterList.stream().map(s -> values.indexOf(s)).collect(Collectors.toList());
        valueTextFilter = valueTextPositions.stream().mapToInt(pos -> pos).mapToObj(i -> valueTexts.get(i)).collect(Collectors.toList());

        if (!removeAllBut) {
            values.removeAll(filterList);
            valueTexts.removeAll(valueTextFilter);
        } else {
            values = filterList;
            valueTexts = valueTextFilter;
        }
        largestValue = findLargestValueString(values);
        largestValueText = findLargestValueString(valueTexts);
    }
}
