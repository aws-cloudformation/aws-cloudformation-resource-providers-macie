package software.amazon.macie.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.FindingPublishingFrequency;
import software.amazon.awssdk.services.macie2.model.GetMacieSessionRequest;
import software.amazon.awssdk.services.macie2.model.GetMacieSessionResponse;
import software.amazon.awssdk.services.macie2.model.Macie2Exception;
import software.amazon.awssdk.services.macie2.model.MacieStatus;
import software.amazon.awssdk.services.macie2.model.UpdateMacieSessionRequest;
import software.amazon.awssdk.services.macie2.model.UpdateMacieSessionResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest {

    protected static final Credentials MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
    private static final String MACIE_NOT_ENABLED_MESSAGE
        = "An error occurred (AccessDeniedException) when calling the GetMacieSession operation: Macie is not enabled";
    private static final String MACIE_NOT_ENABLED_CODE = "403";
    private static final String SERVICE_ROLE = "arn:%s:iam::%s:role/SERVICE-ROLE-NAME";
    private static final String TEST_ACCOUNT_ID = "999999999999";
    private static final String TEST_AWS_PARTITION = "aws";

    @Mock
    private ProxyClient<Macie2Client> proxyMacie2Client;

    @Mock
    private Macie2Client macie2;

    private AmazonWebServicesClientProxy proxy;
    private LoggerProxy logger;
    private UpdateHandler handler;

    @BeforeEach
    public void setup() {
        handler = new UpdateHandler();
        macie2 = mock(Macie2Client.class);
        logger = new LoggerProxy();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyMacie2Client = new ProxyClient<Macie2Client>() {
            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse>
            ResponseT
            injectCredentialsAndInvokeV2(RequestT request, Function<RequestT, ResponseT> requestFunction) {
                return proxy.injectCredentialsAndInvokeV2(request, requestFunction);
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse>
            CompletableFuture<ResponseT>
            injectCredentialsAndInvokeV2Aync(RequestT request,
                Function<RequestT, CompletableFuture<ResponseT>> requestFunction) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Macie2Client client() {
                return macie2;
            }
        };
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        GetMacieSessionResponse getMacieSessionResponse = GetMacieSessionResponse.builder()
            .status(MacieStatus.PAUSED)
            .findingPublishingFrequency(FindingPublishingFrequency.SIX_HOURS)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .serviceRole(String.format(SERVICE_ROLE, TEST_AWS_PARTITION, TEST_ACCOUNT_ID))
            .build();
        when(macie2.getMacieSession(any(GetMacieSessionRequest.class))).thenReturn(getMacieSessionResponse);
        when(macie2.updateMacieSession(any(UpdateMacieSessionRequest.class))).thenReturn(UpdateMacieSessionResponse.builder().build());

        final ResourceModel desiredModel = ResourceModel.builder()
            .status("PAUSED")
            .serviceRole(String.format(SERVICE_ROLE, TEST_AWS_PARTITION, TEST_ACCOUNT_ID))
            .findingPublishingFrequency("SIX_HOURS")
            .build();

        final ResourceModel model = ResourceModel.builder()
            .status("PAUSED")
            .findingPublishingFrequency("SIX_HOURS")
            .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();
        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, new CallbackContext(), proxyMacie2Client, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(desiredModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_NotFound() {
        AwsServiceException macieNotEnabledException = Macie2Exception.builder()
            .statusCode(403)
            .awsErrorDetails(AwsErrorDetails.builder()
                .errorCode(MACIE_NOT_ENABLED_CODE)
                .errorMessage(MACIE_NOT_ENABLED_MESSAGE)
                .sdkHttpResponse(SdkHttpResponse.builder().statusCode(Integer.parseInt(MACIE_NOT_ENABLED_CODE)).build())
                .build()
            )
            .build();
        when(macie2.getMacieSession(any(GetMacieSessionRequest.class))).thenThrow(macieNotEnabledException);

        final ResourceModel model = ResourceModel.builder().build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();
        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, new CallbackContext(), proxyMacie2Client, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getMessage()).contains(MACIE_NOT_ENABLED_MESSAGE);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
    }
}
