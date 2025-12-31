package custom_exceptions;

public class MicrophoneNotSupported extends Exception {
    public MicrophoneNotSupported(String message) {
        super(message);
    }
}
