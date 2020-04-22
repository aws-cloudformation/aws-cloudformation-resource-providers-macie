package software.amazon.macie.session;

import org.mockito.ArgumentMatchers;
import software.amazon.awssdk.services.macie2.model.GetMacieSessionRequest;
import software.amazon.awssdk.services.macie2.model.GetMacieSessionResponse;
import software.amazon.awssdk.services.macie2.model.Macie2Exception;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest {

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
        final ReadHandler handler = new ReadHandler();

        final GetMacieSessionResponse getResponse = GetMacieSessionResponse.builder()
                                                                          .status("ENABLED")
                                                                          .findingPublishingFrequency("SIX_HOURS")
                                                                          .serviceRole("arn:aws:iam::account-id:role/AmazonMacieRole")
                                                                          .createdAt(Instant.ofEpochSecond(1587543212))
                                                                          .updatedAt(Instant.ofEpochSecond(1587543212))
                                                                          .build();

        doReturn(getResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any()
                );

        final ResourceModel model = ResourceModel.builder()
                                                .status("ENABLED")
                                                .findingPublishingFrequency("SIX_HOURS")
                                                .serviceRole("arn:aws:iam::account-id:role/AmazonMacieRole")
                                                .createdAt("2020-04-22T08:13:32Z")
                                                .updatedAt("2020-04-22T08:13:32Z")
                                                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                   .desiredResourceState(model)
                                                                                   .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_AccessDenied() {
        final ReadHandler handler = new ReadHandler();

        doThrow(Macie2Exception.class)
               .when(proxy)
               .injectCredentialsAndInvokeV2(
                       ArgumentMatchers.any(),
                       ArgumentMatchers.any()
               );

        final ResourceModel model = ResourceModel.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                   .desiredResourceState(model)
                                                                                   .build();

        assertThrows(CfnAccessDeniedException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
    }
}
