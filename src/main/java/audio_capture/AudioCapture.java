package audio_capture;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;

public interface AudioCapture extends AutoCloseable {

    ByteBuffer getCapturedData();

    AudioFormat getAudioFormat();
}
