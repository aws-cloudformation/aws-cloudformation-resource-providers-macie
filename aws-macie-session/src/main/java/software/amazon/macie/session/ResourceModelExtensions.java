package software.amazon.macie.session;

import org.json.JSONObject;

public class ResourceModelExtensions {

    public static JSONObject getPrimaryIdentifier(final ResourceModel model) {
        final JSONObject identifier = new JSONObject();
        identifier.append("ServiceRole", model.getServiceRole());
        return identifier;
    }
}
