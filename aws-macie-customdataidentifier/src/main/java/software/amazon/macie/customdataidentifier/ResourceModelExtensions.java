package software.amazon.macie.customdataidentifier;

import org.json.JSONObject;

public class ResourceModelExtensions {

    public static JSONObject getPrimaryIdentifier(final ResourceModel model) {
        final JSONObject identifier = new JSONObject();
        identifier.append("Id", model.getId());
        return identifier;
    }
}
