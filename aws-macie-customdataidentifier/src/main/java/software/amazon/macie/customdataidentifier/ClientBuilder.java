package software.amazon.macie.customdataidentifier;

import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.cloudformation.LambdaWrapper;

public class ClientBuilder {
  public static Macie2Client getClient() {
    return Macie2Client.builder()
                       .httpClient(LambdaWrapper.HTTP_CLIENT)
                       .build();
  }
}
