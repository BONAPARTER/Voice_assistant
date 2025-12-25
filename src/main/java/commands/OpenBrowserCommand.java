package commands;


import java.awt.*;
import java.net.URI;
import java.util.Map;

import static java.lang.System.err;
import static java.lang.System.out;

public class OpenBrowserCommand implements CommandAction {

    @Override
    public void execute(Map<String, String> parameters) {
        String site = parameters.getOrDefault("site", "https://google.com");

        try {
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(new URI(site));
            out.println("Открываю браузер: " + site);
        } catch (Exception e) {
            err.println("Не удалось открыть браузер: " + e.getMessage());
        }
    }
}
