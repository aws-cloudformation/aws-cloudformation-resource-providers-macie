package software.amazon.macie.session;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.cloudformation.proxy.StdCallbackContext;

@Data
@NoArgsConstructor
@Builder
public class CallbackContext extends StdCallbackContext {
}
