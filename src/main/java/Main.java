import java.nio.ByteBuffer;

import activation_recognition.ActivationRecognizer;
import audio_capture.AudioCapture;
import audio_capture.MicrophoneCapture;
import speech_recognition.VoskRecognizer;

public class Main {

    public static void main(String[] args) throws Exception {
        AudioCapture capture = new MicrophoneCapture();
        capture.startCapture();

        ActivationRecognizer wakeRecognizer = new ActivationRecognizer("src/main/resources/models/vosk-model-small-ru-0.22");
        VoskRecognizer commandRecognizer = new VoskRecognizer("src/main/resources/models/vosk-model-small-ru-0.22"); // твой обычный

        boolean listeningForCommand = false;
        long commandTimeout = 0;
        final long TIMEOUT_MS = 8000;

        System.out.println(wakeRecognizer.buildGrammarString());

        while (true) {
            if (!listeningForCommand && !wakeRecognizer.isRunning()) {
                wakeRecognizer.start();
            }

            ByteBuffer chunk = capture.getCapturedData();
            if (chunk != null && chunk.hasRemaining()) {
                byte[] data = new byte[chunk.remaining()];
                chunk.get(data);

                if (wakeRecognizer.processChunk(data)) {

                    String keyword = wakeRecognizer.getDetectedKeyword();

                    if (keyword != null && !keyword.trim().isEmpty()) {
                        wakeRecognizer.stop();
                        System.out.println(">>> ЛЮМЬЕР УСЛЫШАН! (" + keyword + ") <<<");
                        listeningForCommand = true;
                        commandTimeout = System.currentTimeMillis() + TIMEOUT_MS;
                        commandRecognizer.start();
                    }
                }

                if (listeningForCommand) {
                    boolean commandReady = commandRecognizer.acceptAudioChunk(data);

                    if (commandReady) {
                        String command = commandRecognizer.getCleanTextResult();
                        if (!command.isEmpty()) {
                            System.out.println("Команда: " + command);
                        }
                        listeningForCommand = false;
                        commandRecognizer.stop();
                    }

                    if (System.currentTimeMillis() > commandTimeout) {
                        System.out.println("Таймаут команды — возвращаемся к ожиданию Люмьер");
                        listeningForCommand = false;
                        commandRecognizer.stop();
                    }
                }
            }

            Thread.sleep(20);
        }
    }
}