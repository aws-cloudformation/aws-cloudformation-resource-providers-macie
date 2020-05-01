package software.amazon.macie.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static software.amazon.macie.session.BaseMacieSessionHandler.MACIE_NOT_ENABLED;
import static software.amazon.macie.session.BaseMacieSessionHandler.MACIE_NOT_ENABLED_EXPECTED_MESSAGE;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.DisableMacieRequest;
import software.amazon.awssdk.services.macie2.model.DisableMacieResponse;
import software.amazon.awssdk.services.macie2.model.Macie2Exception;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest {
    private static final Credentials MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
    private static final String MACIE_NOT_ENABLED_CODE = "403";

    @Mock
    private ProxyClient<Macie2Client> proxyMacie2Client;

    @Mock
    private Macie2Client macie2;

    private AmazonWebServicesClientProxy proxy;
    private LoggerProxy logger;
    private DeleteHandler handler;

    @BeforeEach
    public void setup() {
        handler = new DeleteHandler();
        logger = new LoggerProxy();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        when(proxyMacie2Client.client()).thenReturn(macie2);
        when(proxyMacie2Client.injectCredentialsAndInvokeV2(any(DisableMacieRequest.class), any())).thenReturn(DisableMacieResponse.builder().build());

        final ResourceModel model = ResourceModel.builder().build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();
        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, new CallbackContext(), proxyMacie2Client, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_NotFound() {
        AwsServiceException macieNotEnabledException = Macie2Exception.builder()
            .message(MACIE_NOT_ENABLED)
            .awsErrorDetails(AwsErrorDetails.builder()
                .errorCode(MACIE_NOT_ENABLED_CODE)
                .errorMessage(MACIE_NOT_ENABLED)
                .sdkHttpResponse(SdkHttpResponse.builder().statusCode(Integer.parseInt(MACIE_NOT_ENABLED_CODE)).build())
                .build()
            )
            .build();
        when(proxyMacie2Client.client()).thenReturn(macie2);
        when(proxyMacie2Client.injectCredentialsAndInvokeV2(any(DisableMacieRequest.class), any())).thenThrow(macieNotEnabledException);

        final ResourceModel model = ResourceModel.builder().build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();
        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, new CallbackContext(), proxyMacie2Client, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getMessage()).contains(String.format(MACIE_NOT_ENABLED_EXPECTED_MESSAGE, ResourceModel.TYPE_NAME, model.getAwsAccountId()));
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
    }
}
