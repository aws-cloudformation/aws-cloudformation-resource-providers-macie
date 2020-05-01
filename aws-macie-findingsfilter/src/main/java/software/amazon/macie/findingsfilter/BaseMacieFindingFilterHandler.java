package software.amazon.macie.findingsfilter;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpStatus;
import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.CreateFindingsFilterRequest;
import software.amazon.awssdk.services.macie2.model.Macie2Exception;
import software.amazon.awssdk.services.macie2.model.UpdateFindingsFilterRequest;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProgressEvent.ProgressEventBuilder;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public abstract class BaseMacieFindingFilterHandler extends BaseHandler<CallbackContext> {

    // All Macie session APIs return this message whe Macie is not enabled
    // Error code being 403, we can't depend on error code for this.
    // Filter already exists returns 400
    protected final static String MACIE_NOT_ENABLED = "Macie is not enabled";
    protected final static String FILTER_ALREADY_EXISTS = "Filter name already exists. A unique name is required.";
    protected static final String RESOURCE_EXISTS_CFN_MESSAGE = "Resource of type '%s' with identifier '%s' already exists.";
    protected static final String RESOURCE_MISSING_CFN_MESSAGE = "Resource of type '%s' with identifier '%s' was not found.";
    private final static String RETRY_MESSAGE = "Detected retryable error, retrying. Exception message: %s";
    private final static String EXCEPTION_MESSAGE = "Exception occurred. Exception message: %s";

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

    // Translation
    // CFN Model to SDK

    protected CreateFindingsFilterRequest createFindingFilterRequest(final String clientToken, final ResourceModel model) {
        return CreateFindingsFilterRequest.builder()
            .clientToken(clientToken)
            .name(model.getName())
            .description(model.getDescription())
            .findingCriteria(macieSdkFindingCriteria(model))
            .action(model.getAction())
            .position(model.getPosition())
            .build();
    }

    protected UpdateFindingsFilterRequest updateFindingFilterRequest(final ResourceModel model) {
        return UpdateFindingsFilterRequest.builder()
            .filterId(model.getFilterId())
            .name(model.getName())
            .description(model.getDescription())
            .findingCriteria(macieSdkFindingCriteria(model))
            .action(model.getAction())
            .position(model.getPosition())
            .build();
    }

    private software.amazon.awssdk.services.macie2.model.FindingCriteria macieSdkFindingCriteria(final ResourceModel model) {
        Map<String, software.amazon.awssdk.services.macie2.model.CriterionAdditionalProperties> criterion = model.getFindingCriteria().getCriterion().entrySet()
            .stream()
            .collect(Collectors.toMap(
                Entry::getKey,
                entry -> macieSdkCriterionAdditionalProperties(entry.getValue())
            ));
        return software.amazon.awssdk.services.macie2.model.FindingCriteria.builder()
            .criterion(criterion).build();
    }

    private software.amazon.awssdk.services.macie2.model.CriterionAdditionalProperties macieSdkCriterionAdditionalProperties(
        final software.amazon.macie.findingsfilter.CriterionAdditionalProperties input) {
        return software.amazon.awssdk.services.macie2.model.CriterionAdditionalProperties.builder()
            .eq(getCollectionOrNull(input.getEq()))
            .neq(getCollectionOrNull(input.getNeq()))
            .gt(getLongOrNull(input.getGt()))
            .gte(getLongOrNull(input.getGte()))
            .lt(getLongOrNull(input.getLt()))
            .lte(getLongOrNull(input.getLte()))
            .build();
    }

    // SDK to CFN model

    protected FindingCriteria cfnModelFindingCriteria(software.amazon.awssdk.services.macie2.model.FindingCriteria criteria) {
        Map<String, software.amazon.macie.findingsfilter.CriterionAdditionalProperties> modelFindingCriteria
            = criteria.criterion().entrySet().stream().collect(Collectors.toMap(
            Entry::getKey,
            entry -> cfnModelCriterionAdditionalProperties(entry.getValue())
        ));
        return FindingCriteria.builder().criterion(modelFindingCriteria).build();
    }

    private software.amazon.macie.findingsfilter.CriterionAdditionalProperties cfnModelCriterionAdditionalProperties(
        software.amazon.awssdk.services.macie2.model.CriterionAdditionalProperties criterionAdditionalProperties) {
        return software.amazon.macie.findingsfilter.CriterionAdditionalProperties.builder()
            .eq(getCollectionOrNull(criterionAdditionalProperties.eq()))
            .neq(getCollectionOrNull(criterionAdditionalProperties.neq()))
            .gt(getIntegerOrNull(criterionAdditionalProperties.gt()))
            .gte(getIntegerOrNull(criterionAdditionalProperties.gte()))
            .lt(getIntegerOrNull(criterionAdditionalProperties.lt()))
            .lte(getIntegerOrNull(criterionAdditionalProperties.lte()))
            .build();
    }

    // Quick utils
    protected Long getLongOrNull(Integer value) {
        return value == null ? null : Long.valueOf(value);
    }

    protected Integer getIntegerOrNull(Long value) {
        return value == null ? null : Math.toIntExact(value);
    }

    protected List<String> getCollectionOrNull(List<String> value) {
        return CollectionUtils.isEmpty(value) ? null : value;
    }

    // Exception handling

    private Boolean macieNotEnabled(Exception exception) {
        return exception.getMessage().contains(MACIE_NOT_ENABLED);
    }

    private Boolean retryError(Exception exception) {
        return exception instanceof Macie2Exception
            && ((Macie2Exception) exception).awsErrorDetails().sdkHttpResponse().statusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR;
    }

    private Boolean notFound(Exception exception) {
        return exception instanceof Macie2Exception
            && ((Macie2Exception) exception).awsErrorDetails().sdkHttpResponse().statusCode() == HttpStatus.SC_NOT_FOUND;
    }

    protected ProgressEvent<ResourceModel, CallbackContext> handleError(final String operation, final Exception exception, final ResourceModel model,
        final CallbackContext context, final Logger logger) {
        // All InternalServerExceptions are retryable
        if (retryError(exception)) {
            logger.log(String.format(RETRY_MESSAGE, exception.getMessage()));
            return ProgressEvent.progress(model, context);
        }

        logger.log(String.format(EXCEPTION_MESSAGE, ExceptionUtils.getStackTrace(exception)));
        ProgressEventBuilder<ResourceModel, CallbackContext> failureProgressEvent = ProgressEvent.<ResourceModel, CallbackContext>builder()
            .status(OperationStatus.FAILED);

        // Check of business exceptions
        if (macieNotEnabled(exception)) {
            throw new CfnAccessDeniedException(operation, exception);
        } else if (notFound(exception)) {
            return failureProgressEvent
                .errorCode(HandlerErrorCode.NotFound)
                .message(String.format(RESOURCE_MISSING_CFN_MESSAGE, ResourceModel.TYPE_NAME, model.getFilterId()))
                .build();
        } else if (exception.getMessage().contains(FILTER_ALREADY_EXISTS)) {
            return failureProgressEvent
                .errorCode(HandlerErrorCode.AlreadyExists)
                .message(String.format(RESOURCE_EXISTS_CFN_MESSAGE, ResourceModel.TYPE_NAME, model.getName()))
                .build();
        } else {
            throw new CfnGeneralServiceException(operation, exception);
        }
    }
}
