package no.ks.fiks.ssbAPI.metadataApi;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <h1>SsbMetadataVariables</h1>
 * This class is a simple POJO class with a filter method for filtering the metadata.
 */

public class SsbMetadataVariables {
    private final String code;
    private final String text;
    private List<String> values;
    private List<String> valueTexts;
    private int largestValue;
    private int largestValueText;

    /**
     * Standard constructor that initializes the global variables. Also calls on the {@link #findLargestValueString(List)}
     * to find the value and value text that is the longest.
     *
     * @param code       This is the metadata variable code.
     * @param text       This is the metadata variable text.
     * @param values     This is a list of the values for the current metadata variable.
     * @param valueTexts This is a list of value texts for the current metadata variable, it's in the same order as
     *                   {@link #values}
     */
    public SsbMetadataVariables(String code, String text, List<String> values, List<String> valueTexts) {
        this.code = code;
        this.text = text;
        this.values = values;
        this.valueTexts = valueTexts;
        this.largestValue = findLargestValueString(values);
        this.largestValueText = findLargestValueString(valueTexts);
    }

    /**
     * <h1>getCode</h1>
     *
     * @return Returns the code of the metadata variable.
     */

    public String getCode() {
        return code;
    }

    /**
     * <h1>getText</h1>
     *
     * @return Returns the text of the metadata variable.
     */

    public String getText() {
        return text;
    }

    /**
     * <h1>getValues</h1>
     *
     * @return Returns the list of values.
     */
    public List<String> getValues() {
        return values;
    }

    /**
     * <h1>getValueTexts</h1>
     *
     * @return Returns the list of value texts.
     */
    public List<String> getValueTexts() {
        return valueTexts;
    }

    /**
     * <h1>getLargestValue</h1>
     *
     * @return Returns the size of the longest string in {@link #values}.
     */
    public int getLargestValue() {
        return largestValue;
    }

    /**
     * <h1>getLargestValueText</h1>
     *
     * @return Returns the size of the longest string in {@link #valueTexts}.
     */
    public int getLargestValueText() {
        return largestValueText;
    }

    /**
     * <h1>findLargestValueString</h1>
     * This method iterates the list in the parameter to find the longest string, if not returns -1.
     *
     * @param stringList This is the list which it will find the longest String.
     * @return Returns the biggest String length or -1 if it doesn't find any strings.
     */
    private int findLargestValueString(List<String> stringList) {
        return stringList.stream().mapToInt(String::length).max().orElse(-1);
    }


    /**
     * <h1>filterValuesAndValueTexts</h1>
     * <p>
     * This method filters out the values and value texts provided by the filterList parameter.
     * Since it filters based on the values and not value texts, we use a stream to find the positions of the elements
     * being filtered. Then use that list to find the matching value texts and create a filter list for value texts.
     * The method checks if you want to remove all but the filter or remove the filter elements.
     *
     * @param filterList   This is the list of values that will be filtered on.
     * @param removeAllBut This boolean is to check if we want to remove all but the filter elements, or filter out those
     *                     elements.
     */
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
