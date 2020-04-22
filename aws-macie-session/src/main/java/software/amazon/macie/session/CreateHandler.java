package software.amazon.macie.session;

import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.EnableMacieRequest;
import software.amazon.awssdk.services.macie2.model.EnableMacieRequest.Builder;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class CreateHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final Macie2Client client = Macie2Client.builder().build();

        client.enableMacie(buildRequest(request));

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(model)
            .status(OperationStatus.SUCCESS)
            .build();
    }

    private EnableMacieRequest buildRequest(ResourceHandlerRequest<ResourceModel> request) {
        final ResourceModel model = request.getDesiredResourceState();
        Builder builder = EnableMacieRequest.builder();

        builder.status(model.getStatus())
               .clientToken(request.getClientRequestToken());

        if (!model.getFindingPublishingFrequency().isEmpty()) {
            builder.findingPublishingFrequency(model.getFindingPublishingFrequency());
        }

        return builder.build();
    }
}
