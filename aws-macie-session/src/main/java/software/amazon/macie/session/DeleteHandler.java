package software.amazon.macie.session;

import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.DisableMacieRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProgressEvent.ProgressEventBuilder;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseMacieSessionHandler {

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
        return proxy.initiate("macie2:disableMacie", client, model, callbackContext)
            // transform Resource model properties to DisableMacie API
            .translateToServiceRequest((m) -> DisableMacieRequest.builder().build())
            // Make a service call. Handler does not worry about credentials, they are auto injected
            .makeServiceCall((r, c) -> c.injectCredentialsAndInvokeV2(r, c.client()::disableMacie))
            // return appropriate failed progress event status by mapping business exceptions.
            .handleError((_request, _exception, _client, _model, _context) -> failureProgressEvent(_exception, _model, _context))
            // return success
            .done((_request, _response, _client, _model, _context) -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                .status(OperationStatus.SUCCESS)
                .resourceModel(_model)
                .build());
    }
}
