package no.ks.fiks.ssbAPI.APIService;

import no.ks.fiks.ssbAPI.builder.MetadataBuilder;
import no.ks.fiks.ssbAPI.klassApi.SsbKlass;
import no.ks.fiks.ssbAPI.metadataApi.SsbMetadata;
import no.ks.fiks.ssbAPI.metadataApi.SsbMetadataVariables;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * <h1>SSB API calls</h1>
 * <p>
 * SsbApiCall simplifies API calls to the ssb.no API. By supplying table number with the number of and calling {@link #tableApiCall()}
 * you will get a list back with the query results.
 * <p>
 * The SsbApiClass is where most of the calls happen, the other classes is mostly for organising, filtering and structuring
 * the data before calling tableApiCall to query the API.
 *
 * @author Hama Keli
 * @version 1.0.2
 * @since 2021-09-29
 */


public class SsbApiCall {

    private URL metadataUrl;
    private List<URL> klassListUrl;
    private SsbMetadata metadata;
    private SsbKlass klass;
    private int numberOfYears;
    private MetadataBuilder metadataBuilder;

    /**
     * <h1>SsbApiCall Constructor</h1>
     * The constructor gets two mandatory parameters and one var-args one.
     * It checks that table number is present before combining the metadata URL with the table number with the API address.
     * Then checks if classification codes was provided, if it was it will combine the url string with the code(s) provided
     * and the start year. It then adds the URL's to a List.
     * <p>
     * It will then try to run metadataApiCall and klassApiCall.
     *
     * @param metadataTableNumber This is the table number you want to query
     * @param numberOfYears       This is the number of years you wish to query for that table
     * @param classifications     This is the classification codes you wish to filter against.
     * @see #metadataApiCall()
     * @see #klassApiCall()
     */

    public SsbApiCall(String metadataTableNumber, int numberOfYears, String... classifications) {
        Optional<String> metadataTableNumberCheckNull = Optional.ofNullable(metadataTableNumber);

        this.numberOfYears = numberOfYears;
        try {
            String urlMetadata = "https://data.ssb.no/api/v0/no/table/";
            if (metadataTableNumberCheckNull.isPresent())
                this.metadataUrl = new URL(urlMetadata + metadataTableNumber);

            if (classifications.length != 0) {
                int urlKlassYear;
                if (numberOfYears > 0)
                    urlKlassYear = Calendar.getInstance().get(Calendar.YEAR) - numberOfYears;
                else
                    urlKlassYear = 1000;
                klassListUrl = new ArrayList<>();
                for (String klassNumber : classifications) {
                    String urlKlassStart = "https://data.ssb.no/api/klass/v1/classifications/";
                    String urlKlassMiddle = "/codes.json?from=";
                    String urlKlassEnd = "-01-01&to=2059-01-01&includeFuture=true";
                    URL klass = new URL(urlKlassStart + klassNumber + urlKlassMiddle + urlKlassYear + urlKlassEnd);
                    klassListUrl.add(klass);
                }
            }
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        }
        try {
            if (classifications.length != 0) {
                klassApiCall();
            }
            metadataApiCall();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * <h1>metadataApiCall</h1>
     * This method creates a SsbMetadata object by calling {@link #apiCall(String, URL, String, int)}.
     *
     * @throws IOException Throws IOException if apiCall encounters an error when querying.
     * @see #apiCall(String, URL, String, int)
     */

    private void metadataApiCall() throws IOException {
        metadata = new SsbMetadata(apiCall("metadata", metadataUrl, "", 0));
        this.metadataBuilder = new MetadataBuilder(metadata, klass);
        buildMetadata();
    }

    /**
     * <h1>metadataApiCall</h1>
     * <p>
     * This method updates the metadata URL and creates a SsbMetadata object.
     *
     * @param tableNumber This is the table number you want to query.
     * @throws IOException Throws IOException if apiCall encounters an error when querying.
     * @see #apiCall(String, URL, String, int)
     */

    public void metadataApiCall(String tableNumber) throws IOException {
        metadataUrl = new URL("https://data.ssb.no/api/v0/no/table/" + tableNumber);
        metadata = new SsbMetadata(apiCall("metadata", metadataUrl, "", 0));
        this.metadataBuilder = new MetadataBuilder(metadata, klass);
        buildMetadata();
    }

    /**
     * <h1>metadataApiCall</h1>
     * <p>
     * This method updates the metadata URL and creates a SsbMetadata object with a metadata filter.
     *
     * @param tableNumber    This is the table number you want to query.
     * @param metadataFilter This is a Map of filters for the metadata.
     * @param removeAllBut   This a boolean to determine if you want to remove the elements in the filters or only keep those elements.
     * @throws IOException Throws IOException if apiCall encounters an error when querying.
     * @see #apiCall(String, URL, String, int)
     */

    public void metadataApiCall(String tableNumber, Map<String, List<String>> metadataFilter, boolean removeAllBut) throws IOException {
        metadataUrl = new URL("https://data.ssb.no/api/v0/no/table/" + tableNumber);
        metadata = new SsbMetadata(apiCall("metadata", metadataUrl, "", 0), metadataFilter, removeAllBut);
        this.metadataBuilder = new MetadataBuilder(metadata, klass);
        buildMetadata();
    }

    /**
     * <h1>metadataApiCall</h1>
     * <p>
     * This method creates a SsbMetadata object with metadata filter.
     *
     * @param metadataFilter This is a Map of filters for the metadata.
     * @param removeAllBut   This a boolean to determine if you want to remove the elements in the filters or only keep those elements.
     * @throws IOException Throws IOException if apiCall encounters an error when querying.
     * @see #apiCall(String, URL, String, int)
     */
    public void metadataApiCall(Map<String, List<String>> metadataFilter, boolean removeAllBut) throws IOException {
        metadata = new SsbMetadata(apiCall("metadata", metadataUrl, "", 0), metadataFilter, removeAllBut);
        this.metadataBuilder = new MetadataBuilder(metadata, klass);
        buildMetadata();
    }

    private void buildMetadata() {
        if (numberOfYears > 0) {
            filterYears();
        }
        metadataBuilder.buildMetadata();

    }

    /**
     * <h1>klassApiCall</h1>
     * <p>
     * This method creates a SsbKlass object and adds the query result to a List. It then calls on convertStringToJson with the created List.
     *
     * @throws IOException Throws IOException if apiCall encounters an error when querying.
     * @see #apiCall(String, URL, String, int)
     */

    private void klassApiCall() throws IOException {
        List<String> klassResult = new ArrayList<>();
        for (URL url : klassListUrl) {
            klassResult.add(apiCall("klass", url, "", 0));
        }
        klass = new SsbKlass();
        klass.convertStringToJson(klassResult);
    }


    /**
     * <h1>tableApiCall</h1>
     * <p>
     * This method first checks if year is bigger than zero, so it can filter the 'Tid' metadata.
     * Then it creates a MetadataBuilder object, then a Map is populated with List of SsbMetadataVariables.
     * It then runs queryBuilder to build the query in a structure the API will accept and returns the list of results.
     *
     * @return Returns a List of the query results.
     * @throws IOException Throws IOException if apiCall encounters an error when querying.
     * @see MetadataBuilder
     * @see #filterYears()
     */

    public List<String> tableApiCall() throws IOException {
        Map<Integer, List<SsbMetadataVariables>> filteredMetadata = metadataBuilder.getBuiltMetadata();
        List<String> queryList = new ArrayList<>();

        for (int key : filteredMetadata.keySet()) {
            queryBuilder(filteredMetadata, queryList, key);
        }
        return queryList;
    }

    /**
     * <h1>apiCall</h1>
     * <p>
     * This method handles all the API calls to the two SSB API's. Depending on what methodCall it does different things.
     *
     * @param methodCall This String says which API it queries and how.
     * @param url        This is the URL it will query.
     * @param query      This is the query String for when used to query a table.
     * @return Returns the result of the API calls.
     * @throws IOException Throws IOException if apiCall encounters an error when querying.
     * @see #handleResponseCodeErrors(HttpURLConnection, int)
     */

    private String apiCall(String methodCall, URL url, String query, int tries) throws IOException {

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");
        if (methodCall.equals("klass") || methodCall.equals("metadata")) {
            connection.setRequestMethod("GET");
            connection.connect();
        } else if (methodCall.equals("table")) {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = query.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            if (!handleResponseCodeErrors(connection, tries)) {
                tries++;
                return apiCall(methodCall, url, query, tries);
            }
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            }

        }
        return IOUtils.toString(url.openStream());
    }

    /**
     * <h1>handleResponseCodeErrors</h1>
     * <p>
     * This method handles the different http response codes we might get. If the error is caused from something on our side
     * it will throw an IOException. If the error is Server-side it will retry after X-Seconds, depending on which error we get.
     *
     * @param connection This is the connection to the API.
     * @return Returns false if it failed because of an error from SSB. Returns true if no errors.
     * @throws IOException Throws IOException if HttpUrlConnection encounters an error when querying.
     */

    private boolean handleResponseCodeErrors(HttpURLConnection connection, int tries) throws IOException {
        if (tries > 5)
            throw new ConnectException("Tried five times, SSB might be down, error: " + connection.getResponseCode());
        if (connection.getResponseCode() == 403)
            throw new IOException("Query is too big, please submit a bug report on git project. " + connection.getResponseCode());
        else if (connection.getResponseCode() == 404)
            throw new IOException("Either wrong url (check that table exists) or syntax error on the query. If table exists, submit bug report. " + connection.getResponseCode());
        else if (connection.getResponseCode() == 429) {
            System.err.println("Too many queries... retrying..." + connection.getResponseCode());
            retryQuery(5000);
            return false;
        } else if (connection.getResponseCode() == 503) {
            System.err.println("Timeout from server, sleeping for 60 seconds and retrying" + connection.getResponseCode());
            retryQuery(60000);
            return false;
        } else if (connection.getResponseCode() == 400) {
            System.err.println("Bad request, retrying...");
            retryQuery(5000);
            return false;
        }
        return true;
    }

    /**
     * <h1>retryQuery</h1>
     * <p>
     * Simple method that waits based on waitTimer.
     *
     * @param waitTimer This int determines how long the thread should wait.
     */
    private void retryQuery(int waitTimer) {
        try {
            Thread.sleep(waitTimer);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * <h1>queryBuilder</h1>
     * <p>
     * This method builds the query based on the metadata provided from Metadata builder.
     *
     * @param filteredMetadata This is a Map of Lists that has the metadata which it builds the query from.
     * @param queryList        This is the List of query results.
     * @param key              This is the Map key.
     * @throws IOException Throws IOException if apiCall encounters an error when querying.
     */

    private void queryBuilder(Map<Integer, List<SsbMetadataVariables>> filteredMetadata, List<String> queryList, int key) throws IOException {
        StringBuilder queryTwo = new StringBuilder();
        for (SsbMetadataVariables metadataVariables : filteredMetadata.get(key)) {
            queryTwo.append(buildString(metadataVariables));
        }
        queryTwo = new StringBuilder(queryTwo.substring(0, queryTwo.length() - 1));
        String queryOne = "{\"query\": [";
        String queryThree = "],\"response\": {\"format\": \"json-stat2\"}}";
        String query = queryOne + queryTwo + queryThree;
        queryList.add(apiCall("table", metadataUrl, query, 0));
    }

    /**
     * <h1>buildString</h1>
     * <p>
     * This is a helper method of queryBuilder that builds the code section of the query correctly as a String before returning it to queryBuilder.
     * It builds the whole code section for all the variables in the metadata.
     *
     * @param ssbMetadataVariables This is object of SsbMetadataVariables.
     * @return Returns the finished 'code' section of the query.
     */

    private String buildString(SsbMetadataVariables ssbMetadataVariables) {
        StringBuilder values = new StringBuilder();
        for (String s : ssbMetadataVariables.getValues()) {
            values.append("\"").append(s).append("\", ");
        }
        values = new StringBuilder(values.substring(0, values.length() - 2));
        return "{ \"code\": \"" + ssbMetadataVariables.getCode() + "\", \"selection\": { \"filter\": \"item\", \"values\": [" + values + "]}},";
    }

    /**
     * <h1>filterYears</h1>
     * <p>
     * This method removes all years/quarters/months if numberOfYears is bigger than 0.
     */
    private void filterYears() {
        for (SsbMetadataVariables metadataVariables : metadata.getVariables()) {
            if (metadataVariables.getCode().equals("Tid")) {
                if (metadataVariables.getValues().size() < numberOfYears)
                    numberOfYears = metadataVariables.getValues().size();

                if (metadataVariables.getValues().get(0).length() > 4)
                    if (metadataVariables.getText().equalsIgnoreCase("kvartal"))
                        numberOfYears = numberOfYears * 4;
                    else if (metadataVariables.getText().equalsIgnoreCase("måned"))
                        numberOfYears = numberOfYears * 12;
                metadataVariables.getValues().subList(0, metadataVariables.getValues().size() - numberOfYears).clear();
            }
        }
    }

    /**
     * <h1>getMetadata</h1>
     *
     * @return Returns metadata object.
     */

    public SsbMetadata getMetadata() {
        return metadata;
    }

    /**
     * <h1>getKlass</h1>
     *
     * @return Returns klass object.
     */
    public SsbKlass getKlass() {
        return klass;
    }

    public int getQuerySize() {
        return metadataBuilder.getBuiltMetadata().size();
    }
}
