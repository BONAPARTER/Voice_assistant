package nlp_parsing;

import java.util.*;
import java.util.regex.*;

public class CommandParserImpl {
    private static final Map<String, Map<Pattern, CommandTemplate>> LANGUAGE_TEMPLATES = new HashMap<>();

    static {

        Map<Pattern, CommandTemplate> ruTemplates = new HashMap<>();
        ruTemplates.put(
                Pattern.compile("(открой|запусти|перейди на)\\s*(?:браузер|сайт|страницу)?", Pattern.CASE_INSENSITIVE),
                new CommandTemplate("OPEN_BROWSER")
        );
        ruTemplates.put(
                Pattern.compile("(?:какая|узнай|подскажи|покажи)\\s*погод(?:у|а)\\s*(?:в|на|для)?\\s*([а-яё]+)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE),
                new CommandTemplate("CHECK_WEATHER", "city")
        );
        ruTemplates.put(
                Pattern.compile("(?:который|сколько)\\s*(?:час|времени)|текущее\\s*время", Pattern.CASE_INSENSITIVE),
                new CommandTemplate("CHECK_TIME")
        );

        Map<Pattern, CommandTemplate> enTemplates = new HashMap<>();
        enTemplates.put(
                Pattern.compile("(open|launch|go to)\\s*(?:browser|site|page)?", Pattern.CASE_INSENSITIVE),
                new CommandTemplate("OPEN_BROWSER")
        );
        enTemplates.put(
                Pattern.compile("(?:what's|check|tell me)\\s*the\\s*weather\\s*(?:in|at)?\\s*([a-z]+)", Pattern.CASE_INSENSITIVE),
                new CommandTemplate("CHECK_WEATHER", "city")
        );
        enTemplates.put(
                Pattern.compile("(?:what time|current time|time now)", Pattern.CASE_INSENSITIVE),
                new CommandTemplate("CHECK_TIME")
        );

        LANGUAGE_TEMPLATES.put("ru", ruTemplates);
        LANGUAGE_TEMPLATES.put("en", enTemplates);
    }

    private final Map<Pattern, CommandTemplate> templates;

    public CommandParserImpl(String language) {
        this.templates = LANGUAGE_TEMPLATES.getOrDefault(language, LANGUAGE_TEMPLATES.get("ru"));
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
                    String cityName = matcher.group(1).trim();
                    parameters.put("city", cityName);
                }

                return new Command(template.getIntent(), parameters);
            }
        }

        return new Command("UNKNOWN", Map.of());
    }
}