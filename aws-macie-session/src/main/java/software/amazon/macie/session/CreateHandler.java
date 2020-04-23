package software.amazon.macie.session;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.EnableMacieRequest;
import software.amazon.awssdk.services.macie2.model.EnableMacieRequest.Builder;
import software.amazon.awssdk.services.macie2.model.EnableMacieResponse;
import software.amazon.awssdk.services.macie2.model.GetMacieSessionRequest;
import software.amazon.awssdk.services.macie2.model.Macie2Exception;
import software.amazon.awssdk.services.macie2.model.MacieStatus;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class CreateHandler extends BaseHandler<CallbackContext> {

    private static final String MACIE_ALREADY_ENABLED = "Macie already enabled. AWS account ID: %s";
    private static final String MACIE_NOT_ENABLED = "Macie is not enabled. Proceeding to enable. AWS account ID: %s";
    AmazonWebServicesClientProxy clientProxy;
    Macie2Client macie2Client;
    Logger loggerProxy;
    String awsAccountId;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger
    ) {
        final ResourceModel model = request.getDesiredResourceState();
        clientProxy = proxy;
        macie2Client = ClientBuilder.getClient();
        loggerProxy = logger;
        awsAccountId = request.getAwsAccountId();

        return checkAndEnableMacie(model);
    }

    private ProgressEvent<ResourceModel, CallbackContext> checkAndEnableMacie(ResourceModel model) {

        GetMacieSessionRequest macieSessionRequest = GetMacieSessionRequest.builder().build();
        try {
            clientProxy
                .injectCredentialsAndInvokeV2(macieSessionRequest, macie2Client::getMacieSession);

            loggerProxy.log(String.format(MACIE_ALREADY_ENABLED, awsAccountId));
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .status(OperationStatus.FAILED)
                .errorCode(HandlerErrorCode.AlreadyExists)
                .build();
        } catch (AwsServiceException e) {
            if (e instanceof Macie2Exception && e.statusCode() == HttpStatusCode.FORBIDDEN) {
                // Macie is not enabled/paused
                loggerProxy.log(String.format(MACIE_NOT_ENABLED, awsAccountId));
                return enableMacie(model);
            }
            // Some other AWSServicesException
            throw e;
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> enableMacie(ResourceModel model) {
        EnableMacieResponse enableMacieResponse = clientProxy
            .injectCredentialsAndInvokeV2(buildRequest(model), macie2Client::enableMacie);
        model.setAwsAccountId(awsAccountId);
        model.setStatus(MacieStatus.ENABLED.name());
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(model)
            .status(OperationStatus.SUCCESS)
            .build();
    }

    private EnableMacieRequest buildRequest(ResourceModel model) {
        Builder builder = EnableMacieRequest.builder()
            .status(model.getStatus())
            .findingPublishingFrequency(model.getFindingPublishingFrequency());
        if (StringUtils.isNotEmpty(model.getClientToken())) {
            builder.clientToken(model.getClientToken());
        }
        return builder.build();
    }
}
