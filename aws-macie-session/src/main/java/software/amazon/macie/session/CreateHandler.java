package software.amazon.macie.session;

import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.EnableMacieRequest;
import software.amazon.awssdk.services.macie2.model.Macie2Exception;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class CreateHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger
    ) {
        final Macie2Client client = ClientBuilder.getClient();
        final ResourceModel model = request.getDesiredResourceState();

        final EnableMacieRequest enableMacieRequest =
                EnableMacieRequest.builder()
                                  .clientToken(request.getClientRequestToken())
                                  .status(model.getStatus())
                                  .findingPublishingFrequency(model.getFindingPublishingFrequency())
                                  .build();
        try {
            proxy.injectCredentialsAndInvokeV2(enableMacieRequest, client::enableMacie);
            logger.log(String.format("%s [%s] created successfully",
                                     ResourceModel.TYPE_NAME,
                                     ResourceModelExtensions.getPrimaryIdentifier(model).toString()));
        } catch (Macie2Exception e) {
            throw new CfnServiceLimitExceededException(ResourceModel.TYPE_NAME, e.toString());
        }

        return ProgressEvent.defaultSuccessHandler(model);
    }
}
