package software.amazon.macie.customdataidentifier;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.DeleteCustomDataIdentifierRequest;
import software.amazon.awssdk.services.macie2.model.DeleteCustomDataIdentifierResponse;
import software.amazon.awssdk.services.macie2.model.Macie2Exception;
import software.amazon.awssdk.services.macie2.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {
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

        return ProgressEvent.progress(model, callbackContext)

            // STEP 1.0 [delete/stabilize progress chain - required for resource deletion]
            .then(progress ->
                // If your service API throws 'ResourceNotFoundException' for delete requests then DeleteHandler can return just proxy.initiate construction
                // STEP 1.0 [initialize a proxy context]
                proxy.initiate("AWS-Macie-CustomDataIdentifier::Delete", proxyClient, model, callbackContext)

                    // STEP 1.1 [construct a body of a request]
                    .translateToServiceRequest(Translator::translateToDeleteRequest)

                    // STEP 1.2 [make an api call]
                    .makeServiceCall(this::deleteResource)

                    // STEP 1.3 [stabilize step is not necessarily required but typically involves describing the resource until it is in a certain status, though it can take many forms]
                    // for more information -> https://docs.aws.amazon.com/cloudformation-cli/latest/userguide/resource-type-test-contract.html
                    .stabilize(this::stabilizedOnDelete)

                    .success());
    }

    /**
     * Implement client invocation of the delete request through the proxyClient, which is already initialised with
     * caller credentials, correct region and retry settings
     * @param request the aws service request to delete a resource
     * @param proxyClient the aws service client to make the call
     * @return delete resource response
     */
    private DeleteCustomDataIdentifierResponse deleteResource(
        final DeleteCustomDataIdentifierRequest request,
        final ProxyClient<Macie2Client> proxyClient
    ) {
        DeleteCustomDataIdentifierResponse response;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::deleteCustomDataIdentifier);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, e.getMessage()); // CloudFormation catches this exception and quietly succeeds
        } catch (final Macie2Exception e) {
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }

        logger.log(String.format("%s successfully deleted.", ResourceModel.TYPE_NAME));
        return response;
    }

    /**
     * If deletion of your resource requires some form of stabilization (e.g. propagation delay)
     * for more information -> https://docs.aws.amazon.com/cloudformation-cli/latest/userguide/resource-type-test-contract.html
     * @param request the aws service request to delete a resource
     * @param response the aws service response to delete a resource
     * @param proxyClient the aws service client to make the call
     * @param model resource model
     * @param callbackContext callback context
     * @return boolean state of stabilized or not
     */
    private boolean stabilizedOnDelete(
        final DeleteCustomDataIdentifierRequest request,
        final DeleteCustomDataIdentifierResponse response,
        final ProxyClient<Macie2Client> proxyClient,
        final ResourceModel model,
        final CallbackContext callbackContext
    ) {
        final boolean stabilized = isStabilized(model, proxyClient);

        if (stabilized) {
            logger.log(String.format("%s has successfully been deleted. Stabilized.", ResourceModel.TYPE_NAME));
        }

        return stabilized;
    }

    private boolean isStabilized(final ResourceModel model, final ProxyClient<Macie2Client> proxyClient) {
        try {
            String id = proxyClient.injectCredentialsAndInvokeV2(Translator.translateToReadRequest(model), proxyClient.client()::getCustomDataIdentifier).id();
            return !StringUtils.hasValue(id);
        } catch (final ResourceNotFoundException e) {
            return true;
        } catch (final Macie2Exception e) {
            // any other exception means the resource was not successfully deleted (throttling, internal server error, etc.)
            return false;
        }
    }
}
