package nlp_parsing;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CommandHandler {
    private final Map<String, Consumer<Command>> handlers = new HashMap<>();

    public CommandHandler() {
        handlers.put("OPEN_BROWSER", this::openBrowser);
        handlers.put("PLAY_MUSIC", this::playMusic);
        handlers.put("CHECK_WEATHER", this::checkWeather);
        handlers.put("CHECK_TIME", this::checkTime);
        handlers.put("UNKNOWN", this::handleUnknownCommand);
    }

    public void handle(Command command) {
        String intent = command.getIntent();
        Consumer<Command> handler = handlers.getOrDefault(intent, this::handleUnknownCommand);
        handler.accept(command);
    }

    private void openBrowser(Command command) {
        System.out.println("Открываю браузер...");
    }

    private void playMusic(Command command) {
        System.out.println("Включаю музыку...");
    }

    private void checkWeather(Command command) {
        System.out.println("Проверяю погоду...");
    }

    private void checkTime(Command command) {
        System.out.println("Текущее время: " + java.time.LocalTime.now());
    }

    private void handleUnknownCommand(Command command) {
        System.out.println("Неизвестная команда: " + command);
    }
}
