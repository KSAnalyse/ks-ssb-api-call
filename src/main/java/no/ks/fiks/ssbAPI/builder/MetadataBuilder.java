package no.ks.fiks.ssbAPI.builder;

import no.ks.fiks.ssbAPI.klassApi.SsbKlass;
import no.ks.fiks.ssbAPI.metadataApi.SsbMetadata;
import no.ks.fiks.ssbAPI.metadataApi.SsbMetadataVariables;

import java.time.LocalDate;
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
        for (String sTid : tidVar.getValues()) {
            List<String> regionValues = new ArrayList<>();
            List<String> regionValueTexts = new ArrayList<>();

            int tid = Integer.parseInt(sTid);
            if (tid < LocalDate.now().getYear() - 5)
                continue;
            for (String region : regionVar.getValues()) {
                if (tid >= klass.getKlassCodesResultJson().get(region).getFromYear() && tid < klass.getKlassCodesResultJson().get(region).getToYear()) {
                    if (checkSize(regionValues) >= 790000) {
                        addToFilteredMap(sTid, tidVar, regionVar, regionValues, regionValueTexts);
                        regionValues = new ArrayList<>();
                        regionValueTexts = new ArrayList<>();
                    }
                    int i = regionVar.getValues().indexOf(region);
                    regionValues.add(region);
                    regionValueTexts.add(regionVar.getValues().get(i));
                }
            }
            if (tidVar.getValues().get(tidVar.getValues().size() - 1).equals(sTid)) {
                addToFilteredMap(sTid, tidVar, regionVar, regionValues, regionValueTexts);
            }
        }
        return filteredMetadata;
    }

    private void addToFilteredMap(String sTid, SsbMetadataVariables tidVar, SsbMetadataVariables regionVar, List<String> regionValues, List<String> regionValueTexts) {
        List<SsbMetadataVariables> tempList = new ArrayList<>();
        for (SsbMetadataVariables codes : metadata.getVariables()) {
            if (codes.getCode().equals(tidVar.getCode())) {
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

    private int checkSize(List<String> region) {
        int count = 1;
        for (SsbMetadataVariables metadataVariables : metadata.getVariables()) {
            if (metadataVariables.getCode().equalsIgnoreCase("Tid"))
                count = count * 1;
            else if (metadataVariables.getText().equalsIgnoreCase("region"))
                count = count * region.size();
            else
                count = count * metadataVariables.getValues().size();
        }
        return count;
    }
}
