package software.amazon.macie.session;

import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.GetMacieSessionRequest;
import software.amazon.awssdk.services.macie2.model.GetMacieSessionResponse;
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

        final ResourceModel model = getMacieSession();

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(model)
            .status(OperationStatus.SUCCESS)
            .build();
    }

    private ResourceModel getMacieSession() {
        final GetMacieSessionRequest request = GetMacieSessionRequest.builder().build();
        final GetMacieSessionResponse response;

        try {
            response = proxy.injectCredentialsAndInvokeV2(request, client::getMacieSession);
        } catch (final Macie2Exception e) {
            throw new CfnAccessDeniedException(ResourceModel.TYPE_NAME);
        }

        return ResourceModel.builder()
                            .status(response.statusAsString())
                            .findingPublishingFrequency(response.findingPublishingFrequencyAsString())
                            .serviceRole(response.serviceRole())
                            .createdAt(response.createdAt().toString())
                            .updatedAt(response.updatedAt().toString())
                            .build();
    }
}
