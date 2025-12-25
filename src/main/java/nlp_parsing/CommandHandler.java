package nlp_parsing;

import commands.CommandAction;
import commands.OpenBrowserCommand;
import commands.CheckWeatherCommand;
import commands.CheckTimeCommand;

import java.util.HashMap;
import java.util.Map;

public class CommandHandler {

    private final Map<String, CommandAction> handlers = new HashMap<>();

    public CommandHandler() {
        registerHandlers();
    }

    private void registerHandlers() {
        handlers.put("OPEN_BROWSER", new OpenBrowserCommand());
        handlers.put("CHECK_WEATHER", new CheckWeatherCommand());
        handlers.put("CHECK_TIME", new CheckTimeCommand());

        handlers.put("UNKNOWN", parameters -> System.out.println("Неизвестная команда"));
    }

    /**
     * Обрабатывает команду на основе её намерения.
     *
     * @param command объект команды
     */
    public void handle(Command command) {
        String intent = command.getIntent();
        Map<String, String> parameters = command.getParameters();

        CommandAction handler = handlers.getOrDefault(intent, handlers.get("UNKNOWN"));

        handler.execute(parameters);
    }
}