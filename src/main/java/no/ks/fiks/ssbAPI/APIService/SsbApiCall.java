package no.ks.fiks.ssbAPI.APIService;

import no.ks.fiks.ssbAPI.klassApi.SsbKlass;
import no.ks.fiks.ssbAPI.metadataApi.SsbMetadata;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

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
        metadata = new SsbMetadata(apiCall("metadata", metadataUrl));
    }

    public void klassApiCall() throws IOException {
        List<String> klassResult = new ArrayList<>();
        for (URL url : klassListUrl) {
            klassResult.add(apiCall("klass", url));
        }
        klass = new SsbKlass(klassResult);
    }

    public void ssbApiCall() throws IOException {

    }

    private String apiCall(String methodCall, URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        if (methodCall.equals("klass")) {
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod("GET");
        } else if (methodCall.equals("metadata")) {
            connection.setRequestMethod("GET");
        }
        connection.connect();
        if (!responseCode(connection.getResponseCode())) {
            return "Response code: " + connection.getResponseCode();
        }
        return IOUtils.toString(url.openStream());
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
