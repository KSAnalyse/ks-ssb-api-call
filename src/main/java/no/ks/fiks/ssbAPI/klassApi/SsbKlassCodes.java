package no.ks.fiks.ssbAPI.klassApi;

import java.time.LocalDate;

/**
 * <h1>SsbKlassCodes</h1>
 * <p>
 * Simple POJO class for classification codes.
 */

public class SsbKlassCodes {

    private final String regionCode;
    private final String regionName;
    private LocalDate validFromInRequestedRange;
    private LocalDate validToInRequestedRange;

    /**
     * Initializes the variables.
     *
     * @param regionCode                This is the region code from classification codes query result.
     * @param regionName                This is the region name belonging to the region code.
     * @param validFromInRequestedRange This is the date this region code is valid from.
     * @param validToInRequestedRange   This the date this region code is valid to.
     */
    public SsbKlassCodes(String regionCode, String regionName, LocalDate validFromInRequestedRange, LocalDate validToInRequestedRange) {
        this.regionCode = regionCode;
        this.regionName = regionName;
        this.validFromInRequestedRange = validFromInRequestedRange;
        this.validToInRequestedRange = validToInRequestedRange;
    }

    /**
     * <h1>getRegionCode</h1>
     *
     * @return Returns the region code.
     */
    public String getRegionCode() {
        return regionCode;
    }

    /**
     * <h1>getRegionName</h1>
     *
     * @return Returns the region name
     */
    public String getRegionName() {
        return regionName;
    }

    /**
     * <h1>getValidFromInRequestedRange</h1>
     *
     * @return Returns the valid from date.
     */
    public LocalDate getValidFromInRequestedRange() {
        return validFromInRequestedRange;
    }

    /**
     * <h1>setValidFromInRequestedRange</h1>
     * <p>
     * Simple setter to update the from date on the object.
     *
     * @param validFromInRequestedRange This parameter is date from the classification query.
     */
    public void setValidFromInRequestedRange(LocalDate validFromInRequestedRange) {
        this.validFromInRequestedRange = validFromInRequestedRange;
    }

    /**
     * <h1>getValidToInRequestedRange</h1>
     *
     * @return Returs the valid to date.
     */
    public LocalDate getValidToInRequestedRange() {
        return validToInRequestedRange;
    }

    /**
     * <h1>setValidToInRequestedRange</h1>
     *
     * @param validToInRequestedRange This parameter is date from the classification query.
     */
    public void setValidToInRequestedRange(LocalDate validToInRequestedRange) {
        this.validToInRequestedRange = validToInRequestedRange;
    }

    /**
     * <h1>getFromYear</h1>
     *
     * @return Returns the year from {@link #validFromInRequestedRange}
     */

    public int getFromYear() {
        return validFromInRequestedRange.getYear();
    }

    /**
     * <h1>getToYear</h1>
     *
     * @return Returns the year from {@link #validToInRequestedRange}
     */

    public int getToYear() {
        return validToInRequestedRange.getYear();
    }
}
