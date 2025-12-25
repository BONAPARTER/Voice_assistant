package audio_capture;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import static java.lang.System.out;

public class MicrophoneCapture implements AudioCapture {
    private static final AudioFormat FORMAT = new AudioFormat(
            16000,
            16,
            1,
            true,
            false
    );

    private TargetDataLine microphone;
    private boolean running;

    // Небольшой буфер, куда будем складывать свежие данные
    private ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024); // ~ 32–33 секунды звука

    @Override
    public void startCapture() throws Exception {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, FORMAT);
        if (!AudioSystem.isLineSupported(info))
            throw new Exception("Твой микрофон не поддерживает нужный формат звука");

        microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(FORMAT);
        microphone.start();

        running = true;


        new Thread(() -> {
            byte[] tempBuffer = new byte[1024];
            while (running) {
                int count = microphone.read(tempBuffer, 0, tempBuffer.length);
                if (count > 0) {
                    try {
                        buffer.put(tempBuffer, 0, count);
                    } catch (BufferOverflowException e) {
                        System.err.println("Буфер переполнен! Пропускаем данные...");
                        buffer.compact();
                    }
                }
            }
        }).start();

        out.println("Микрофон запущен. Говорите!");
    }

    @Override
    public void stopCapture() {
        running = false;
        if (microphone != null) {
            microphone.stop();
            microphone.close();
            out.println("Микрофон остановлен.");
        }
    }

    @Override
    public ByteBuffer getCapturedData() {
        if(buffer.position() > 0) {
            buffer.flip();
            ByteBuffer result = buffer.duplicate();
            buffer.clear();
            return result;
        }
        return null;
    }

    @Override
    public AudioFormat getAudioFormat() {
        return FORMAT;
    }
}
