import no.ks.fiks.ssbAPI.APIService.SsbApiCall;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException {
        test();
    }

    private static void test() throws IOException {
        SsbApiCall test = new SsbApiCall("12367", "131", "104", "214", "231", "127");
        test.metadataApiCall();
        test.klassApiCall();
        test.tableApiCall();
    }
}
