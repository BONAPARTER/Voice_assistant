package audio_capture;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;

public interface AudioCapture {
    void startCapture() throws Exception;

    void stopCapture();

    ByteBuffer getCapturedData();

    AudioFormat getAudioFormat();
}
