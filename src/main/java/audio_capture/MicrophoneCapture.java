package audio_capture;

import custom_exceptions.MicrophoneNotSupported;

import javax.sound.sampled.*;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import static java.lang.System.err;
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

    private ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024); // ~ 32–33 секунды звука

    public MicrophoneCapture() throws MicrophoneNotSupported, RuntimeException{ // TODO: создать класс для обработки
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, FORMAT);
        if (!AudioSystem.isLineSupported(info)) {
            throw new MicrophoneNotSupported("Микрофон не поддерживает нужный формат звука");
        }

        try {
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(FORMAT);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }

        microphone.start();
        running = true;

        new Thread(() -> {
            byte[] tempBuffer = new byte[1024];
            while (running) {
                int count = microphone.read(tempBuffer, 0, tempBuffer.length);

                if (count > 0) {
                    try {
                        if (buffer.remaining() < count) {
                            err.println("Буфер переполнен! Пропускаем данные...");
                            buffer.clear();
                        } else {
                            buffer.put(tempBuffer, 0, count);
                        }
                    } catch (BufferOverflowException e) {
                        err.println("Буфер переполнен! Перезапускаем буфер...");
                        buffer.clear();
                    }
                }
            }
        }).start();

        out.println("Микрофон запущен. Говорите!");
    }

    @Override
    public void close() {
        running = false;
        if (microphone != null) {
            microphone.stop();
            microphone.close();
            out.println("Микрофон остановлен.");
        }
    }

    @Override
    public ByteBuffer getCapturedData() {
        if (buffer.position() > 0) {
            buffer.flip();

            ByteBuffer result = ByteBuffer.allocate(buffer.remaining());
            result.put(buffer);
            result.flip();

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