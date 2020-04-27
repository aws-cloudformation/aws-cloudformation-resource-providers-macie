package software.amazon.macie.session;

import java.net.URI;
import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.cloudformation.LambdaWrapper;

class ClientBuilder {
    static Macie2Client getClient() {
        URI override = URI.create("https://macie2.prod.us-west-2.macie.amazonaws.com");
        return Macie2Client.builder()
            .endpointOverride(override)
            .httpClient(LambdaWrapper.HTTP_CLIENT)
            .build();
    }
}
