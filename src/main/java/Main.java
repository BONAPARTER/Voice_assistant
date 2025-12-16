import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import audio_capture.AudioCapture;
import audio_capture.MicrophoneCapture;
import org.vosk.LogLevel;
import org.vosk.Recognizer;
import org.vosk.LibVosk;
import org.vosk.Model;

public class Main {

    public static void main(String[] argv) throws IOException, UnsupportedAudioFileException, Exception {
//        LibVosk.setLogLevel(LogLevel.INFO);
//
//        try (Model model = new Model("src/main/resources/models/vosk-model-small-ru-0.22");
//             AudioInputStream ais = AudioSystem.getAudioInputStream(
//                     new BufferedInputStream(new FileInputStream("src/main/resources/test.wav")));
//        ) {
//            Recognizer recognizer = new Recognizer(model, ais.getFormat().getSampleRate());
//
//            byte[] buffer = new byte[4096];
//            int nbytes;
//            while ((nbytes = ais.read(buffer)) >= 0) {
//                recognizer.acceptWaveForm(buffer, nbytes);
//            }
//
//            String finalJson = recognizer.getFinalResult();
//            System.out.println(finalJson);
//
//            recognizer.close();
//        }


        AudioCapture capture = new MicrophoneCapture();

        capture.startCapture();

        // Просто проверяем, приходят ли данные
        for (int i = 0; i < 50; i++) {
            ByteBuffer data = capture.getCapturedData();
            if (data != null) {
                System.out.println("Получил " + data.remaining() + " байт звука");
            } else System.out.println("  Пусто, ничего нет");
            Thread.sleep(1000);  // Ждём 0.1 секунды
        }

        capture.stopCapture();
    }
}
