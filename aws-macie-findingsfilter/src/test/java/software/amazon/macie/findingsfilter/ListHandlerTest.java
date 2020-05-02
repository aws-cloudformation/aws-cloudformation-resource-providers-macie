package software.amazon.macie.findingsfilter;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.FindingsFilterListItem;
import software.amazon.awssdk.services.macie2.model.ListFindingsFiltersRequest;
import software.amazon.awssdk.services.macie2.model.ListFindingsFiltersResponse;
import software.amazon.awssdk.services.macie2.model.Macie2Exception;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static software.amazon.macie.findingsfilter.BaseMacieFindingFilterHandler.MACIE_NOT_ENABLED;
import static software.amazon.macie.findingsfilter.ListHandler.OPERATION;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest {

    private static final Credentials MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
    private static final String FILTER_ID = "1b1a111a-1111-1ba1-ba1a-11111111bb11";
    private static final String FILTER_NAME = "findings_filter";
    private static final String NEXT_TOKEN = "next_token";
    private static final String MACIE_NOT_ENABLED_HTTP_STATUS_CODE = "403";
    private static final String ACCESS_DENIED_CFN_MESSAGE = "Access denied for operation '%s'.";

    private final ResourceModel model = ResourceModel.builder().build();
    @Mock
    private ProxyClient<Macie2Client> proxyMacie2Client;

    private AmazonWebServicesClientProxy proxy;
    @Mock
    private Macie2Client macie2;
    private LoggerProxy logger;
    private ListHandler handler;

    @BeforeEach
    public void setup() {
        handler = new ListHandler();
        logger = new LoggerProxy();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        ListFindingsFiltersResponse listFindingsFiltersResponse = ListFindingsFiltersResponse.builder()
            .findingsFilterListItems(ImmutableList.of(FindingsFilterListItem.builder()
                                                                           .name(FILTER_NAME)
                                                                           .filterId(FILTER_ID)
                                                                           .build()))
            .build();
        when(proxyMacie2Client.client()).thenReturn(macie2);
        when(proxyMacie2Client
            .injectCredentialsAndInvokeV2(any(ListFindingsFiltersRequest.class), any())).thenReturn(listFindingsFiltersResponse);
        final ResourceModel desiredOutputModel = ResourceModel.builder()
            .findingsFilterListItems(ImmutableList.of(software.amazon.macie.findingsfilter.FindingsFilterListItem.builder()
                .name(FILTER_NAME)
                .filterId(FILTER_ID)
                .build()))
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
        assertThat(response.getResourceModel()).isEqualTo(desiredOutputModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_SimpleSuccess_nextToken() {
        ListFindingsFiltersResponse listFindingsFiltersResponse = ListFindingsFiltersResponse.builder()
            .findingsFilterListItems(ImmutableList.of(FindingsFilterListItem.builder()
                                                                           .name(FILTER_NAME)
                                                                           .filterId(FILTER_ID)
                                                                           .build()))
            .nextToken(NEXT_TOKEN)
            .build();
        when(proxyMacie2Client.client()).thenReturn(macie2);
        when(proxyMacie2Client
            .injectCredentialsAndInvokeV2(any(ListFindingsFiltersRequest.class), any())).thenReturn(listFindingsFiltersResponse);
        final ResourceModel desiredOutputModel = ResourceModel.builder()
            .findingsFilterListItems(ImmutableList.of(software.amazon.macie.findingsfilter.FindingsFilterListItem.builder()
                .name(FILTER_NAME)
                .filterId(FILTER_ID)
                .build()))
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();
        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, new CallbackContext(), proxyMacie2Client, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getNextToken()).isEqualTo(NEXT_TOKEN);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(desiredOutputModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_SimpleSuccess_Empty() {
        ListFindingsFiltersResponse listFindingsFiltersResponse = ListFindingsFiltersResponse.builder()
            .findingsFilterListItems(ImmutableList.of()).build();
        when(proxyMacie2Client.client()).thenReturn(macie2);
        when(proxyMacie2Client
            .injectCredentialsAndInvokeV2(any(ListFindingsFiltersRequest.class), any())).thenReturn(listFindingsFiltersResponse);

        final ResourceModel desiredOutputModel = ResourceModel.builder()
            .findingsFilterListItems(ImmutableList.of()).build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();
        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, new CallbackContext(), proxyMacie2Client, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(desiredOutputModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_MacieNotEnabled_Fails() {
        AwsServiceException macieNotEnabledException = Macie2Exception.builder()
            .message(MACIE_NOT_ENABLED)
            .awsErrorDetails(AwsErrorDetails.builder()
                .errorCode(MACIE_NOT_ENABLED_HTTP_STATUS_CODE)
                .errorMessage(MACIE_NOT_ENABLED)
                .sdkHttpResponse(SdkHttpResponse.builder().statusCode(Integer.parseInt(MACIE_NOT_ENABLED_HTTP_STATUS_CODE)).build())
                .build()
            )
            .build();
        when(proxyMacie2Client.client()).thenReturn(macie2);
        when(proxyMacie2Client.injectCredentialsAndInvokeV2(any(ListFindingsFiltersRequest.class), any())).thenThrow(macieNotEnabledException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();
        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, new CallbackContext(), proxyMacie2Client, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getMessage()).contains(String.format(ACCESS_DENIED_CFN_MESSAGE, OPERATION));
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InternalFailure);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
    }
}
