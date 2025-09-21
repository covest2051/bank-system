package util;

import entity.Client;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Service
public class ClientIdValidator implements Validator {

    private static final String CLIENT_ID_PATTERN = "^(\\d{2})(\\d{2})(\\d{8})$";

    @Override
    public boolean supports(Class<?> clazz) {
        return Client.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Client c = (Client) target;

        String clientId = c.getClientId();

        if (clientId == null || clientId.isBlank()) {
            errors.rejectValue("clientId", "clientId.empty", "clientId can not be blank");
            return;
        }

        if (!clientId.matches(CLIENT_ID_PATTERN)) {
            errors.rejectValue("clientId", "clientId.invalidFormat",
                    "Wrong clientId format. Expected: XXFFNNNNNNNN");
        }
    }
}
