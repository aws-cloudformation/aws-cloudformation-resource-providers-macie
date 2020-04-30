package software.amazon.macie.customdataidentifier;

import software.amazon.awssdk.services.macie2.model.CustomDataIdentifierSummary;
import software.amazon.awssdk.services.macie2.model.ListCustomDataIdentifiersRequest;
import software.amazon.awssdk.services.macie2.model.ListCustomDataIdentifiersResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.*;

public class ListHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger
    ) {
        final List<ResourceModel> models = new ArrayList<>();

        // STEP 1 [construct a body of a request]
        final ListCustomDataIdentifiersRequest listRequest = Translator.translateToListRequest(request.getNextToken());

        // STEP 2 [make an api call]
        ListCustomDataIdentifiersResponse listResponse = proxy.injectCredentialsAndInvokeV2(listRequest, ClientBuilder.getClient()::listCustomDataIdentifiers);

        // STEP 3 [get a token for the next page]
        String nextToken = listResponse.nextToken();

        // STEP 4 [construct resource models]
        for (CustomDataIdentifierSummary summary: listResponse.items()) {
            models.add(
                    ResourceModel.builder()
                                 .id(summary.id())
                                 .arn(summary.arn())
                                 .name(summary.name())
                                 .description(summary.description())
                                 .createdAt(summary.createdAt().toString())
                                 .build()
            );
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                            .resourceModels(models)
                            .nextToken(nextToken)
                            .status(OperationStatus.SUCCESS)
                            .build();
    }
}
