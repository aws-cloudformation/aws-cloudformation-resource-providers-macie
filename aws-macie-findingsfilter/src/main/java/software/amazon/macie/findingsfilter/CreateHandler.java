package software.amazon.macie.findingsfilter;

import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.CreateFindingsFilterRequest;
import software.amazon.awssdk.services.macie2.model.CreateFindingsFilterResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class CreateHandler extends BaseMacieFindingFilterHandler {

    protected static final String OPERATION = "macie2::CreateFindingsFilter";

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
            // transform Resource model properties to CreateFindingsFilter API
            .translateToServiceRequest((m) -> createFindingFilterRequest(request.getClientRequestToken(), m))
            // Make a service call. Handler does not worry about credentials, they are auto injected
            .makeServiceCall((r, c) -> c.injectCredentialsAndInvokeV2(r, c.client()::createFindingsFilter))
            // return appropriate failed progress event status by mapping business exceptions.
            .handleError((_request, _exception, _client, _model, _context) -> handleError(OPERATION, _exception, _model, _context, logger))
            // Update model so identifier can be used by subsequent read call.
            .done(this::buildModelFromResponse)
            // we then delegate to ReadHandler to read the live state and send back successful response.
            .then((r) -> new ReadHandler()
                .handleRequest(proxy, request, callbackContext, client, logger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> buildModelFromResponse(final CreateFindingsFilterRequest createFindingsFilterRequest,
        final CreateFindingsFilterResponse response, final ProxyClient<Macie2Client> clientProxyClient, final ResourceModel model,
        final CallbackContext callbackContext) {

        model.setFilterId(response.filterId());
        model.setArn(response.arn());
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(model)
            .status(OperationStatus.IN_PROGRESS)
            .build();
    }
}
