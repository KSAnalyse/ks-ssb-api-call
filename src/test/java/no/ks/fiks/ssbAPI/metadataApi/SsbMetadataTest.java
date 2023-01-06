package no.ks.fiks.ssbAPI.metadataApi;

import no.ks.fiks.ssbAPI.APIService.SsbApiCall;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SsbMetadataTest {

    private static SsbApiCall ssbApiCallFilter;
    private static SsbMetadata metadata;


    @BeforeAll
    static void setSsbMetadata() throws IOException {
        ssbApiCallFilter = new SsbApiCall("11816", 5, null, "131", "104", "214", "231", "127");
        List<String> region = List.of("EAK", "3001");
        List<String> statistikkvariabel = List.of("KOSandelgsavalle0000", "KOSbtodrutggatel0000");
        Map<String, List<String>> localFilter = new LinkedHashMap<>();
        localFilter.put("KOKkommuneregion0000", region);
        localFilter.put("ContentsCode", statistikkvariabel);
        ssbApiCallFilter.metadataApiCall(localFilter);
        ssbApiCallFilter.tableApiCall();
        metadata = ssbApiCallFilter.getMetadata();
    }

    @Test
    void getTitle() {
        String expectedString = "11816: Utvalgte nøkkeltall for samferdsel , etter region, statistikkvariabel og år";
        byte[] charset = expectedString.getBytes();
        String expected = new String(charset, StandardCharsets.UTF_8);
        assertEquals(metadata.getTitle(), expected);
    }

    @Test
    void getVariablesCode() {
        assertAll("Checking the code value of all the variables",
                () -> assertEquals("KOKkommuneregion0000", metadata.getVariables().get(0).getCode()),
                () -> assertEquals("ContentsCode", metadata.getVariables().get(1).getCode()),
                () -> assertEquals("Tid", metadata.getVariables().get(2).getCode()));
    }

    @Test
    void getVariablesText() {
        assertAll("Checking the text value of the all the variables",
                () -> assertEquals("region", metadata.getVariables().get(0).getText()),
                () -> assertEquals("statistikkvariabel", metadata.getVariables().get(1).getText()),
                () -> assertEquals("år", metadata.getVariables().get(2).getText())
        );
    }

    @Test
    void getValuesList() {
        assertAll("Checking value lists of the variables",
                () -> assertEquals(List.of("EAK", "3001"), metadata.getVariables().get(0).getValues()),
                () -> assertEquals(List.of("KOSandelgsavalle0000", "KOSbtodrutggatel0000"), metadata.getVariables().get(1).getValues()),
                () -> assertEquals(List.of("2017", "2018", "2019", "2020", "2021"), metadata.getVariables().get(2).getValues())
        );
    }
}