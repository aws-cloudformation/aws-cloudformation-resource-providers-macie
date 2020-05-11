package software.amazon.macie.customdataidentifier;

import org.apache.commons.lang3.exception.ExceptionUtils;
import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.GetCustomDataIdentifierRequest;
import software.amazon.awssdk.services.macie2.model.GetCustomDataIdentifierResponse;
import software.amazon.awssdk.services.macie2.model.Macie2Exception;
import software.amazon.awssdk.services.macie2.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandlerStd {
    private Logger logger;
    private ResourceHandlerRequest<ResourceModel> request;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
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
        return proxy.initiate("AWS-Macie-CustomDataIdentifier::Read", proxyClient, model, callbackContext)

            // STEP 2 [construct a body of a request]
            .translateToServiceRequest(Translator::translateToReadRequest)

            // STEP 3 [make an api call]
            .makeServiceCall(this::readResource)

            // STEP 4 [gather all properties of the resource]
            .done(this::constructResourceModelFromResponse);
    }

    /**
     * Implement client invocation of the read getCustomDataIdentifierRequest through the proxyClient, which is already initialised with
     * caller credentials, correct region and retry settings
     * @param getCustomDataIdentifierRequest the aws service getCustomDataIdentifierRequest to describe a resource
     * @param proxyClient the aws service client to make the call
     * @return describe resource response
     */
    private GetCustomDataIdentifierResponse readResource(
        final GetCustomDataIdentifierRequest getCustomDataIdentifierRequest,
        final ProxyClient<Macie2Client> proxyClient
    ) {
        GetCustomDataIdentifierResponse response;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(getCustomDataIdentifierRequest, proxyClient.client()::getCustomDataIdentifier);
        } catch (final ResourceNotFoundException e) {
            logger.log(String.format(EXCEPTION_MESSAGE, request.getAwsAccountId(), ExceptionUtils.getStackTrace(e)));
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, e.getMessage());
        } catch (final Macie2Exception e) {
            logger.log(String.format(EXCEPTION_MESSAGE, request.getAwsAccountId(), ExceptionUtils.getStackTrace(e)));
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }

        logger.log(String.format("%s has successfully been read.", ResourceModel.TYPE_NAME));
        return response;
    }

    /**
     * Build the Progress Event object from SDK response.
     * @param response the aws service describe resource response
     * @return progressEvent indicating success, in progress with delay callback or failed state
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
        final GetCustomDataIdentifierResponse response
    ) {
        return ProgressEvent.defaultSuccessHandler(Translator.translateFromReadResponse(response));
    }
}
