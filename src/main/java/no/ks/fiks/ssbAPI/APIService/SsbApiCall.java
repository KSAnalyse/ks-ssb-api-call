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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SsbApiCall {
    private URL metadataUrl;
    private List<URL> klassListUrl;
    private SsbMetadata metadata;
    private SsbKlass klass;
    private final int numberOfYears;

    public SsbApiCall(String metadataTableNumber, int numberOfYears, String... classifications) {
        Optional<String> metadataTableNumberCheckNull = Optional.ofNullable(metadataTableNumber);
        this.numberOfYears = numberOfYears;
        try {
            String urlMetadata = "https://data.ssb.no/api/v0/no/table/";
            if (metadataTableNumberCheckNull.isPresent())
                this.metadataUrl = new URL(urlMetadata + metadataTableNumber);

            if (classifications.length != 0) {
                int urlKlassYear = Calendar.getInstance().get(Calendar.YEAR) - numberOfYears;
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
            metadataApiCall();
            if (classifications.length != 0) {
                klassApiCall();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void metadataApiCall() throws IOException {
        metadata = new SsbMetadata(apiCall("metadata", metadataUrl, ""));
    }

    public void metadataApiCall(String tableNumber) throws IOException {
        metadataUrl = new URL("https://data.ssb.no/api/v0/no/table/" + tableNumber);
        metadata = new SsbMetadata(apiCall("metadata", metadataUrl, ""));
    }

    public void metadataApiCall(String tableNumber, Map<String, List<String>> metadataFilter, boolean removeAllBut) throws IOException {
        metadataUrl = new URL("https://data.ssb.no/api/v0/no/table/" + tableNumber);
        metadata = new SsbMetadata(apiCall("metadata", metadataUrl, ""), metadataFilter, removeAllBut);

    }

    public void metadataApiCall(Map<String, List<String>> metadataFilter, boolean removeAllBut) throws IOException {
        metadata = new SsbMetadata(apiCall("metadata", metadataUrl, ""), metadataFilter, removeAllBut);

    }

    private void klassApiCall() throws IOException {
        List<String> klassResult = new ArrayList<>();
        for (URL url : klassListUrl) {
            klassResult.add(apiCall("klass", url, ""));
        }
        klass = new SsbKlass();
        klass.convertStringToJson(klassResult);
    }

    public List<String> tableApiCall() throws IOException {
        if (klass == null) {
            return klassUnfilteredSsbCall();
        } else {
            return ssbApiCall();
        }
    }

    private List<String> klassUnfilteredSsbCall() throws IOException {
        MetadataBuilder metadataBuilder = new MetadataBuilder(metadata, klass, numberOfYears);
        Map<Integer, List<SsbMetadataVariables>> filteredMetadata = metadataBuilder.buildMetadata();
        List<String> queryList = new ArrayList<>();
        for (int key : filteredMetadata.keySet()) {
            queryBuilder(filteredMetadata, queryList, key);
        }
        return queryList;
    }

    private List<String> ssbApiCall() throws IOException {
        MetadataBuilder metadataBuilder = new MetadataBuilder(metadata, klass, numberOfYears);
        Map<Integer, List<SsbMetadataVariables>> filteredMetadata = metadataBuilder.filterMetadata();
        List<String> queryList = new ArrayList<>();
        for (int key : filteredMetadata.keySet()) {
            queryBuilder(filteredMetadata, queryList, key);
        }
        return queryList;
    }

    private String apiCall(String methodCall, URL url, String query) throws IOException {

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
            if (!handleResponseCodeErrors(connection))
                return apiCall(methodCall, url, query);
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            }

        }
        return IOUtils.toString(url.openStream());
    }

    private boolean handleResponseCodeErrors(HttpURLConnection connection) throws IOException {
        if (connection.getResponseCode() == 403)
            throw new IOException("Query is too big, please submit a bug report on git project. " + connection.getResponseCode());
        else if (connection.getResponseCode() == 404)
            throw new IOException("Either wrong url (check that table exists) or syntax error on the query. If table exists, submit bug report. " + connection.getResponseCode());
        else if (connection.getResponseCode() == 429) {
            System.err.println("Too many queries... retrying...");
            retryQuery(5000);
            return false;
        } else if (connection.getResponseCode() == 503) {
            System.err.println("Timeout from server, sleeping for 60 seconds and retrying");
            retryQuery(60000);
            return false;
        }
        return true;
    }

    private void retryQuery(int waitTimer) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void queryBuilder(Map<Integer, List<SsbMetadataVariables>> filteredMetadata, List<String> queryList, int key) throws IOException {
        StringBuilder queryTwo = new StringBuilder();
        for (SsbMetadataVariables metadataVariables : filteredMetadata.get(key))
            queryTwo.append(buildString(metadataVariables));
        queryTwo = new StringBuilder(queryTwo.substring(0, queryTwo.length() - 1));
        String queryOne = "{\"query\": [";
        String queryThree = "],\"response\": {\"format\": \"json-stat2\"}}";
        String query = queryOne + queryTwo + queryThree;
        System.out.println(query);
        queryList.add(apiCall("table", metadataUrl, query));
    }

    private String buildString(SsbMetadataVariables test) {
        StringBuilder values = new StringBuilder();
        for (String s : test.getValues()) {
            values.append("\"").append(s).append("\", ");
        }
        values = new StringBuilder(values.substring(0, values.length() - 2));
        return "{ \"code\": \"" + test.getCode() + "\", \"selection\": { \"filter\": \"item\", \"values\": [" + values + "]}},";
    }

    public SsbMetadata getMetadata() {
        return metadata;
    }

    public SsbKlass getKlass() {
        return klass;
    }
}
