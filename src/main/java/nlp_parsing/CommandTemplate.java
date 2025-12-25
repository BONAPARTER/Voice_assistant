package nlp_parsing;

import java.util.List;

public class CommandTemplate {
    private final String intent;
    private final List<String> parameters;

    public CommandTemplate(String intent, String... parameters) {
        this.intent = intent;
        this.parameters = List.of(parameters);
    }

    public String getIntent() {
        return intent;
    }

    public List<String> getParameters() {
        return parameters;
    }
}