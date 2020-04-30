package software.amazon.macie.customdataidentifier;

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
}
