package software.amazon.macie.customdataidentifier;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.cloudformation.LambdaWrapper;

import java.net.URI;

public class ClientBuilder {
  public static Macie2Client getClient() {
    // TODO remove
    URI override = URI.create("https://macie2.prod.us-east-1.macie.amazonaws.com");
    return Macie2Client.builder()
                       .endpointOverride(override)
                       .httpClient(LambdaWrapper.HTTP_CLIENT)
                       .build();
    /*
    return Macie2Client.builder()
                       .httpClient(LambdaWrapper.HTTP_CLIENT)
                       .build();
    */
  }
}
