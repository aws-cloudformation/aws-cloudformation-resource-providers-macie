package software.amazon.macie.findingsfilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static software.amazon.macie.findingsfilter.BaseMacieFindingFilterHandler.MACIE_NOT_ENABLED;
import static software.amazon.macie.findingsfilter.BaseMacieFindingFilterHandler.RESOURCE_MISSING_CFN_MESSAGE;
import static software.amazon.macie.findingsfilter.ReadHandler.OPERATION;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
import software.amazon.awssdk.services.macie2.model.GetFindingsFilterRequest;
import software.amazon.awssdk.services.macie2.model.GetFindingsFilterResponse;
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
public class ReadHandlerTest {

    private static final Credentials MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
    private static final String TEST_ACCOUNT_ID = "999999999999";
    private static final String FILTER_ID = "1b1a111a-1111-1ba1-ba1a-11111111bb11";
    private static final String FILTER_ARN = "arn:aws:macie:us-west-2:%s:findingfilter/%s";
    private static final String FILTER_NAME = "findings_filter";
    private static final String FILTER_DESCRIPTION = "findings_filter_description";
    private static final String FILTER_ACTION = "ARCHIVE";
    private static final int FILTER_POSITION = 1;
    private static final String ACCOUNT_ID = "accountId";
    private static final String MACIE_NOT_ENABLED_HTTP_STATUS_CODE = "403";
    private static final String FILTER_MISSING_HTTP_STATUS_CODE = "404";
    private static final String FILTER_MISSING_MACIE_MESSAGE = "Resource not found. Verify any provided IDs are correct.";
    private static final String ACCESS_DENIED_CFN_MESSAGE = "Access denied for operation '%s'.";

    private final ResourceModel model = ResourceModel.builder()
        .id(FILTER_ID)
        .build();
    @Mock
    private ProxyClient<Macie2Client> proxyMacie2Client;

    private AmazonWebServicesClientProxy proxy;
    @Mock
    private Macie2Client macie2;
    private LoggerProxy logger;
    private ReadHandler handler;

    @BeforeEach
    public void setup() {
        handler = new ReadHandler();
        logger = new LoggerProxy();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
    }


    @Test
    public void handleRequest_SimpleSuccess() {
        GetFindingsFilterResponse findingsFilterResponse = GetFindingsFilterResponse.builder()
            .id(FILTER_ID)
            .arn(String.format(FILTER_ARN, TEST_ACCOUNT_ID, FILTER_ID))
            .name(FILTER_NAME)
            .description(FILTER_DESCRIPTION)
            .action(FILTER_ACTION)
            .position(FILTER_POSITION)
            .findingCriteria(software.amazon.awssdk.services.macie2.model.FindingCriteria.builder()
                .criterion(ImmutableMap.of(
                    ACCOUNT_ID,
                    software.amazon.awssdk.services.macie2.model.CriterionAdditionalProperties.builder().eq(ImmutableList.of(TEST_ACCOUNT_ID)).build()
                ))
                .build())
            .build();
        when(proxyMacie2Client.client()).thenReturn(macie2);
        when(proxyMacie2Client
            .injectCredentialsAndInvokeV2(any(GetFindingsFilterRequest.class), any())).thenReturn(findingsFilterResponse);
        final ResourceModel desiredOutputModel = ResourceModel.builder()
            .id(FILTER_ID)
            .arn(String.format(FILTER_ARN, TEST_ACCOUNT_ID, FILTER_ID))
            .name(FILTER_NAME)
            .description(FILTER_DESCRIPTION)
            .action(FILTER_ACTION)
            .position(FILTER_POSITION)
            .findingCriteria(
                FindingCriteria.builder()
                    .criterion(ImmutableMap.of(
                        ACCOUNT_ID, CriterionAdditionalProperties.builder().eq(ImmutableList.of(TEST_ACCOUNT_ID)).build()
                    ))
                    .build()
            )
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
    public void handleRequest_NotFound_Fails() {
        AwsServiceException filterNotFoundException = Macie2Exception.builder()
            .awsErrorDetails(AwsErrorDetails.builder()
                .errorCode(FILTER_MISSING_HTTP_STATUS_CODE)
                .errorMessage(FILTER_MISSING_MACIE_MESSAGE)
                .sdkHttpResponse(SdkHttpResponse.builder().statusCode(Integer.parseInt(FILTER_MISSING_HTTP_STATUS_CODE)).build())
                .build()
            )
            .build();
        when(proxyMacie2Client.client()).thenReturn(macie2);
        when(proxyMacie2Client.injectCredentialsAndInvokeV2(any(GetFindingsFilterRequest.class), any())).thenThrow(filterNotFoundException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();
        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, new CallbackContext(), proxyMacie2Client, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getMessage()).contains(String.format(RESOURCE_MISSING_CFN_MESSAGE, ResourceModel.TYPE_NAME, model.getId()));
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
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
        when(proxyMacie2Client.injectCredentialsAndInvokeV2(any(GetFindingsFilterRequest.class), any())).thenThrow(macieNotEnabledException);

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
