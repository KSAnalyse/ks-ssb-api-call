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

    public SsbApiCall(String metadataTableNumber, String... classifications) {
        Optional<String> metadataTableNumberCheckNull = Optional.ofNullable(metadataTableNumber);

        try {
            String urlMetadata = "https://data.ssb.no/api/v0/no/table/";
            if (metadataTableNumberCheckNull.isPresent())
                this.metadataUrl = new URL(urlMetadata + metadataTableNumber);

            if (classifications != null) {
                int urlKlassYear = Calendar.getInstance().get(Calendar.YEAR) - 5;
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
    }

    public void metadataApiCall() throws IOException {
        metadata = new SsbMetadata(apiCall("metadata", metadataUrl, ""));
    }

    public void metadataApiCall(String tableNumber) throws IOException {
        metadataUrl = new URL("https://data.ssb.no/api/v0/no/table/" + tableNumber);
        metadata = new SsbMetadata(apiCall("metadata", metadataUrl, ""));
    }

    public void klassApiCall() throws IOException {
        List<String> klassResult = new ArrayList<>();
        for (URL url : klassListUrl) {
            klassResult.add(apiCall("klass", url, ""));
        }
        klass = new SsbKlass();
        klass.convertStringToJson(klassResult);
    }

    public List<String> tableApiCall() throws IOException {
        MetadataBuilder metadataBuilder = new MetadataBuilder(metadata, klass);
        Map<Integer, List<SsbMetadataVariables>> filteredMetadata = metadataBuilder.filterMetadata();
        List<String> queryList = new ArrayList<>();

        for (int key : filteredMetadata.keySet()) {
            StringBuilder queryTwo = new StringBuilder();
            for (SsbMetadataVariables metadataVariables : filteredMetadata.get(key))
                queryTwo.append(buildString(metadataVariables));
            queryTwo = new StringBuilder(queryTwo.substring(0, queryTwo.length() - 1));
            String queryOne = "{\"query\": [";
            String queryThree = "],\"response\": {\"format\": \"json-stat2\"}}";
            String query = queryOne + queryTwo + queryThree;
            queryList.add(apiCall("table", metadataUrl, query));
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
        } else if (methodCall.equals("table")) {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = query.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
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
        if (!responseCode(connection.getResponseCode())) {
            return "Response code: " + connection.getResponseCode();
        }
        return IOUtils.toString(url.openStream());
    }

    private String buildString(SsbMetadataVariables test) {
        StringBuilder values = new StringBuilder();
        for (String s : test.getValues()) {
            values.append("\"").append(s).append("\", ");
        }
        values = new StringBuilder(values.substring(0, values.length() - 2));
        return "{ \"code\": \"" + test.getCode() + "\", \"selection\": { \"filter\": \"item\", \"values\": [" + values + "]}},";
    }

    private boolean responseCode(int code) {
        return code == 200;
    }

    public SsbMetadata getMetadata() {
        return metadata;
    }

    public SsbKlass getKlass() {
        return klass;
    }
}
