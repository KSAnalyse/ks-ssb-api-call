package no.ks.fiks.ssbAPI.builder;

import no.ks.fiks.ssbAPI.klassApi.SsbKlass;
import no.ks.fiks.ssbAPI.metadataApi.SsbMetadata;
import no.ks.fiks.ssbAPI.metadataApi.SsbMetadataVariables;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * <h1>MetadataBuilder</h1>
 * <p>
 * MetadataBuilder handles the building of the metadata that will be used for each query, depending on
 * if you have added classification codes and what data is available in the metadata. It returns a map of lists in which
 * it filters out regions not valid in the years you are querying or if no classification codes are provided, returns
 * the same map unfiltered. It also makes sure the metadata it builds up doesn't exceed 800000 rows. Before it reaches
 * 800000 rows, it starts on a new list.
 */

public class MetadataBuilder {

    private final SsbMetadata metadata;
    private final SsbKlass klass;
    private final Map<Integer, List<SsbMetadataVariables>> builtMetadata;
    private int query = 0;

    /**
     * <h1>MetadataBuilder</h1>
     * <p>
     * Standard constructor that just sets the objects it receives and initializes {@link #builtMetadata}.
     *
     * @param metadata This is the metadata object from SsbApiClass
     * @param klass    This is the metadata object from SsbApiClass
     */

    public MetadataBuilder(SsbMetadata metadata, SsbKlass klass) {
        this.metadata = metadata;
        this.klass = klass;
        this.builtMetadata = new LinkedHashMap<>();
    }

    /**
     * <h1>buildMetadata()</h1>
     * <p>
     * This method checks if you have provided classification codes or if the 'Region' variable is available in
     * the metadata class and calls on the correct method. Either {@link #buildFilteredMetadata()} or {@link #buildUnfilteredMetadata()}.
     *
     * @see #buildUnfilteredMetadata()
     * @see #buildFilteredMetadata()
     */

    public void buildMetadata() {
        if (klass == null || findRegionInList() == -1)
            buildUnfilteredMetadata();
        else
            buildFilteredMetadata();
    }

    /**
     * <h1>buildFilteredMetadata()</h1>
     * <p>
     * This method filters out the regions for each year you are querying. It filters them by year because some regions
     * are valid in certain years and not all. This is done, so we avoid querying for regions where the value of the row
     * we get is 'NULL' because the region is not valid for that year.
     * <p>
     * We also save space by filtering out region codes that did not exist in that year. Important to note that it does
     * not filter rows that are null in years when the region is valid.
     * <p>
     * By checking the region in the metadata against the classification code list, we can find if the region is valid
     * for the year we are querying. If it is, then the method adds it to the list. Once that list might pass 800k rows
     * it adds that list to the Map and empties the list.
     * <p>
     * It returns a map with an Integer key, so we can identify how many queries will be done, this is so we can follow
     * the API limitation of 30 queries per 60 seconds. It also uses a map so each map insert can be built as a separate
     * query. A check for 30 queries per 60 seconds will be implemented in the future.
     * <p>
     * NOTE: On tables that has 0 as the whole country instead of EAK, we have to switch 0 to EAK and back again. This is done
     * because no classification code list has 0 to identify the whole country.
     *
     * @see SsbMetadata
     * @see SsbMetadataVariables
     * @see SsbKlass
     * @see no.ks.fiks.ssbAPI.klassApi.SsbKlassCodes
     * @see #findTidInList()
     * @see #findRegionInList()
     * @see #checkSize(List, boolean, String)
     * @see #addToMap(String, SsbMetadataVariables, SsbMetadataVariables, List, List)
     */

    private void buildFilteredMetadata() {
        SsbMetadataVariables tidVar = metadata.getVariables().get(findTidInList());
        SsbMetadataVariables regionVar = metadata.getVariables().get(findRegionInList());
        boolean added = false;
        for (String sTid : tidVar.getValues()) {
            List<String> regionValues = new ArrayList<>();
            List<String> regionValueTexts = new ArrayList<>();

            int tid = Integer.parseInt(sTid);

            for (String region : regionVar.getValues()) {
                boolean changedRegion = false;
                if (region.equals("0")) {
                    changedRegion = true;
                    region = "EAK";
                }
                if (!klass.getKlassCodesResultJson().containsKey(region))
                    continue;

                if (tid >= klass.getKlassCodesResultJson().get(region).getFromYear()
                        && tid < klass.getKlassCodesResultJson().get(region).getToYear()) {
                    if (region.equals("EAK") && changedRegion) {
                        region = "0";
                    }
                    if (checkSize(regionValues, true, regionVar.getText()) >= 750000) {
                        addToMap(sTid, tidVar, regionVar, regionValues, regionValueTexts);
                        added = true;
                        regionValues = new ArrayList<>();
                        regionValueTexts = new ArrayList<>();
                        int i = regionVar.getValues().indexOf(region);
                        regionValues.add(region);
                        regionValueTexts.add(regionVar.getValues().get(i));
                    } else {
                        int i = regionVar.getValues().indexOf(region);
                        regionValues.add(region);
                        regionValueTexts.add(regionVar.getValues().get(i));
                        added = false;
                    }
                }
            }
            if (!added) {
                addToMap(sTid, tidVar, regionVar, regionValues, regionValueTexts);
            }
        }
    }

