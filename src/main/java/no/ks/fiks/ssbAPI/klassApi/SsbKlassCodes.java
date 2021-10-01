package no.ks.fiks.ssbAPI.klassApi;

import java.time.LocalDate;

public class SsbKlassCodes {

    private final String regionCode;
    private final String regionName;
    private LocalDate validFromInRequestedRange;
    private LocalDate validToInRequestedRange;

    public SsbKlassCodes(String regionCode, String regionName, LocalDate validFromInRequestedRange, LocalDate validToInRequestedRange) {
        this.regionCode = regionCode;
        this.regionName = regionName;
        this.validFromInRequestedRange = validFromInRequestedRange;
        this.validToInRequestedRange = validToInRequestedRange;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public String getRegionName() {
        return regionName;
    }

    public LocalDate getValidFromInRequestedRange() {
        return validFromInRequestedRange;
    }

    public void setValidFromInRequestedRange(LocalDate validFromInRequestedRange) {
        this.validFromInRequestedRange = validFromInRequestedRange;
    }

    public LocalDate getValidToInRequestedRange() {
        return validToInRequestedRange;
    }

    public void setValidToInRequestedRange(LocalDate validToInRequestedRange) {
        this.validToInRequestedRange = validToInRequestedRange;
    }

    public int getFromYear() {
        return validFromInRequestedRange.getYear();
    }

    public int getToYear() {
        return validToInRequestedRange.getYear();
    }
}
