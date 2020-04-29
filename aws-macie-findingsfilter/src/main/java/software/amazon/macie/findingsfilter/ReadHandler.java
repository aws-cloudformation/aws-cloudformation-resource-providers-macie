package software.amazon.macie.findingsfilter;

import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.DescribeFindingsFilterRequest;
import software.amazon.awssdk.services.macie2.model.DescribeFindingsFilterResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseMacieFindingFilterHandler {
    protected static final String OPERATION = "macie2::DescribeFindingsFilter";

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
            .translateToServiceRequest((m) -> DescribeFindingsFilterRequest.builder()
                .filterId(model.getFilterId())
                .build())
            // Make a service call. Handler does not worry about credentials, they are auto injected
            .makeServiceCall((r, c) -> c.injectCredentialsAndInvokeV2(r, c.client()::describeFindingsFilter))
            // return appropriate failed progress event status by mapping business exceptions.
            .handleError((_request, _exception, _client, _model, _context) -> handleError(OPERATION, _exception, _model, _context, logger))
            // build model from successful response
            .done(this::buildModelFromResponse);
    }

    private ProgressEvent<ResourceModel, CallbackContext> buildModelFromResponse(final DescribeFindingsFilterRequest request,
        final DescribeFindingsFilterResponse response, final ProxyClient<Macie2Client> clientProxyClient, final ResourceModel model,
        final CallbackContext callbackContext) {
        model.setDescription(response.description());
        model.setName(response.name());
        model.setAction(response.actionAsString());
        model.setArn(response.arn());
        model.setPosition(response.position());
        model.setFindingCriteria(cfnModelFindingCriteria(response.findingCriteria()));
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(model)
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
