package software.amazon.macie.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.SdkResponse;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.macie2.model.DisableMacieRequest;
import software.amazon.awssdk.services.macie2.model.DisableMacieResponse;
import software.amazon.awssdk.services.macie2.model.GetMacieSessionRequest;
import software.amazon.awssdk.services.macie2.model.GetMacieSessionResponse;
import software.amazon.awssdk.services.macie2.model.Macie2Exception;
import software.amazon.awssdk.services.macie2.model.MacieStatus;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest {

    private static final String MACIE_NOT_ENABLED_MESSAGE
        = "An error occurred (AccessDeniedException) when calling the GetMacieSession operation: Macie is not enabled";
    private static final String INTERNAL_ERROR_MESSAGE = "Internal Error";

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        SdkResponse getMacieSessionResponse = GetMacieSessionResponse.builder()
            .status(MacieStatus.ENABLED)
            .sdkHttpResponse(SdkHttpResponse.builder().statusCode(200).build())
            .build();
        SdkResponse disableMacieResponse = DisableMacieResponse.builder()
            .sdkHttpResponse(SdkHttpResponse.builder().statusCode(200).build())
            .build();
        doReturn(getMacieSessionResponse).when(proxy).injectCredentialsAndInvokeV2(any(GetMacieSessionRequest.class), any());
        doReturn(disableMacieResponse).when(proxy).injectCredentialsAndInvokeV2(any(DisableMacieRequest.class), any());

        final DeleteHandler handler = new DeleteHandler();
        final ResourceModel model = ResourceModel.builder().build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();
        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_NotFound() {
        AwsServiceException macieNotEnabledException = Macie2Exception.builder().statusCode(403).message(MACIE_NOT_ENABLED_MESSAGE).build();
        doThrow(macieNotEnabledException).when(proxy).injectCredentialsAndInvokeV2(any(GetMacieSessionRequest.class), any());

        final DeleteHandler handler = new DeleteHandler();
        final ResourceModel model = ResourceModel.builder().build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();
        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isEqualTo(MACIE_NOT_ENABLED_MESSAGE);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
    }

    @Test
    public void handleRequest_ServiceException() {
        AwsServiceException awsServiceException = AwsServiceException.builder().statusCode(500).message(INTERNAL_ERROR_MESSAGE).build();
        doThrow(awsServiceException).when(proxy).injectCredentialsAndInvokeV2(any(GetMacieSessionRequest.class), any());

        final DeleteHandler handler = new DeleteHandler();
        final ResourceModel model = ResourceModel.builder().build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        Exception exception = assertThrows(AwsServiceException.class, () -> handler.handleRequest(proxy, request, null, logger));
        assertTrue(exception.getMessage().contains(INTERNAL_ERROR_MESSAGE));
    }

    @Test
    public void handleRequest_MacieException() {
        AwsServiceException awsServiceException = Macie2Exception.builder().statusCode(500).message(INTERNAL_ERROR_MESSAGE).build();
        doThrow(awsServiceException).when(proxy).injectCredentialsAndInvokeV2(any(GetMacieSessionRequest.class), any());

        final DeleteHandler handler = new DeleteHandler();
        final ResourceModel model = ResourceModel.builder().build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        Exception exception = assertThrows(Macie2Exception.class, () -> handler.handleRequest(proxy, request, null, logger));
        assertTrue(exception.getMessage().contains(INTERNAL_ERROR_MESSAGE));
    }
}
