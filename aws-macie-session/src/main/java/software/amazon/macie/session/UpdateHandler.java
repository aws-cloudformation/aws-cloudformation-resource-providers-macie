package software.amazon.macie.session;

import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.UpdateMacieSessionRequest;
import software.amazon.awssdk.services.macie2.model.UpdateMacieSessionRequest.Builder;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class UpdateHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        Macie2Client client = Macie2Client.builder().build();

        client.updateMacieSession(buildRequest(model));

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(model)
            .status(OperationStatus.SUCCESS)
            .build();
    }

    private UpdateMacieSessionRequest buildRequest(ResourceModel model) {
        Builder builder = UpdateMacieSessionRequest.builder();
        if (!model.getStatus().isEmpty()) {
            builder.status(model.getStatus());
        }

        if (!model.getFindingPublishingFrequency().isEmpty()) {
            builder.findingPublishingFrequency(model.getFindingPublishingFrequency());
        }
        return builder.build();
    }
}
