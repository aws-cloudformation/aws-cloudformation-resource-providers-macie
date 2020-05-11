package software.amazon.macie.customdataidentifier;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.exception.ExceptionUtils;
import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.CustomDataIdentifierSummary;
import software.amazon.awssdk.services.macie2.model.ListCustomDataIdentifiersRequest;
import software.amazon.awssdk.services.macie2.model.ListCustomDataIdentifiersResponse;
import software.amazon.awssdk.services.macie2.model.Macie2Exception;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ListHandler extends BaseHandlerStd {

    private Logger logger;
    private ResourceHandlerRequest<ResourceModel> request;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<Macie2Client> proxyClient,
        final Logger logger
    ) {
        this.logger = logger;
        this.request = request;

        final ResourceModel model = request.getDesiredResourceState();

        // STEP 1 [initialize a proxy context]
        return proxy.initiate("AWS-Macie-CustomDataIdentifier::List", proxyClient, model, callbackContext)

            // STEP 2 [construct a body of a request]
            .translateToServiceRequest(resourceModel -> Translator.translateToListRequest(request.getNextToken()))

            // STEP 3 [make an api call]
            .makeServiceCall(this::listResources)
            .done(this::constructResourceModelFromResponse);
    }

    /**
     * Implement client invocation of the list request through the proxyClient, which is already initialised with caller credentials, correct region and retry
     * settings
     *
     * @param listCustomDataIdentifiersRequest the aws service request to list resources
     * @param proxyClient                      the aws service client to make the call
     * @return create resource response
     */
    private ListCustomDataIdentifiersResponse listResources(
        final ListCustomDataIdentifiersRequest listCustomDataIdentifiersRequest,
        final ProxyClient<Macie2Client> proxyClient
    ) {
        ListCustomDataIdentifiersResponse response;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(listCustomDataIdentifiersRequest, proxyClient.client()::listCustomDataIdentifiers);
        } catch (final Macie2Exception e) {
            logger.log(String.format(EXCEPTION_MESSAGE, request.getAwsAccountId(), ExceptionUtils.getStackTrace(e)));
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }

        logger.log(String.format("%s successfully listed.", ResourceModel.TYPE_NAME));
        return response;
    }

    /**
     * Build the Progress Event object from SDK response.
     *
     * @param listResponse the aws service describe resource response
     * @return progressEvent indicating success, in progress with delay callback or failed state
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
        final ListCustomDataIdentifiersResponse listResponse
    ) {
        final List<ResourceModel> models = new ArrayList<>();
        String nextToken = listResponse.nextToken();

        // STEP 4 [construct resource models]
        for (CustomDataIdentifierSummary summary : listResponse.items()) {
            models.add(
                ResourceModel.builder()
                    .id(summary.id())
                    .arn(summary.arn())
                    .name(summary.name())
                    .description(summary.description())
                    .createdAt(summary.createdAt().toString())
                    .build()
            );
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModels(models)
            .nextToken(nextToken)
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
