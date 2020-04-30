package software.amazon.macie.customdataidentifier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.CreateCustomDataIdentifierRequest;
import software.amazon.awssdk.services.macie2.model.CreateCustomDataIdentifierResponse;
import software.amazon.awssdk.services.macie2.model.GetCustomDataIdentifierRequest;
import software.amazon.awssdk.services.macie2.model.GetCustomDataIdentifierResponse;
import software.amazon.awssdk.services.macie2.model.Macie2Exception;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<Macie2Client> proxyClient;

    @Mock
    Macie2Client sdkClient;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(Macie2Client.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
    }

    @Test
    public void handleRequest_whenCreateSucceeds_thenCreateResource() {
        final CreateHandler handler = new CreateHandler();

        final ResourceModel model
                = ResourceModel.builder()
                               .id(CUSTOM_DATA_IDENTIFIER_ID)
                               .name(CUSTOM_DATA_IDENTIFIER_NAME)
                               .regex(CUSTOM_DATA_IDENTIFIER_REGEX)
                               .keywords(new ArrayList<>())
                               .ignoreWords(new ArrayList<>())
                               .build();

        final ResourceHandlerRequest<ResourceModel> request
                = ResourceHandlerRequest.<ResourceModel>builder()
                                        .clientRequestToken(CLIENT_TOKEN)
                                        .desiredResourceState(model)
                                        .build();

        doReturn(CreateCustomDataIdentifierResponse.builder().customDataIdentifierId(CUSTOM_DATA_IDENTIFIER_ID).build())
                .when(sdkClient)
                .createCustomDataIdentifier(ArgumentMatchers.any(CreateCustomDataIdentifierRequest.class));

        doReturn(GetCustomDataIdentifierResponse.builder()
                                               .id(CUSTOM_DATA_IDENTIFIER_ID)
                                               .name(model.getName())
                                               .regex(model.getRegex())
                                               .build())
                 .when(sdkClient)
                 .getCustomDataIdentifier(ArgumentMatchers.any(GetCustomDataIdentifierRequest.class));

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_whenCreateFails_thenThrowGeneralException() {
        final CreateHandler handler = new CreateHandler();

        final ResourceModel model
                = ResourceModel.builder()
                               .id(CUSTOM_DATA_IDENTIFIER_ID)
                               .name(CUSTOM_DATA_IDENTIFIER_NAME)
                               .regex(CUSTOM_DATA_IDENTIFIER_REGEX)
                               .build();

        final ResourceHandlerRequest<ResourceModel> request
                = ResourceHandlerRequest.<ResourceModel>builder()
                                        .clientRequestToken(CLIENT_TOKEN)
                                        .desiredResourceState(model)
                                        .build();

        doThrow(Macie2Exception.class)
               .when(sdkClient)
               .createCustomDataIdentifier(ArgumentMatchers.any(CreateCustomDataIdentifierRequest.class));

        Assertions.assertThrows(CfnGeneralServiceException.class,
                                () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    }
}
