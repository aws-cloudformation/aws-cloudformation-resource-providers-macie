package software.amazon.macie.session;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpStatus;
import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.Macie2Exception;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
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
    protected final static String MACIE_ALREADY_ENABLED = "Macie has already been enabled";
    protected static final String MACIE_ALREADY_ENABLED_EXPECTED_MESSAGE = "Resource of type '%s' with identifier '%s' already exists.";
    protected static final String MACIE_NOT_ENABLED_EXPECTED_MESSAGE = "Resource of type '%s' with identifier '%s' was not found.";
    private final static String RETRY_MESSAGE = "Detected retryable error for AWS account id [%s], retrying. Exception message: %s";
    private final static String EXCEPTION_MESSAGE = "Exception occurred for AWS account id [%s]. Exception message: %s";

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

    private Boolean retryError(Exception exception) {
        return exception instanceof Macie2Exception
            && ((Macie2Exception) exception).awsErrorDetails().sdkHttpResponse().statusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR;
    }

    public ProgressEvent<ResourceModel, CallbackContext> handleError(final String operation,
        ResourceHandlerRequest<ResourceModel> request, final Exception exception,
        final ResourceModel model,
        CallbackContext context, Logger logger) {
        // All InternalServerExceptions are retryable
        if (retryError(exception)) {
            logger.log(String.format(RETRY_MESSAGE, request.getAwsAccountId(), exception.getMessage()));
            return ProgressEvent.progress(model, context);
        }

        logger.log(String.format(EXCEPTION_MESSAGE, request.getAwsAccountId(), ExceptionUtils.getStackTrace(exception)));
        ProgressEventBuilder<ResourceModel, CallbackContext> failureProgressEvent = ProgressEvent.<ResourceModel, CallbackContext>builder()
            .status(OperationStatus.FAILED);
        if (exception.getMessage().contains(MACIE_NOT_ENABLED)) {
            return failureProgressEvent
                .errorCode(HandlerErrorCode.NotFound)
                .message(String.format(MACIE_NOT_ENABLED_EXPECTED_MESSAGE, ResourceModel.TYPE_NAME, model.getAwsAccountId()))
                .build();
        } else if (exception.getMessage().contains(MACIE_ALREADY_ENABLED)) {
            return failureProgressEvent
                .errorCode(HandlerErrorCode.AlreadyExists)
                .message(String.format(MACIE_ALREADY_ENABLED_EXPECTED_MESSAGE, ResourceModel.TYPE_NAME, model.getAwsAccountId()))
                .build();
        } else {
            throw new CfnGeneralServiceException(operation, exception);
        }
    }
}
