package software.amazon.macie.classificationjob;

import java.util.Map;
import org.json.JSONObject;
import org.json.JSONTokener;

class Configuration extends BaseConfiguration {

    public Configuration() {
        super("aws-macie-classificationjob.json");
    }
}
