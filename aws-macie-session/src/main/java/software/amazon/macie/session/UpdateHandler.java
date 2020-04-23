package software.amazon.macie.session;

import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.Macie2Exception;
import software.amazon.awssdk.services.macie2.model.UpdateMacieSessionRequest;
import software.amazon.awssdk.services.macie2.model.UpdateMacieSessionRequest.Builder;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class UpdateHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {
        final Macie2Client client = ClientBuilder.getClient();
        final ResourceModel model = request.getDesiredResourceState();

        final UpdateMacieSessionRequest updateMacieSessionRequest = buildRequest(model);

        try {
            proxy.injectCredentialsAndInvokeV2(updateMacieSessionRequest, client::updateMacieSession);
            logger.log(String.format("%s [%s] updated successfully",
                                     ResourceModel.TYPE_NAME,
                                     ResourceModelExtensions.getPrimaryIdentifier(model).toString()));
        } catch (Macie2Exception e) {
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }

        return ProgressEvent.defaultSuccessHandler(model);
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
