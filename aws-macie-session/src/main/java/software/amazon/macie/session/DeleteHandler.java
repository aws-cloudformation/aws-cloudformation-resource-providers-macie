package software.amazon.macie.session;

import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.DisableMacieRequest;
import software.amazon.awssdk.services.macie2.model.Macie2Exception;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger
    ) {

        final Macie2Client client = ClientBuilder.getClient();
        final ResourceModel model = request.getDesiredResourceState();

        try {
            final DisableMacieRequest disableMacieRequest = DisableMacieRequest.builder().build();
            proxy.injectCredentialsAndInvokeV2(disableMacieRequest, client::disableMacie);
            logger.log(String.format("%s [%s] deleted successfully",
                                     ResourceModel.TYPE_NAME,
                                     ResourceModelExtensions.getPrimaryIdentifier(model).toString()));
        } catch (final Macie2Exception e) {
            throw new CfnAccessDeniedException(ResourceModel.TYPE_NAME);
        }

        return ProgressEvent.defaultSuccessHandler(null);
    }
}
