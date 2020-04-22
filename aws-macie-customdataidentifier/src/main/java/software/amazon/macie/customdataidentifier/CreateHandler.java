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

        final Macie2Client client = Macie2Client.builder().build();
        ProgressEventBuilder<ResourceModel, CallbackContext> progressEventBuilder
                = ProgressEvent.<ResourceModel, CallbackContext>builder().resourceModel(model);

        ListCustomDataIdentifiersRequest listCustomDataIdentifiersRequest
                = ListCustomDataIdentifiersRequest.builder()
                                                 .maxResults(MAX_LIST_RESULTS)
                                                 .build();

        ListCustomDataIdentifiersResponse listCustomDataIdentifiersResponse
                = client.listCustomDataIdentifiers(listCustomDataIdentifiersRequest);

        Optional<CustomDataIdentifierSummary> existingCustomDataIdentifier
                = listCustomDataIdentifiersResponse.items()
                                                  .stream()
                                                  .filter(summary -> summary.name().equalsIgnoreCase(model.getName()))
                                                  .findAny();

        if (existingCustomDataIdentifier.isPresent()) {
            progressEventBuilder.status(OperationStatus.FAILED)
                                .errorCode(HandlerErrorCode.AlreadyExists)
                                .callbackContext(CallbackContext.builder().build());
        } else {
            progressEventBuilder.status(OperationStatus.SUCCESS)
                                .callbackContext(CallbackContext.builder()
                                                                .customDataIdentifierId(
                                                                        client.createCustomDataIdentifier(
                                                                                  buildRequest(request, model))
                                                                              .customDataIdentifierId())
                                                                .build());
        }

        return progressEventBuilder.build();
    }

    private CreateCustomDataIdentifierRequest buildRequest(
            ResourceHandlerRequest<ResourceModel> request,
            ResourceModel model) {
        return CreateCustomDataIdentifierRequest.builder()
                                               .clientToken(request.getClientRequestToken())
                                               .name(model.getName())
                                               .description(model.getDescription())
                                               .ignoreWords(model.getIgnoreWords())
                                               .keywords(model.getKeywords())
                                               .maximumMatchDistance(model.getMaximumMatchDistance())
                                               .regex(model.getRegex())
                                               .build();
    }
}
