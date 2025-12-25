package commands;


import java.time.LocalDateTime;
import java.util.Map;

import static java.lang.System.out;

public class CheckTimeCommand implements CommandAction {

    @Override
    public void execute(Map<String, String> parameters) {
        LocalDateTime time = LocalDateTime.now();
        out.println("Текущее время: " + time);
    }
}
