import java.nio.ByteBuffer;
import audio_capture.AudioCapture;
import audio_capture.MicrophoneCapture;
import speech_recognition.SpeechRecognizer;
import speech_recognition.VoskRecognizer;

public class Main {

    public static void main(String[] args) throws Exception {
        AudioCapture capture = new MicrophoneCapture();
        SpeechRecognizer recognizer = new VoskRecognizer("src/main/resources/models/vosk-model-small-ru-0.22");

        capture.startCapture();
        recognizer.start();

        System.out.println("Говори... (Ctrl+C для выхода)");

        try {
            while (true) {
                ByteBuffer chunk = capture.getCapturedData();
                if (chunk != null && chunk.hasRemaining()) {
                    byte[] data = new byte[chunk.remaining()];
                    chunk.get(data);

                    boolean phraseReady = recognizer.acceptAudioChunk(data);

                    if (phraseReady) {
                        String text = ((VoskRecognizer) recognizer).getCleanTextResult();
                        if (!text.isEmpty()) {
                            System.out.println("Распознано: " + text);
                        }
                    }

                    // Можно показывать частичное для отладки
                    // String partial = recognizer.getPartialResult();
                    // if (!partial.isEmpty()) System.out.println("... " + partial);
                }

                Thread.sleep(20); // ~50 fps — хороший баланс
            }
        } finally {
            recognizer.stop();
            capture.stopCapture();
        }
    }
}
