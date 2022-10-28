package neoflix;

import java.util.*;

public class ValidationException extends RuntimeException {
    private final Map<String,String> details = new HashMap<>();
    public ValidationException(String message, Map<String,String> details) {
        super(message);
        if (details!=null) this.details.putAll(details);
    }
    public Map<String,String> getDetails() {
        return details;
    }
}