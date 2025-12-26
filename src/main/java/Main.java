import java.nio.ByteBuffer;
import activation_recognition.ActivationRecognizer;
import audio_capture.AudioCapture;
import audio_capture.MicrophoneCapture;
import speech_recognition.VoskRecognizer;
import nlp_parsing.Command;
import nlp_parsing.CommandHandler;
import nlp_parsing.CommandParserImpl;
import ui.LanguageSelectorUI;

import static java.lang.System.out;

public class Main {

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            // Передаём колбэк для обработки выбора языка
            LanguageSelectorUI.show((modelPath, languageCode) -> {
                try {
                    startVoiceAssistant(modelPath, languageCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else {
            startVoiceAssistant(args[0], args.length > 1 ? args[1] : "ru");
        }
    }


    private static void startVoiceAssistant(String modelPath, String language) throws Exception {
            out.println("Запуск голосового ассистента...");
            out.println("Модель: " + modelPath);
            out.println("Язык: " + language);

            AudioCapture capture = new MicrophoneCapture();
            capture.startCapture();

            ActivationRecognizer wakeRecognizer = new ActivationRecognizer(modelPath, language);
            VoskRecognizer commandRecognizer = new VoskRecognizer(modelPath, language);

            boolean listeningForCommand = false;
            long commandTimeout = 0;
            final long TIMEOUT_MS = 8000;

            CommandParserImpl parser = new CommandParserImpl(language);
            CommandHandler handler = new CommandHandler();


            while (true) {

                if (!listeningForCommand && !wakeRecognizer.isRunning())
                    wakeRecognizer.start();

                ByteBuffer chunk = capture.getCapturedData();
                if (chunk != null && chunk.hasRemaining()) {
                    byte[] data = new byte[chunk.remaining()];
                    chunk.get(data);

                    if (!listeningForCommand && wakeRecognizer.processChunk(data)) {
                        String keyword = wakeRecognizer.getDetectedKeyword();

                        if (keyword != null && !keyword.trim().isEmpty()) {
                            wakeRecognizer.stop();
                            out.println(">>> ЛЮМЬЕР УСЛЫШАЛ! (" + keyword + ") <<<");
                            listeningForCommand = true;
                            commandTimeout = System.currentTimeMillis() + TIMEOUT_MS;
                            commandRecognizer.start();
                        }
                    }

                    if (listeningForCommand) {
                        boolean commandReady = commandRecognizer.acceptAudioChunk(data);

                        if (commandReady) {
                            String text = commandRecognizer.getCleanTextResult();

                            if (!text.isEmpty()) {
                                out.println("Команда: " + text);

                                Command command = parser.parse(text);

                                handler.handle(command);
                            }

                            listeningForCommand = false;
                            commandRecognizer.stop();
                            wakeRecognizer.start();
                        }

                        if (System.currentTimeMillis() > commandTimeout) {
                            out.println("Таймаут команды — возвращаемся к ожиданию Люмьера?");
                            listeningForCommand = false;
                            commandRecognizer.stop();
                            wakeRecognizer.start();
                        }
                    }
                }

                Thread.sleep(20);
            }
        }
}