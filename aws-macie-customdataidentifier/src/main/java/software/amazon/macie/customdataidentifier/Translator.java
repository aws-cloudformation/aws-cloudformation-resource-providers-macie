package software.amazon.macie.customdataidentifier;

import software.amazon.awssdk.services.macie2.model.CreateCustomDataIdentifierRequest;
import software.amazon.awssdk.services.macie2.model.CreateCustomDataIdentifierResponse;
import software.amazon.awssdk.services.macie2.model.DeleteCustomDataIdentifierRequest;
import software.amazon.awssdk.services.macie2.model.GetCustomDataIdentifierRequest;
import software.amazon.awssdk.services.macie2.model.GetCustomDataIdentifierResponse;
import software.amazon.awssdk.services.macie2.model.ListCustomDataIdentifiersRequest;

/**
 * This class is a centralized placeholder for
 *  - api request construction
 *  - object translation to/from aws sdk
 *  - resource model construction for read/list handlers
 */

public class Translator {

  private static final int LIST_CDIS_MAX_RESULTS = 200;

  /**
   * Request to create a resource
   * @param model resource model
   * @return awsRequest the aws service request to create a resource
   */
  static CreateCustomDataIdentifierRequest translateToCreateRequest(final ResourceModel model, final String clientToken) {
    return CreateCustomDataIdentifierRequest.builder()
                                           .clientToken(clientToken)
                                           .name(model.getName())
                                           .description(model.getDescription())
                                           .ignoreWords(model.getIgnoreWords())
                                           .keywords(model.getKeywords())
                                           .maximumMatchDistance(model.getMaximumMatchDistance())
                                           .regex(model.getRegex())
                                           .resourceId(model.getResourceId())
                                           .build();
  }

  /**
   * Translates resource object from sdk into a resource model
   * @param request resource create request
   * @param response resource create response
   * @return model resource model
   */
  static ResourceModel translateFromCreateResponse(final CreateCustomDataIdentifierRequest request,
                                                   final CreateCustomDataIdentifierResponse response) {
    return ResourceModel.builder()
                        .id(response.customDataIdentifierId())
                        .name(request.name())
                        .description(request.description())
                        .ignoreWords(request.ignoreWords())
                        .keywords(request.keywords())
                        .maximumMatchDistance(request.maximumMatchDistance())
                        .regex(request.regex())
                        .build();
  }

  /**
   * Request to read a resource
   * @param model resource model
   * @return awsRequest the aws service request to describe a resource
   */
  static GetCustomDataIdentifierRequest translateToReadRequest(final ResourceModel model) {
    return GetCustomDataIdentifierRequest.builder()
                                        .id(model.getId())
                                        .build();
  }

  /**
   * Translates resource object from sdk into a resource model
   * @param awsResponse the aws service describe resource response
   * @return model resource model
   */
  static ResourceModel translateFromReadResponse(final GetCustomDataIdentifierResponse awsResponse) {
    return ResourceModel.builder()
                        .id(awsResponse.id())
                        .arn(awsResponse.arn())
                        .name(awsResponse.name())
                        .description(awsResponse.description())
                        .ignoreWords(awsResponse.ignoreWords())
                        .keywords(awsResponse.keywords())
                        .maximumMatchDistance(awsResponse.maximumMatchDistance())
                        .regex(awsResponse.regex())
                        .build();
  }

  /**
   * Request to delete a resource
   * @param model resource model
   * @return awsRequest the aws service request to delete a resource
   */
  static DeleteCustomDataIdentifierRequest translateToDeleteRequest(final ResourceModel model) {
    return DeleteCustomDataIdentifierRequest.builder()
                                           .id(model.getId())
                                           .build();
  }

  /**
   * Request to update properties of a previously created resource
   * @param nextToken token passed to the aws service describe resource request
   * @return awsRequest the aws service request to describe resources within aws account
   */
  static ListCustomDataIdentifiersRequest translateToListRequest(final String nextToken) {
    return ListCustomDataIdentifiersRequest.builder().maxResults(LIST_CDIS_MAX_RESULTS).nextToken(nextToken).build();
  }
}
