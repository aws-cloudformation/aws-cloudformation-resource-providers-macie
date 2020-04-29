package software.amazon.macie.session;

import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
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

    public ProgressEvent<ResourceModel, CallbackContext> handleError(final String operation, final Exception exception, final ResourceModel model) {
        if (exception.getMessage().contains(MACIE_NOT_ENABLED)) {
            throw new CfnNotFoundException(model.TYPE_NAME, model.getAwsAccountId(), exception);
        } else if (exception.getMessage().contains(MACIE_ALRAEDY_ENABLED)) {
            throw new CfnAlreadyExistsException(model.TYPE_NAME, model.getAwsAccountId(), exception);
        } else {
            throw new CfnGeneralServiceException(operation, exception);
        }
    }
}
