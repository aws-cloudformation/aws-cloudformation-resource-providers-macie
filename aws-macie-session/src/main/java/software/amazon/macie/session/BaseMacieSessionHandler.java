package software.amazon.macie.session;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.GetMacieSessionRequest;
import software.amazon.awssdk.services.macie2.model.Macie2Exception;
import software.amazon.awssdk.services.macie2.model.MacieStatus;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProgressEvent.ProgressEventBuilder;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public abstract class BaseMacieSessionHandler extends BaseHandler<CallbackContext> {

    // All Macie session APIs return this message whe Macie is not enabled
    // Error code being 403, we can't depend on error code for this.
    protected final static String MACIE_NOT_ENABLED = "Macie is not enabled";
    protected final static String MACIE_ALRAEDY_ENABLED = "Macie has already been enabled";

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(AmazonWebServicesClientProxy proxy,
        ResourceHandlerRequest<ResourceModel> request, CallbackContext callbackContext, Logger logger) {
        return handleRequest(
            proxy,
            request,
            callbackContext != null ? callbackContext : new CallbackContext(),
            proxy.newProxy(ClientBuilder::getClient),
            logger);
    }

    protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        AmazonWebServicesClientProxy proxy,
        ResourceHandlerRequest<ResourceModel> request,
        CallbackContext callbackContext,
        ProxyClient<Macie2Client> proxyClient,
        Logger logger);

    protected ProgressEvent<ResourceModel, CallbackContext> failureProgressEvent(final Exception exception, final ResourceModel model,
        final CallbackContext context) {
        ProgressEventBuilder<ResourceModel, CallbackContext> failedProcessEvent = ProgressEvent.<ResourceModel, CallbackContext>builder()
            .status(OperationStatus.FAILED)
            .message(exception.getMessage());
        if (exception.getMessage().contains(MACIE_NOT_ENABLED)) {
            failedProcessEvent.errorCode(HandlerErrorCode.NotFound);
        } else if (exception.getMessage().contains(MACIE_ALRAEDY_ENABLED)) {
            failedProcessEvent.errorCode(HandlerErrorCode.AlreadyExists);
        } else {
            // Not all HTTPStatus code are HandlerErrorCode members.
            HandlerErrorCode handlerErrorCode;
            try {
                handlerErrorCode = HandlerErrorCode.valueOf(((AwsServiceException) exception).awsErrorDetails().errorCode());
            } catch (IllegalArgumentException e) {
                handlerErrorCode = HandlerErrorCode.GeneralServiceException;
            }
            failedProcessEvent.errorCode(handlerErrorCode);
        }
        return failedProcessEvent.build();
    }


    protected boolean isStabilized(final ResourceModel model, final ProxyClient<Macie2Client> proxyClient) {
        try {
            // Check if Macie status can be queried successfully.
            proxyClient.injectCredentialsAndInvokeV2(
                GetMacieSessionRequest.builder().build(),
                proxyClient.client()::getMacieSession);
            return true;
        } catch (Macie2Exception e){
            return false;
        }
    }
}
