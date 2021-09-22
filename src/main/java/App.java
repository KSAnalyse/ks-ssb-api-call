import no.ks.fiks.ssbAPI.APIService.SsbApiCall;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException {
        new SsbApiCall("12367").metadataApiCall();
    }
}
