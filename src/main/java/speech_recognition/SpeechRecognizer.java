package speech_recognition;

public interface SpeechRecognizer {
    void start() throws Exception;

    /**
     * Передать очередной кусок аудио
     * @return true если была распознана завершённая фраза в этом куске
     */
    boolean acceptAudioChunk(byte[] pcmData);

    String getResult();

    String getPartialResult();

    void stop();
}