    /**
     * <h1>buildUnfilteredMetadata</h1>
     * <p>
     * This method is practically the same as buildFilteredMetadata, but it ignores all filtering based on
     * classification code lists and years. It's also the method that handles tables without 'Region' metadata. See the
     * {@link #buildFilteredMetadata()} method.
     *
     * @see #buildFilteredMetadata()
     */

    private void buildUnfilteredMetadata() {
        SsbMetadataVariables firstVar = metadata.getVariables().get(0);
        boolean added = false;

        List<String> firstVarValues = new ArrayList<>();
        List<String> firstVarValueTexts = new ArrayList<>();

        for (String firstVariable : firstVar.getValues()) {
            if (checkSize(firstVarValues, false, firstVar.getText()) >= 750000) {
                addToMap("", null, firstVar, firstVarValues, firstVarValueTexts);
                added = true;
                firstVarValues = new ArrayList<>();
                firstVarValueTexts = new ArrayList<>();
                int i = firstVar.getValues().indexOf(firstVariable);
                firstVarValues.add(firstVariable);
                firstVarValueTexts.add(firstVar.getValues().get(i));
            } else {
                int i = firstVar.getValues().indexOf(firstVariable);
                firstVarValues.add(firstVariable);
                firstVarValueTexts.add(firstVar.getValues().get(i));
                added = false;
            }
        }

        if (!added) {
            addToMap("", null, firstVar, firstVarValues, firstVarValueTexts);
        }
    }

    /**
     * <h1>addToMap</h1>
     * <p>
     * This method adds the lists from metadata and the custom build metadata to the Map.
     * Loops through metadata variables and adds them to the Map.
     *
     * @param sTid                This is the String value of 'Tid'
     * @param tidVar              This is the 'Tid' object from buildFilteredMetadata
     * @param iteratingVar        This is variable object the {@link #buildFilteredMetadata()}/{@link #buildUnfilteredMetadata()} iterates on.
     * @param iteratingValues     This is variable list of values the {@link #buildFilteredMetadata()}/{@link #buildUnfilteredMetadata()} built.
     * @param iteratingValueTexts This is variable list of value texts the {@link #buildFilteredMetadata()}/{@link #buildUnfilteredMetadata()} built.
     * @see SsbMetadataVariables
     * @see #buildFilteredMetadata()
     * @see #buildUnfilteredMetadata()
     */

    private void addToMap(String sTid, SsbMetadataVariables tidVar, SsbMetadataVariables iteratingVar,
                          List<String> iteratingValues, List<String> iteratingValueTexts) {
        List<SsbMetadataVariables> tempList = new ArrayList<>();
        for (SsbMetadataVariables codes : metadata.getVariables()) {
            if (tidVar != null && codes.getCode().equals(tidVar.getCode())) {
                tempList.add(new SsbMetadataVariables(tidVar.getCode(),
                        tidVar.getText(),
                        List.of(sTid),
                        List.of(tidVar.getValueTexts().get(tidVar.getValues().indexOf(sTid)))));
            } else if (codes.getCode().equals(iteratingVar.getCode())) {
                tempList.add(new SsbMetadataVariables(iteratingVar.getCode(), iteratingVar.getText(), iteratingValues, iteratingValueTexts));
            } else {
                tempList.add(new SsbMetadataVariables(codes.getCode(), codes.getText(), codes.getValues(), codes.getValueTexts()));
            }
        }
        builtMetadata.put(query++, tempList);
    }

    /**
     * <h1>findTidInList</h1>
     * <p>
     * This method finds 'Tid' metadata and returns the position it's in, if it doesn't exist it returns -1.
     *
     * @return Returns the position of 'Tid' variable in metadata variables list.
     */
    private int findTidInList() {
        return IntStream.range(0, metadata.getVariables().size()).filter(i ->
                metadata.getVariables()
                        .get(i)
                        .getCode().equals("Tid")).findFirst().orElse(-1);
    }

    /**
     * <h1>findRegionsInList</h1>
     * <p>
     * This method finds 'Region' metadata and returns the position it's in, if it doesn't exist it returns -1.
     *
     * @return Returns the position of 'Region' variable in metadata variables list.
     */
    private int findRegionInList() {
        return IntStream.range(0, metadata.getVariables().size()).filter(i ->
                metadata.getVariables()
                        .get(i)
                        .getText().equals("region")).findFirst().orElse(-1);
    }

    /**
     * <h1>checkSize</h1>
     * This method calculates the size of the current list of metadata.
     *
     * @param iteratingVar     This is the variable we will be iterating on.
     * @param filtered         This is to check if it's coming from {@link #buildFilteredMetadata()} or {@link #buildUnfilteredMetadata()}.
     * @param iteratingVarName This is the name of the metadata we are iterating on.
     * @return Returns the size of the list.
     */

    private int checkSize(List<String> iteratingVar, boolean filtered, String iteratingVarName) {
        int count = 1;
        for (SsbMetadataVariables metadataVariables : metadata.getVariables()) {
            if (filtered && metadataVariables.getCode().equalsIgnoreCase("Tid"))
                count = count * 1;
            else if (metadataVariables.getText().equalsIgnoreCase(iteratingVarName))
                count = count * iteratingVar.size();
            else
                count = count * metadataVariables.getValues().size();
        }
        return count;
    }

    public Map<Integer, List<SsbMetadataVariables>> getBuiltMetadata() {
        return builtMetadata;
    }
}
