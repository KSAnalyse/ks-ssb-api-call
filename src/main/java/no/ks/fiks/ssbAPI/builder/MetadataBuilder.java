package no.ks.fiks.ssbAPI.builder;

import no.ks.fiks.ssbAPI.klassApi.SsbKlass;
import no.ks.fiks.ssbAPI.metadataApi.SsbMetadata;
import no.ks.fiks.ssbAPI.metadataApi.SsbMetadataVariables;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class MetadataBuilder {
    private final SsbMetadata metadata;
    private final SsbKlass klass;
    private final Map<Integer, List<SsbMetadataVariables>> filteredMetadata;
    private int query = 0;

    public MetadataBuilder(SsbMetadata metadata, SsbKlass klass) {
        this.metadata = metadata;
        this.klass = klass;
        this.filteredMetadata = new LinkedHashMap<>();
    }

    public Map<Integer, List<SsbMetadataVariables>> filterMetadata() {
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

                if (tid >= klass.getKlassCodesResultJson().get(region).getFromYear() && tid < klass.getKlassCodesResultJson().get(region).getToYear()) {
                    if (region.equals("EAK") && changedRegion) {
                        region = "0";
                    }
                    if (checkSize(regionValues, true) >= 750000) {
                        addToFilteredMap(sTid, tidVar, regionVar, regionValues, regionValueTexts);
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
                addToFilteredMap(sTid, tidVar, regionVar, regionValues, regionValueTexts);
            }
        }
        return filteredMetadata;
    }

    public Map<Integer, List<SsbMetadataVariables>> buildMetadata() {
        SsbMetadataVariables regionVar = metadata.getVariables().get(findRegionInList());
        boolean added = false;

        List<String> regionValues = new ArrayList<>();
        List<String> regionValueTexts = new ArrayList<>();

        for (String region : regionVar.getValues()) {
            if (checkSize(regionValues, false) >= 750000) {
                addToFilteredMap("", null, regionVar, regionValues, regionValueTexts);
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

        if (!added) {
            addToFilteredMap("", null, regionVar, regionValues, regionValueTexts);
        }

        return filteredMetadata;
    }

    private void addToFilteredMap(String sTid, SsbMetadataVariables tidVar, SsbMetadataVariables regionVar, List<String> regionValues, List<String> regionValueTexts) {
        List<SsbMetadataVariables> tempList = new ArrayList<>();
        for (SsbMetadataVariables codes : metadata.getVariables()) {
            if (tidVar != null && codes.getCode().equals(tidVar.getCode())) {
                tempList.add(new SsbMetadataVariables(tidVar.getCode(),
                        tidVar.getText(),
                        List.of(sTid),
                        List.of(tidVar.getValueTexts().get(tidVar.getValues().indexOf(sTid)))));
            } else if (codes.getCode().equals(regionVar.getCode())) {
                tempList.add(new SsbMetadataVariables(regionVar.getCode(), regionVar.getText(), regionValues, regionValueTexts));
            } else {
                tempList.add(new SsbMetadataVariables(codes.getCode(), codes.getText(), codes.getValues(), codes.getValueTexts()));
            }
        }
        filteredMetadata.put(query++, tempList);
    }

    private int findTidInList() {
        return IntStream.range(0, metadata.getVariables().size()).filter(i -> metadata.getVariables().get(i).getCode().equals("Tid")).findFirst().orElse(-1);
    }

    private int findRegionInList() {
        return IntStream.range(0, metadata.getVariables().size()).filter(i -> metadata.getVariables().get(i).getText().equals("region")).findFirst().orElse(-1);
    }

    private int checkSize(List<String> region, boolean filtered) {
        int count = 1;
        for (SsbMetadataVariables metadataVariables : metadata.getVariables()) {
            if (filtered && metadataVariables.getCode().equalsIgnoreCase("Tid"))
                count = count * 1;
            else if (metadataVariables.getText().equalsIgnoreCase("region"))
                count = count * region.size();
            else
                count = count * metadataVariables.getValues().size();
        }
        return count;
    }
}
