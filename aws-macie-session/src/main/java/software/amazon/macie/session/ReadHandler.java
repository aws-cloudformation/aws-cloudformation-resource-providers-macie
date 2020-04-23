package software.amazon.macie.session;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.GetMacieSessionRequest;
import software.amazon.awssdk.services.macie2.model.GetMacieSessionResponse;
import software.amazon.awssdk.services.macie2.model.Macie2Exception;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandler<CallbackContext> {

    private final DateTimeFormatter df = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.systemDefault()); //'Tue, 3 Jun 2008 11:05:30 GMT'
    private AmazonWebServicesClientProxy clientProxy;
    private Macie2Client macie2Client;
    private Logger loggerProxy;
    private String awsAccountId;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        clientProxy = proxy;
        macie2Client = ClientBuilder.getClient();
        loggerProxy = logger;
        awsAccountId = request.getAwsAccountId();

        return getMacieSession(model);
    }

    private ProgressEvent<ResourceModel, CallbackContext> getMacieSession(ResourceModel model) {
        try {
            GetMacieSessionRequest macieSessionRequest = GetMacieSessionRequest.builder().build();
            GetMacieSessionResponse macieSession = clientProxy
                .injectCredentialsAndInvokeV2(macieSessionRequest, macie2Client::getMacieSession);
            return buildModelFromSession(model, macieSession);
        } catch (AwsServiceException e) {
            if (e instanceof Macie2Exception && e.statusCode() == HttpStatusCode.FORBIDDEN) {
                // Macie is not enabled
                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.FAILED)
                    .errorCode(HandlerErrorCode.NotFound)
                    .message(e.getMessage())
                    .build();
            }
            throw e;
        }
    }

    ProgressEvent<ResourceModel, CallbackContext> buildModelFromSession(ResourceModel model, GetMacieSessionResponse macieSession) {
        model.setStatus(macieSession.statusAsString());
        model.setAwsAccountId(awsAccountId);
        model.setFindingPublishingFrequency(macieSession.findingPublishingFrequencyAsString());
        model.setCreatedAt(df.format(macieSession.createdAt()));
        model.setUpdatedAt(df.format(macieSession.updatedAt()));
        model.setServiceRole(macieSession.serviceRole());

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(model)
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
