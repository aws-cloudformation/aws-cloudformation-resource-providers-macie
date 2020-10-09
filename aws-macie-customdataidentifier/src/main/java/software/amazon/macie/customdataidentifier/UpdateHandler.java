package software.amazon.macie.customdataidentifier;

import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class UpdateHandler extends BaseHandlerStd {
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<Macie2Client> proxyClient,
        final Logger logger
    ) {
        logger.log(String.format("AWS Account ID [%s] triggered %s dummy update handler",
            request.getAwsAccountId(), ResourceModel.TYPE_NAME));

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(request.getDesiredResourceState())
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
