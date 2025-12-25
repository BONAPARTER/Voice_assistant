package speech_recognition;

import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import java.io.IOException;

import static java.lang.System.out;

public class VoskRecognizer implements SpeechRecognizer {

    private final String modelPath;
    private Model model;
    private Recognizer recognizer;
    private String lastFullResult = "";
    private String lastPartialResult = "";

    public VoskRecognizer(String modelPath) {
        this.modelPath = modelPath;
    }

    @Override
    public void start() throws Exception {
        LibVosk.setLogLevel(LogLevel.WARNINGS);

        try {
            model = new Model(modelPath);
            recognizer = new Recognizer(model, 16000.0f);
            out.println("Vosk инициализирован");
        } catch (IOException e) {
            throw new Exception("Не удалось загрузить модель Vosk: " + e.getMessage());
        }
    }

    @Override
    public boolean acceptAudioChunk(byte[] pcmData) {
        if (recognizer == null)
            return false;

        boolean phraseDetected = recognizer.acceptWaveForm(pcmData, pcmData.length);

        if (phraseDetected) {
            lastFullResult = recognizer.getResult();
            lastPartialResult = "";
            return true;
        } else {
            lastPartialResult = recognizer.getPartialResult();
            return false;
        }
    }

    @Override
    public String getResult() {
        return lastFullResult;
    }

    @Override
    public String getPartialResult() {
        return lastPartialResult;
    }

    @Override
    public void stop() {
        if (recognizer != null) {
            recognizer.close();
            recognizer = null;
        }
        if (model != null) {
            model.close();
            model = null;
        }
        out.println("Vosk остановлен");
    }

    public String getCleanTextResult() {
        if (lastFullResult == null || lastFullResult.isEmpty())
            return "";

        int start = lastFullResult.indexOf("\"text\" : \"") + 10;
        int end = lastFullResult.lastIndexOf("\"");
        if (start >= 10 && end > start)
            return lastFullResult.substring(start, end).trim();

        return lastFullResult.trim();
    }
}
