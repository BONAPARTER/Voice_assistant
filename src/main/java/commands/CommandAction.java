package commands;

import java.util.Map;

public interface CommandAction {
    void execute(Map<String, String> parameters);
}
