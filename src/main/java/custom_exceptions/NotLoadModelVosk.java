package custom_exceptions;

import java.io.IOException;

public class NotLoadModelVosk extends IOException {
    public NotLoadModelVosk(String message) {
        super(message);
    }
}
