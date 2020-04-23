package software.amazon.macie.session;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.GetMacieSessionRequest;
import software.amazon.awssdk.services.macie2.model.Macie2Exception;
import software.amazon.awssdk.services.macie2.model.UpdateMacieSessionRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class UpdateHandler extends BaseHandler<CallbackContext> {

    AmazonWebServicesClientProxy clientProxy;
    Macie2Client macie2Client;
    Logger loggerProxy;
    String awsAccountId;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        clientProxy = proxy;
        macie2Client = ClientBuilder.getClient();
        loggerProxy = logger;
        awsAccountId = request.getAwsAccountId();

        return checkAndUpdateMacieSession(model);
    }

    private ProgressEvent<ResourceModel, CallbackContext> checkAndUpdateMacieSession(ResourceModel model) {
        try {
            // ensure Macie session exists
            GetMacieSessionRequest macieSessionRequest = GetMacieSessionRequest.builder().build();
            clientProxy.injectCredentialsAndInvokeV2(macieSessionRequest, macie2Client::getMacieSession);

            // now update it
            return updateMacieSession(model);
        } catch (AwsServiceException e) {
            if (e instanceof Macie2Exception && e.statusCode() == HttpStatusCode.FORBIDDEN) {
                // Macie is not enabled
                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.FAILED)
                    .errorCode(HandlerErrorCode.NotFound)
                    .message(e.getMessage())
                    .build();
            }
            throw e;
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateMacieSession(ResourceModel model) {
        clientProxy.injectCredentialsAndInvokeV2(buildRequest(model), macie2Client::updateMacieSession);
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(model)
            .status(OperationStatus.SUCCESS)
            .build();
    }

    private UpdateMacieSessionRequest buildRequest(ResourceModel model) {
        UpdateMacieSessionRequest.Builder builder = UpdateMacieSessionRequest.builder();
        if (StringUtils.isNotEmpty(model.getStatus())) {
            builder.status(model.getStatus());
        }
        if (StringUtils.isNotEmpty(model.getFindingPublishingFrequency())) {
            builder.findingPublishingFrequency(model.getFindingPublishingFrequency());
        }

        return builder.build();
    }
}
