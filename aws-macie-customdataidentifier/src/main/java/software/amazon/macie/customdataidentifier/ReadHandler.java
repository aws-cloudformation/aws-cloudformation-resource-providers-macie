package software.amazon.macie.customdataidentifier;

import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.GetCustomDataIdentifierRequest;
import software.amazon.awssdk.services.macie2.model.GetCustomDataIdentifierResponse;
import software.amazon.awssdk.services.macie2.model.Macie2Exception;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandler<CallbackContext> {

    private AmazonWebServicesClientProxy proxy;
    private Macie2Client client;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger
    ) {
        this.proxy = proxy;
        this.client = ClientBuilder.getClient();

        final ResourceModel model = getCustomDataIdentifier(request.getDesiredResourceState());

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();

    }

    private ResourceModel getCustomDataIdentifier(ResourceModel model) {
        final GetCustomDataIdentifierRequest request = GetCustomDataIdentifierRequest.builder().id(model.getId()).build();
        final GetCustomDataIdentifierResponse response;

        try {
            response = proxy.injectCredentialsAndInvokeV2(request, client::getCustomDataIdentifier);
        } catch (final Macie2Exception e) {
            throw new CfnAccessDeniedException(ResourceModel.TYPE_NAME);
        }

        return ResourceModel.builder()
                            .arn(response.arn())
                            .id(response.id())
                            .name(response.name())
                            .description(response.description())
                            .regex(response.regex())
                            .keywords(response.keywords())
                            .ignoreWords(response.ignoreWords())
                            .maximumMatchDistance(response.maximumMatchDistance())
                            .deleted(response.deleted())
                            .createdAt(response.createdAt().toString())
                            .build();
    }

}
