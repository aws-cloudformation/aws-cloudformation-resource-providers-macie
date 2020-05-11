package software.amazon.macie.customdataidentifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.CustomDataIdentifierSummary;
import software.amazon.awssdk.services.macie2.model.ListCustomDataIdentifiersRequest;
import software.amazon.awssdk.services.macie2.model.ListCustomDataIdentifiersResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    Macie2Client sdkClient;

    @Mock
    private ProxyClient<Macie2Client> proxyClient;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(Macie2Client.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
    }

    @Test
    public void handleRequest_whenListSucceeds_thenListResources() {
        final ListHandler handler = new ListHandler();

        final ResourceModel model = ResourceModel.builder()
                                                .id(CUSTOM_DATA_IDENTIFIER_ID)
                                                .arn(CUSTOM_DATA_IDENTIFIER_ARN)
                                                .name(CUSTOM_DATA_IDENTIFIER_NAME)
                                                .description(CUSTOM_DATA_IDENTIFIER_DESCRIPTION)
                                                .createdAt(CUSTOM_DATA_IDENTIFIER_CREATED_AT)
                                                .build();

        final CustomDataIdentifierSummary summary = CustomDataIdentifierSummary.builder()
                                                                            .id(CUSTOM_DATA_IDENTIFIER_ID)
                                                                            .arn(CUSTOM_DATA_IDENTIFIER_ARN)
                                                                            .name(CUSTOM_DATA_IDENTIFIER_NAME)
                                                                            .description(CUSTOM_DATA_IDENTIFIER_DESCRIPTION)
                                                                            .createdAt(Instant.ofEpochSecond(CUSTOM_DATA_IDENTIFIER_CREATED_AT_LONG))
                                                                            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                   .desiredResourceState(model)
                                                                                   .build();

        doReturn(ListCustomDataIdentifiersResponse.builder().items(summary).build())
            .when(sdkClient)
            .listCustomDataIdentifiers(ArgumentMatchers.any(ListCustomDataIdentifiersRequest.class));

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getResourceModels().size()).isEqualTo(1);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
