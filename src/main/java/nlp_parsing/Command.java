package nlp_parsing;

import java.util.Map;

public class Command {
    private final String intent;
    private final Map<String, String> parameters;

    public Command(String intent, Map<String, String> parameters) {
        this.intent = intent;
        this.parameters = parameters;
    }

    public String getIntent() {
        return intent;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "Command{" +
                "intent='" + intent + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}
