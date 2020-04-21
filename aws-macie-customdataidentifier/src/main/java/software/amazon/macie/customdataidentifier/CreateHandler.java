package software.amazon.macie.customdataidentifier;

import java.util.Optional;
import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.CreateCustomDataIdentifierRequest;
import software.amazon.awssdk.services.macie2.model.CustomDataIdentifierSummary;
import software.amazon.awssdk.services.macie2.model.ListCustomDataIdentifiersRequest;
import software.amazon.awssdk.services.macie2.model.ListCustomDataIdentifiersResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProgressEvent.ProgressEventBuilder;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class CreateHandler extends BaseHandler<CallbackContext> {

    private int MAX_LIST_RESULTS = 100;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        return createCustomDataIdentifier(model);
    }

    private ProgressEvent<ResourceModel, CallbackContext> createCustomDataIdentifier(ResourceModel model) {
        final Macie2Client client = Macie2Client.builder().build();
        ProgressEventBuilder<ResourceModel, CallbackContext> progressEventBuilder = ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(model);
        ListCustomDataIdentifiersRequest request = ListCustomDataIdentifiersRequest.builder()
            .maxResults(MAX_LIST_RESULTS)
            .build();
        ListCustomDataIdentifiersResponse response = client.listCustomDataIdentifiers(request);
        Optional<CustomDataIdentifierSummary> existingCustomDataIdentifier = response.items().stream()
            .filter(customDataIdentifierSummary -> customDataIdentifierSummary.name().equalsIgnoreCase(model.getName()))
            .findAny();
        existingCustomDataIdentifier.ifPresentOrElse(
            customDataIdentifierSummary -> progressEventBuilder.status(OperationStatus.FAILED)
                .errorCode(HandlerErrorCode.AlreadyExists)
                .callbackContext(CallbackContext.builder().build()),
            () -> progressEventBuilder.status(OperationStatus.SUCCESS)
                .callbackContext(CallbackContext.builder()
                    .customDataIdentifierId(client.createCustomDataIdentifier(buildRequest(model)).customDataIdentifierId())
                    .customDataIdentifierName()
                    .build()));
        return progressEventBuilder.build();
    }

    private CreateCustomDataIdentifierRequest buildRequest(ResourceModel model) {
        return CreateCustomDataIdentifierRequest.builder()
            .name(model.getName())
            .clientToken(model.getClientToken())
            .description(model.getDescription())
            .ignoreWords(model.getIgnoreWords())
            .keywords(model.getKeywords())
            .maximumMatchDistance(model.getMaximumMatchDistance())
            .regex(model.getRegex())
            .build();
    }
}
