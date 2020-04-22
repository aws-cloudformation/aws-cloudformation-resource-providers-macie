package software.amazon.macie.session;

import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.cloudformation.LambdaWrapper;

class ClientBuilder {
    static Macie2Client getClient() {
        return Macie2Client.builder()
            .httpClient(LambdaWrapper.HTTP_CLIENT)
            .build();
    }
}
