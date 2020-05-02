package software.amazon.macie.findingsfilter;

import java.util.List;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.ListFindingsFiltersRequest;
import software.amazon.awssdk.services.macie2.model.ListFindingsFiltersResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ListHandler extends BaseMacieFindingFilterHandler {

    protected static final String OPERATION = "macie2::ListFindingsFilters";
    private static final int MAX_LIST_RESULTS = 50;

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
            // transform Resource model properties to ListFindingsFilters API
            .translateToServiceRequest((m) -> ListFindingsFiltersRequest.builder()
                .maxResults(MAX_LIST_RESULTS)
                .nextToken(request.getNextToken())
                .build())
            // Make a service call. Handler does not worry about credentials, they are auto injected
            .makeServiceCall((r, c) -> c.injectCredentialsAndInvokeV2(r, c.client()::listFindingsFilters))
            // return appropriate failed progress event status by mapping business exceptions.
            .handleError((_request, _exception, _client, _model, _context) -> handleError(OPERATION, _exception, _model, _context, logger))
            // build model from successful response
            .done(this::buildModelFromResponse);
    }

    private ProgressEvent<ResourceModel, CallbackContext> buildModelFromResponse(ListFindingsFiltersRequest request,
        ListFindingsFiltersResponse response,
        ProxyClient<Macie2Client> clientProxyClient, ResourceModel model, CallbackContext context) {
        List<FilterListItem> filterListItems = response.findingsFilterListItems().stream()
            .map(item -> FilterListItem.builder().filterId(item.filterId()).name(item.name()).build())
            .collect(Collectors.toList());
        model.setFilterListItems(filterListItems);
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .status(OperationStatus.SUCCESS)
            .resourceModel(model)
            .nextToken(response.nextToken())
            .build();
    }
}
