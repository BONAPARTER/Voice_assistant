package ui;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LanguageSelectorUI {

    private static final Map<String, String> LANGUAGE_MODELS = new HashMap<>();
    private static final Map<String, String> LANGUAGE_CODES = new HashMap<>();

    static {
        LANGUAGE_MODELS.put("Русский", "src/main/resources/models/vosk-model-small-ru-0.22");
        LANGUAGE_MODELS.put("English", "src/main/resources/models/vosk-model-small-en-us-0.15");

        LANGUAGE_CODES.put("Русский", "ru");
        LANGUAGE_CODES.put("English", "en");
    }

    public static void show(LanguageSelectionListener listener) {
        JFrame frame = new JFrame("Voice Assistant - Language Selection");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        ImageIcon backgroundImage = new ImageIcon(
                Objects.requireNonNull(
                        LanguageSelectorUI.class.getClassLoader().getResource("sphere.gif"),
                        "sphere.gif not found in resources"
                )
        );

        JLabel background = new JLabel(backgroundImage);
        background.setLayout(new GridBagLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(400, 250));

        JComboBox<String> languageComboBox =
                new JComboBox<>(new String[]{"Русский", "English"});
        JButton selectButton = new JButton("Подтвердить / Confirm");

        selectButton.setForeground(Color.WHITE);
        selectButton.setBackground(Color.BLACK);

        selectButton.addActionListener(e -> {
            String selectedLanguage = (String) languageComboBox.getSelectedItem();
            String modelPath = LANGUAGE_MODELS.get(selectedLanguage);
            String languageCode = LANGUAGE_CODES.get(selectedLanguage);

            frame.dispose();
            listener.onLanguageSelected(modelPath, languageCode);
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Выберите язык / Select language"), gbc);

        gbc.gridy = 1;
        panel.add(languageComboBox, gbc);

        gbc.gridy = 2;
        panel.add(selectButton, gbc);

        background.add(panel);
        frame.setContentPane(background);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
