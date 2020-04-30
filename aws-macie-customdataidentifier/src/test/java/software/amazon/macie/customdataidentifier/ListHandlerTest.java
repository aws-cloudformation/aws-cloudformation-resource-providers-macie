package software.amazon.macie.customdataidentifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.macie2.model.CustomDataIdentifierSummary;
import software.amazon.awssdk.services.macie2.model.ListCustomDataIdentifiersRequest;
import software.amazon.awssdk.services.macie2.model.ListCustomDataIdentifiersResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest extends AbstractTestBase {

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
                .when(proxy)
                .injectCredentialsAndInvokeV2(ArgumentMatchers.any(ListCustomDataIdentifiersRequest.class),
                                              ArgumentMatchers.any());

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, null, logger);

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
