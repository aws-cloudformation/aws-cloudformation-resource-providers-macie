package software.amazon.macie.member;

import java.util.Map;
import org.json.JSONObject;
import org.json.JSONTokener;

class Configuration extends BaseConfiguration {

    public Configuration() {
        super("aws-macie-member.json");
    }
}
