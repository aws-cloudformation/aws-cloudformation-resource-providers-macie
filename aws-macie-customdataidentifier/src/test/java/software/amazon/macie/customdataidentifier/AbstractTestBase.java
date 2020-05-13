package software.amazon.macie.customdataidentifier;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

public class AbstractTestBase {
  protected static final Credentials MOCK_CREDENTIALS;
  protected static final LoggerProxy logger;

  protected static final String CUSTOM_DATA_IDENTIFIER_ID = "1234";
  protected static final String CUSTOM_DATA_IDENTIFIER_ARN = "arn:aws:123";
  protected static final String CUSTOM_DATA_IDENTIFIER_NAME = "MyCustomDataIdentifier";
  protected static final String CUSTOM_DATA_IDENTIFIER_DESCRIPTION = "My custom data identifier";
  protected static final String CUSTOM_DATA_IDENTIFIER_REGEX = "[0-9]{12}";
  protected static final String CUSTOM_DATA_IDENTIFIER_CREATED_AT = "now";
  protected static final long CUSTOM_DATA_IDENTIFIER_CREATED_AT_LONG = 1588181942;
  protected static final String CLIENT_TOKEN = "abcd";

  static {
    MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
    logger = new LoggerProxy();
  }
  static ProxyClient<Macie2Client> MOCK_PROXY(
    final AmazonWebServicesClientProxy proxy,
    final Macie2Client sdkClient
  ) {
    return new ProxyClient<Macie2Client>() {
      @Override
      public <RequestT extends AwsRequest, ResponseT extends AwsResponse>
      ResponseT
      injectCredentialsAndInvokeV2(RequestT request, Function<RequestT, ResponseT> requestFunction) {
        return proxy.injectCredentialsAndInvokeV2(request, requestFunction);
      }

      @Override
      public <RequestT extends AwsRequest, ResponseT extends AwsResponse>
      CompletableFuture<ResponseT>
      injectCredentialsAndInvokeV2Async(RequestT request, Function<RequestT, CompletableFuture<ResponseT>> requestFunction) {
        throw new UnsupportedOperationException();
      }

      @Override
      public <RequestT extends AwsRequest, ResponseT extends AwsResponse, IterableT extends SdkIterable<ResponseT>>
      IterableT
      injectCredentialsAndInvokeIterableV2(RequestT request, Function<RequestT, IterableT> requestFunction) {
        return proxy.injectCredentialsAndInvokeIterableV2(request, requestFunction);
      }

      @Override
      public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseInputStream<ResponseT> injectCredentialsAndInvokeV2InputStream(
          RequestT requestT, Function<RequestT, ResponseInputStream<ResponseT>> function) {
        throw new UnsupportedOperationException();
      }

      @Override
      public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseBytes<ResponseT> injectCredentialsAndInvokeV2Bytes(RequestT requestT,
          Function<RequestT, ResponseBytes<ResponseT>> function) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Macie2Client client() {
        return sdkClient;
      }
    };
  }
}
