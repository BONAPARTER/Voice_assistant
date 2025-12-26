package activation_recognition;

import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import java.io.IOException;

import static java.lang.System.out;

public class ActivationRecognizer {

    private final String modelPath;
    private final String[] KEYWORDS;
    private Model model;
    private Recognizer recognizer;

    private String lastFullResult = "";
    private String lastPartialResult = "";

    public ActivationRecognizer(String modelPath, String language) {
        this.modelPath = modelPath;

        if ("ru".equalsIgnoreCase(language)) {
            this.KEYWORDS = new String[]{
                    "люмьер",
                    "эй люмьер",
                    "люмьер помощник",
                    "люмьер слушай"
            };
        } else if ("en".equalsIgnoreCase(language)) {
            this.KEYWORDS = new String[]{
                    "lumiere",
                    "hey lumiere",
                    "lumiere assistant",
                    "lumiere listen"
            };
        } else {
            this.KEYWORDS = new String[]{
                    "люмьер",
                    "эй люмьер",
                    "люмьер помощник",
                    "люмьер слушай"
            };
        }
    }

    public String buildGrammarString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < KEYWORDS.length; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(KEYWORDS[i].replace("\"", "\\\"")).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    public void start() throws Exception {
        LibVosk.setLogLevel(LogLevel.WARNINGS);

        try {
            model = new Model(modelPath);

            String grammar = buildGrammarString();

            recognizer = new Recognizer(model, 16000.0f, grammar);
            out.println("ActivationRecognizer запущен - ждём активационное слово");
        } catch (IOException e) {
            throw new Exception("Не удалось загрузить модель для wake-word: " + e.getMessage());
        }
    }

    /**
     * Передаём очередной кусок аудио
     *
     * @return true — если Vosk решил, что фраза закончилась (возможно с ключевым словом)
     */
    public boolean processChunk(byte[] pcmData) {
        if (recognizer == null) return false;

        boolean detected = recognizer.acceptWaveForm(pcmData, pcmData.length);

        if (detected) {
            lastFullResult = recognizer.getResult();
            lastPartialResult = "";

            return true;
        } else {
            lastPartialResult = recognizer.getPartialResult();
            return false;
        }
    }

    public String getDetectedKeyword() {
        if (lastFullResult == null || lastFullResult.isEmpty()) {
            return "";
        }

        String text = extractTextFromJson(lastFullResult);
        return text.trim();
    }

    private String extractTextFromJson(String json) {
        int start = json.indexOf("\"text\"");
        if (start == -1) return "";

        start = json.indexOf("\"", start + 6);
        if (start == -1) return "";
        start++;

        int end = json.indexOf("\"", start);
        if (end == -1) return "";

        return json.substring(start, end);
    }

    public String getPartial() {
        return lastPartialResult;
    }

    public String getFullResultJson() {
        return lastFullResult;
    }

    public void stop() {
        if (recognizer != null) {
            recognizer.close();
            recognizer = null;
        }
        if (model != null) {
            model.close();
            model = null;
        }
        out.println("ActivationRecognizer остановлен");
    }

    public void clearLastResult() {
        lastFullResult = "";
        lastPartialResult = "";
    }

    public boolean isRunning() {
        return recognizer != null;
    }
}