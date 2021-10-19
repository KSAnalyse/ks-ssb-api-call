package no.ks.fiks.ssbAPI.APIService;

import no.ks.fiks.ssbAPI.klassApi.SsbKlass;
import no.ks.fiks.ssbAPI.metadataApi.SsbMetadata;
import no.ks.fiks.ssbAPI.metadataApi.SsbMetadataVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SsbApiCallTest {
    private SsbApiCall ssbApiCall;
    private SsbMetadata metadata;
    private SsbKlass klass;
    private final Map<String, List<String>> filter = new LinkedHashMap<>();

    @BeforeEach
    void setObjects() {
        ssbApiCall = new SsbApiCall("11816", -1, "131", "104", "214", "231", "127");
        metadata = ssbApiCall.getMetadata();
        klass = ssbApiCall.getKlass();
        List<String> region = List.of("EAK", "EAKUO", "3001", "EKG16", "EKG17");
        List<String> omfang = List.of("A");
        List<String> funksjon = List.of("100", "110", "120", "FGK8b", "FGK9");
        List<String> art = List.of("AGD4", "AGD10", "AGD2", "AGD56", "AGD28");
        filter.put("KOKkommuneregion0000", region);
        filter.put("KOKregnskapsomfa0000", omfang);
        filter.put("KOKfunksjon0000", funksjon);
        filter.put("KOKart0000", art);
    }

    @Test
    void testMetadataApiCallInitial() {
        assertEquals("11816: Utvalgte nøkkeltall for samferdsel , etter region, statistikkvariabel og år", metadata.getTitle());
    }

    @Test
    void testMetadataApiCallTableNumber() throws IOException {
        ssbApiCall.metadataApiCall("11814");
        metadata = ssbApiCall.getMetadata();
        assertEquals("11814: Kommunale veier, utvalgte kjennetegn , etter region, statistikkvariabel og år", metadata.getTitle());
    }

    @Test
    void testMetadataApiCallTableNumberAndFilterRemove() throws IOException {
        ssbApiCall.metadataApiCall("12367", filter, false);
        metadata = ssbApiCall.getMetadata();
        assertAll("Checking if removed variables are gone and the first values are ones after the ones removed.",
                () -> assertEquals("12367: Detaljerte regnskapstall driftsregnskapet, etter region, regnskapsomfang, funksjon, art, statistikkvariabel og år", metadata.getTitle()),
                () -> assertEquals("3002", metadata.getVariables().get(0).getValues().get(0)),
                () -> assertEquals("3003", metadata.getVariables().get(0).getValues().get(1)),
                () -> assertEquals("3004", metadata.getVariables().get(0).getValues().get(2)),
                () -> assertThrowsExactly(IndexOutOfBoundsException.class, () -> metadata.getVariables().get(0).getValues().get(767)),
                () -> assertThrowsExactly(IndexOutOfBoundsException.class, () -> metadata.getVariables().get(0).getValues().get(768)),
                () -> assertEquals(-1, metadata.getVariables().get(0).getValues().indexOf("EAK")),
                () -> assertEquals(-1, metadata.getVariables().get(0).getValues().indexOf("EAKUO")),
                () -> assertEquals(-1, metadata.getVariables().get(0).getValues().indexOf("3001")),
                () -> assertEquals(-1, metadata.getVariables().get(0).getValues().indexOf("EKG16")),
                () -> assertEquals(-1, metadata.getVariables().get(0).getValues().indexOf("EKG17")),
                () -> assertEquals(-1, metadata.getVariables().get(1).getValues().indexOf("A")),
                () -> assertEquals(0, metadata.getVariables().get(1).getValues().indexOf("B")),
                () -> assertEquals(-1, metadata.getVariables().get(2).getValues().indexOf("100")),
                () -> assertEquals(-1, metadata.getVariables().get(2).getValues().indexOf("110")),
                () -> assertEquals(-1, metadata.getVariables().get(2).getValues().indexOf("120")),
                () -> assertEquals(-1, metadata.getVariables().get(2).getValues().indexOf("FGK8b")),
                () -> assertEquals(-1, metadata.getVariables().get(2).getValues().indexOf("FGK9")),
                () -> assertEquals("121", metadata.getVariables().get(2).getValues().get(0)),
                () -> assertEquals("130", metadata.getVariables().get(2).getValues().get(1)),
                () -> assertEquals(-1, metadata.getVariables().get(3).getValues().indexOf("AGD4")),
                () -> assertEquals(-1, metadata.getVariables().get(3).getValues().indexOf("AGD10")),
                () -> assertEquals(-1, metadata.getVariables().get(3).getValues().indexOf("AGD2")),
                () -> assertEquals(-1, metadata.getVariables().get(3).getValues().indexOf("AGD56")),
                () -> assertEquals(-1, metadata.getVariables().get(3).getValues().indexOf("AGD28")),
                () -> assertEquals("AG16", metadata.getVariables().get(3).getValues().get(0)),
                () -> assertEquals("A710", metadata.getVariables().get(3).getValues().get(1))
        );
    }

    @Test
    void testMetadataApiCallTableNumberAndFilterRemoveAllBut() throws IOException {
        ssbApiCall.metadataApiCall("12367", filter, true);
        metadata = ssbApiCall.getMetadata();

        assertAll("Check that list values only contain filter values.",
                () -> assertEquals("12367: Detaljerte regnskapstall driftsregnskapet, etter region, regnskapsomfang, funksjon, art, statistikkvariabel og år", metadata.getTitle()),
                () -> assertEquals(filter.get("KOKkommuneregion0000"), metadata.getVariables().get(0).getValues()),
                () -> assertEquals(filter.get("KOKregnskapsomfa0000"), metadata.getVariables().get(1).getValues()),
                () -> assertEquals(filter.get("KOKfunksjon0000"), metadata.getVariables().get(2).getValues()),
                () -> assertEquals(filter.get("KOKart0000"), metadata.getVariables().get(3).getValues())
        );
    }

    @Test
    void testMetadataApiCallFilter() throws IOException {
        List<String> region = List.of("EAK", "3001");
        List<String> statistikkvariabel = List.of("KOSandelgsavalle0000", "KOSbtodrutggatel0000");
        Map<String, List<String>> localFilter = new LinkedHashMap<>();
        localFilter.put("KOKkommuneregion0000", region);
        localFilter.put("ContentsCode", statistikkvariabel);
        ssbApiCall.metadataApiCall(localFilter, true);
        metadata = ssbApiCall.getMetadata();

        assertAll("Check that table is unchanged and values only contain filter values.",
                () -> assertEquals("11816: Utvalgte nøkkeltall for samferdsel , etter region, statistikkvariabel og år", metadata.getTitle()),
                () -> assertEquals(region, metadata.getVariables().get(0).getValues()),
                () -> assertEquals(statistikkvariabel, metadata.getVariables().get(1).getValues()));
    }

    @Test
    void tableApiCallFilter() throws IOException {
        List<String> region = List.of("EAK", "3001", "0101");
        List<String> statistikkvariabel = List.of("KOSandelgsavalle0000", "KOSbtodrutggatel0000");
        List<String> aar = List.of("2016", "2020");
        Map<String, List<String>> localFilter = new LinkedHashMap<>();
        localFilter.put("KOKkommuneregion0000", region);
        localFilter.put("ContentsCode", statistikkvariabel);
        localFilter.put("Tid", aar);

        String jsonOne = Files.readString(Path.of("src/main/resources/testMetadataResult2016.json"));
        String jsonTwo = Files.readString(Path.of("src/main/resources/testMetadataResult2020.json"));
        jsonOne = jsonOne.replaceAll("\\s+(?=([^\"]*\"[^\"]*\")*[^\"]*$)", "");
        jsonTwo = jsonTwo.replaceAll("\\s+(?=([^\"]*\"[^\"]*\")*[^\"]*$)", "");

        ssbApiCall.metadataApiCall("11816", localFilter, true);


        List<String> queryList = ssbApiCall.tableApiCall();
        String finalJsonTwo = jsonTwo;
        String finalJsonOne = jsonOne;

        assertAll("Checking various parts of the query",
                () -> assertEquals(2, queryList.size()),
                () -> assertEquals(finalJsonOne, queryList.get(0)),
                () -> assertEquals(finalJsonTwo, queryList.get(1)));
    }

    @Test
    void tableApiCallUnfiltered() throws IOException {
        ssbApiCall = new SsbApiCall("11816", -1);
        List<String> region = List.of("EAK", "3001", "0101");
        List<String> statistikkvariabel = List.of("KOSandelgsavalle0000", "KOSbtodrutggatel0000");
        List<String> aar = List.of("2016", "2020");
        Map<String, List<String>> localFilter = new LinkedHashMap<>();
        localFilter.put("KOKkommuneregion0000", region);
        localFilter.put("ContentsCode", statistikkvariabel);
        localFilter.put("Tid", aar);

        String jsonOne = Files.readString(Path.of("src/main/resources/testMetadataResult.json"));
        /*String[] jsonSplit = new String[100];

        int jsonLen = jsonOne.length() / 100;
        int lastLen = 0;
        for (int i = 0; i < jsonSplit.length - 1; i++) {
            jsonSplit[i] = jsonOne.substring(jsonLen * i, jsonLen * (i + 1));
            lastLen = jsonLen * (i + 1);
        }
        jsonSplit[99] = jsonOne.substring(lastLen);
        jsonOne = "";
        StringBuilder cleanedJson = new StringBuilder();
        for (String s : jsonSplit) {
            s = s.replaceAll("\\s+(?=([^\"]*\"[^\"]*\")*[^\"]*$)", "");
            s = s.replaceAll("\\t", "");
            s = s.replaceAll("\\n", "");

            cleanedJson.append(s);
        }*/
        ssbApiCall.metadataApiCall(localFilter, true);
        jsonOne = jsonOne.replaceAll("\\s+(?=([^\"]*\"[^\"]*\")*[^\"]*$)", "");
        List<String> queryList = ssbApiCall.tableApiCall();

        String finalJsonOne = jsonOne;
        assertAll("Checking various parts of the query",
                () -> assertEquals(1, queryList.size()),
                () -> assertEquals(finalJsonOne, queryList.get(0))
        );
    }

    @Test
    void filterYears() throws IOException {
        ssbApiCall = new SsbApiCall("11816", 2);
        ssbApiCall.tableApiCall();
        metadata = ssbApiCall.getMetadata();
        assertEquals(List.of("2019", "2020"), metadata.getVariables().get(2).getValues());
    }

    @Test
    void filterYearsMoreThanListSize() throws IOException {
        ssbApiCall = new SsbApiCall("11816", 10);
        ssbApiCall.tableApiCall();
        metadata = ssbApiCall.getMetadata();
        assertEquals(List.of("2015", "2016", "2017", "2018", "2019", "2020"), metadata.getVariables().get(2).getValues());
    }

    @Test
    void quarterlyToYearConversionApiCall() throws IOException {
        ssbApiCall = new SsbApiCall("01222", 5);
        metadata = ssbApiCall.getMetadata();
        ssbApiCall.tableApiCall();
        int count = 0;
        for (SsbMetadataVariables metadataVariables : metadata.getVariables()) {
            if (metadataVariables.getCode().equals("Tid"))
                break;
            count++;
        }
        assertEquals(20, metadata.getVariables().get(count).getValues().size());
    }

    @Test
    void monthlyToYearConversionApiCall() throws IOException {
        ssbApiCall = new SsbApiCall("08655", 5);
        metadata = ssbApiCall.getMetadata();
        ssbApiCall.tableApiCall();
        int count = 0;
        for (SsbMetadataVariables metadataVariables : metadata.getVariables()) {
            if (metadataVariables.getCode().equals("Tid"))
                break;
            count++;
        }
        assertEquals(60, metadata.getVariables().get(count).getValues().size());
    }

    @Test
    void getMetadata() {
        assertEquals(metadata.getTitle(), "11816: Utvalgte nøkkeltall for samferdsel , etter region, statistikkvariabel og år");
    }

    @Test
    void getKlass() {
        assertNotNull(klass);
    }
}