package software.amazon.macie.findingsfilter;

import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.DeleteFindingsFilterRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseMacieFindingFilterHandler {

    protected static final String OPERATION = "macie2::deleteFindingsFilter";

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
        return proxy.initiate(OPERATION, client, model, callbackContext)
            // transform Resource model properties to DeleteFindingsFilter API
            .translateToServiceRequest((m) -> DeleteFindingsFilterRequest.builder()
                .id(model.getId())
                .build())
            // Make a service call. Handler does not worry about credentials, they are auto injected
            .makeServiceCall((r, c) -> c.injectCredentialsAndInvokeV2(r, c.client()::deleteFindingsFilter))
            // return appropriate failed progress event status by mapping business exceptions.
            .handleError((_request, _exception, _client, _model, _context) -> handleError(OPERATION, _exception, _model, _context, logger))
            // return success
            .done((_request, _response, _client, _model, _context) -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                .status(OperationStatus.SUCCESS)
                .resourceModel(_model)
                .build());
    }
}
