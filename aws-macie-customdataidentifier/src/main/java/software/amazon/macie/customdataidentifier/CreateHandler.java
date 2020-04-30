package software.amazon.macie.customdataidentifier;

import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.CreateCustomDataIdentifierRequest;
import software.amazon.awssdk.services.macie2.model.CreateCustomDataIdentifierResponse;
import software.amazon.awssdk.services.macie2.model.Macie2Exception;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class CreateHandler extends BaseHandlerStd {

    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<Macie2Client> proxyClient,
        final Logger logger
    ) {
        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();

        // If your service API throws 'ResourceAlreadyExistsException' for create requests then CreateHandler can return just proxy.initiate construction
        // STEP 1.0 [initialize a proxy context]
        return ProgressEvent.progress(model, callbackContext)
                .then(progress ->
                    proxy.initiate("AWS-Macie-CustomDataIdentifier::Create", proxyClient, model, callbackContext)

                        // STEP 1.1 [construct a body of a request]
                        .translateToServiceRequest(_model -> Translator.translateToCreateRequest(_model, request.getClientRequestToken()))

                        // STEP 1.2 [make an api call]
                        .makeServiceCall(this::createResource)

                        // STEP 1.4 [stabilize the resource]
                        .stabilize(this::stabilizedOnCreate)

                        .progress()
                )
                // STEP 1.5 [describe call/chain to return the resource model]
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    /**
     * Implement client invocation of the create request through the proxyClient, which is already initialised with
     * caller credentials, correct region and retry settings
     *
     * @param request the aws service request to create a resource
     * @param proxyClient the aws service client to make the call
     * @return create resource response
     */
    private CreateCustomDataIdentifierResponse createResource(
        final CreateCustomDataIdentifierRequest request,
        final ProxyClient<Macie2Client> proxyClient
    ) {
        CreateCustomDataIdentifierResponse response;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::createCustomDataIdentifier);
        } catch (final Macie2Exception e) {
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }

        logger.log(String.format("%s successfully created.", ResourceModel.TYPE_NAME));
        return response;
    }

    /**
     * Using stabilize to pass the returned custom data identifier ID from the Create API to the Read API.
     *
     * @param request the aws service request to create a resource
     * @param response the aws service response to create a resource
     * @param proxyClient the aws service client to make the call
     * @param callbackContext callback context
     * @return boolean state of stabilized or not
     */
    private boolean stabilizedOnCreate(
            final CreateCustomDataIdentifierRequest request,
            final CreateCustomDataIdentifierResponse response,
            final ProxyClient<Macie2Client> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext
    ) {
        model.setId(response.customDataIdentifierId());
        return true;
    }
}
