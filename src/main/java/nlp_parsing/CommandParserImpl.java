package nlp_parsing;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandParserImpl implements CommandParser {

    private final Map<Pattern, CommandTemplate> commandPatterns = new HashMap<>();

    public CommandParserImpl() {
        addPattern("откр(ой|ывает?) браузер", "OPEN_BROWSER");
        addPattern("(запусти|включи) музыку", "PLAY_MUSIC");
        addPattern("(какая|узнай|подскажи) погод(у|а)( в (.+))?", "CHECK_WEATHER", "city");
        addPattern("(сколько времени|текущее время)", "CHECK_TIME");
    }

    private void addPattern(String regex, String intent, String... parameters) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        commandPatterns.put(pattern, new CommandTemplate(intent, Arrays.asList(parameters)));
    }

    @Override
    public Command parse(String text) {
        for (Map.Entry<Pattern, CommandTemplate> entry : commandPatterns.entrySet()) {
            Pattern pattern = entry.getKey();
            CommandTemplate template = entry.getValue();

            Matcher matcher = pattern.matcher(text);
            if (matcher.matches()) {
                Map<String, String> parameters = new HashMap<>();
                List<String> parameterNames = template.getParameters();

                for (int i = 0; i < parameterNames.size(); i++) {
                    String paramName = parameterNames.get(i);
                    String paramValue = matcher.group(i + 1); // Группы начинаются с 1

                    if (paramValue != null) {
                        parameters.put(paramName, paramValue.trim());
                    } else {
                        parameters.put(paramName, "");
                    }
                }

                return new Command(template.getIntent(), parameters);
            }
        }

        return new Command("UNKNOWN", Collections.emptyMap());
    }
}
