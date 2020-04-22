package software.amazon.macie.findingsfilter;

import org.json.JSONObject;

public class ResourceModelExtensions {

    public static JSONObject getPrimaryIdentifier(final ResourceModel model) {
        final JSONObject identifier = new JSONObject();
        identifier.append("FilterId", model.getFilterId());
        return identifier;
    }
}
