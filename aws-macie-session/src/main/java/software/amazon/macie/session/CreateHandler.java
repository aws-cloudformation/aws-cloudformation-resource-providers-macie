package software.amazon.macie.session;

import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.EnableMacieRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class CreateHandler extends BaseMacieSessionHandler {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<Macie2Client> client,
        final Logger logger
    ) {
        final ResourceModel model = request.getDesiredResourceState();

        // initiate the call context.
        return proxy.initiate("macie2:enableMacie", client, model, callbackContext)
            // transform Resource model properties to EnableMacie API
            .translateToServiceRequest((m) -> EnableMacieRequest.builder()
                .clientToken(request.getClientRequestToken())
                .findingPublishingFrequency(m.getFindingPublishingFrequency())
                .status(m.getStatus())
                .build())
            // Make a service call. Handler does not worry about credentials, they are auto injected
            .makeServiceCall((r, c) -> c.injectCredentialsAndInvokeV2(r, c.client()::enableMacie))
            // Ensure all requisite resources are created and we can query Macie session
            .stabilize((_request, _response, _client, _model, _context) -> isStabilized(_model, _client))
            // return appropriate failed progress event status by mapping business exceptions.
            .handleError((_request, _exception, _client, _model, _context) -> failureProgressEvent(_exception, _model, _context))
            // Once ACTIVE return progress
            .progress()
            // we then delegate to ReadHandler to read the live state and send back successful response.
            .then((r) -> new ReadHandler()
                .handleRequest(proxy, request, callbackContext, client, logger));
    }
}
