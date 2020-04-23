package software.amazon.macie.session;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.DisableMacieRequest;
import software.amazon.awssdk.services.macie2.model.GetMacieSessionRequest;
import software.amazon.awssdk.services.macie2.model.Macie2Exception;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandler<CallbackContext> {

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

        return checkAndDisableMacie(model);
    }

    private ProgressEvent<ResourceModel, CallbackContext> checkAndDisableMacie(ResourceModel model) {
        try {
            // ensure Macie session exists
            GetMacieSessionRequest macieSessionRequest = GetMacieSessionRequest.builder().build();
            clientProxy.injectCredentialsAndInvokeV2(macieSessionRequest, macie2Client::getMacieSession);

            // now disable it
            return disableMacie(model);
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

    private ProgressEvent<ResourceModel, CallbackContext> disableMacie(ResourceModel model) {
        clientProxy.injectCredentialsAndInvokeV2(DisableMacieRequest.builder().build(), macie2Client::disableMacie);
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
