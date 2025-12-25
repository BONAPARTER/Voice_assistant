package nlp_parsing;

import java.util.*;
import java.util.regex.*;

public class CommandParserImpl {

    private static final Map<Pattern, CommandTemplate> templates = new HashMap<>();

    static {

        templates.put(
                Pattern.compile("(открой|запусти)\\s+браузер", Pattern.CASE_INSENSITIVE),
                new CommandTemplate("OPEN_BROWSER")
        );

        templates.put(
                Pattern.compile("(погода|погоду|погоде|погодой|погоду|погода)\\s+([а-яё]+)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE),
                new CommandTemplate("CHECK_WEATHER", "city")
        );

        templates.put(
                Pattern.compile("(который час|сколько время|который|сколько времени)", Pattern.CASE_INSENSITIVE),
                new CommandTemplate("CHECK_TIME")
        );
    }

    public Command parse(String text) {
        String lowerText = text.toLowerCase();

        for (Map.Entry<Pattern, CommandTemplate> entry : templates.entrySet()) {
            Pattern pattern = entry.getKey();
            CommandTemplate template = entry.getValue();

            Matcher matcher = pattern.matcher(lowerText);
            if (matcher.find()) {
                Map<String, String> parameters = new HashMap<>();

                if (template.getParameters().size() > 0 && matcher.groupCount() > 0) {
                    String cityName = matcher.group(2).trim();
                    parameters.put("city", cityName);
                }

                return new Command(template.getIntent(), parameters);
            }
        }

        return new Command("UNKNOWN", Map.of());
    }
}