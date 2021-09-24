package no.ks.fiks.ssbAPI.builder;

import no.ks.fiks.ssbAPI.klassApi.SsbKlass;
import no.ks.fiks.ssbAPI.metadataApi.SsbMetadata;
import no.ks.fiks.ssbAPI.metadataApi.SsbMetadataVariables;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

//TODO: Se om du får omgjort Listen til HashMap for å kunne skille mellom årene med data som er filtrert.

public class MetadataBuilder {
    private final SsbMetadata metadata;
    private final SsbKlass klass;
    private final List<SsbMetadataVariables> filteredMetadata;
    private int earliestYear;

    public MetadataBuilder(SsbMetadata metadata, SsbKlass klass) {
        this.metadata = metadata;
        this.klass = klass;
        this.filteredMetadata = new ArrayList<>();
    }

    public List<SsbMetadataVariables> filterMetadata() {
        SsbMetadataVariables tidVar = metadata.getVariables().get(findTidInList());
        SsbMetadataVariables regionVar = metadata.getVariables().get(findRegionInList());
        List<String> values = new ArrayList<>();
        List<String> valueTexts = new ArrayList<>();
        for (String sTid : tidVar.getValues()) {
            int tid = Integer.parseInt(sTid);
            if (tid < LocalDate.now().getYear() - 5)
                continue;
            for (String region : regionVar.getValues()) {
                if (tid >= klass.getKlassCodesResultJson().get(region).getFromYear() && tid <= klass.getKlassCodesResultJson().get(region).getToYear()) {
                    int i = regionVar.getValues().indexOf(region);
                    values.add(region);
                    valueTexts.add(regionVar.getValues().get(i));
                }
            }
            for (SsbMetadataVariables codes : metadata.getVariables()) {
                if (codes.getCode().equals(tidVar.getCode())) {
                    filteredMetadata.add(new SsbMetadataVariables(tidVar.getCode(),
                            tidVar.getText(),
                            List.of(sTid),
                            List.of(tidVar.getValueTexts().get(tidVar.getValues().indexOf(sTid)))));
                } else if (codes.getCode().equals(regionVar.getCode())) {
                    filteredMetadata.add(new SsbMetadataVariables(regionVar.getCode(), regionVar.getText(), values, valueTexts));
                } else {
                    filteredMetadata.add(new SsbMetadataVariables(codes.getCode(), codes.getText(), codes.getValues(), codes.getValueTexts()));
                }
                values = new ArrayList<>();
                valueTexts = new ArrayList<>();
            }
        }
        return filteredMetadata;
    }

    private int findTidInList() {
        return IntStream.range(0, metadata.getVariables().size()).filter(i -> metadata.getVariables().get(i).getCode().equals("Tid")).findFirst().orElse(-1);
    }

    private int findRegionInList() {
        return IntStream.range(0, metadata.getVariables().size()).filter(i -> metadata.getVariables().get(i).getText().equals("region")).findFirst().orElse(-1);
    }
}
