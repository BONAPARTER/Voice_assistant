import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import activation_recognition.ActivationRecognizer;
import audio_capture.AudioCapture;
import audio_capture.MicrophoneCapture;
import speech_recognition.SpeechRecognizer;
import speech_recognition.VoskRecognizer;
import nlp_parsing.Command;
import nlp_parsing.CommandHandler;
import nlp_parsing.CommandParserImpl;
import ui.LanguageSelectorUI;

import static java.lang.System.out;

public class Main {

    private enum State {
        WAITING_FOR_WAKE_WORD,
        LISTENING_FOR_COMMAND,
        PROCESSING
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            LanguageSelectorUI.show((modelPath, languageCode) -> {
                try {
                    startVoiceAssistant(modelPath, languageCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else {
            startVoiceAssistant(args[0], args.length > 1 ? args[1] : "ru");
        }
    }

    private static void startVoiceAssistant(String modelPath, String language) throws Exception {
        out.println("Запуск голосового ассистента...");
        out.println("Модель: " + modelPath);
        out.println("Язык:   " + language);

        AtomicReference<State> state = new AtomicReference<>(State.WAITING_FOR_WAKE_WORD);
        AtomicBoolean running = new AtomicBoolean(true);
        final Object lock = new Object();

        CommandParserImpl parser = new CommandParserImpl(language);
        CommandHandler handler = new CommandHandler();

        final long COMMAND_TIMEOUT_MS = 8500;

        try (AudioCapture capture = new MicrophoneCapture()) {
            // Поток обнаружения ключевого слова
            Thread wakeThread = new Thread(() -> {
                while (running.get()) {
                    synchronized (lock) {
                        while (state.get() != State.WAITING_FOR_WAKE_WORD && running.get()) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }
                    }

                    if (!running.get()) break;

                    try (ActivationRecognizer wakeRecognizer = new ActivationRecognizer(modelPath, language)) {
                        while (state.get() == State.WAITING_FOR_WAKE_WORD && running.get()) {
                            ByteBuffer chunk = capture.getCapturedData();

                            if (chunk == null || !chunk.hasRemaining()) {
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                                continue;
                            }

                            byte[] data = new byte[chunk.remaining()];
                            chunk.get(data);

                            if (wakeRecognizer.processChunk(data)) {
                                String keyword = wakeRecognizer.getDetectedKeyword();
                                if (keyword != null && !keyword.trim().isEmpty()) {
                                    out.println(">>> ЛЮМЬЕР УСЛЫШАЛ! (" + keyword + ") <<<");
                                    synchronized (lock) {
                                        state.set(State.LISTENING_FOR_COMMAND);
                                        lock.notifyAll();
                                    }
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, "Wake-Word-Thread");

            // Поток распознавания команды
            Thread commandThread = new Thread(() -> {
                while (running.get()) {
                    synchronized (lock) {
                        while (state.get() != State.LISTENING_FOR_COMMAND && running.get()) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }
                    }

                    if (!running.get()) break;

                    try (VoskRecognizer recognizer = new VoskRecognizer(modelPath, language)) {
                        long commandStartTime = System.currentTimeMillis();
                        boolean done = false;

                        while (!done && running.get() && state.get() == State.LISTENING_FOR_COMMAND) {
                            if (System.currentTimeMillis() - commandStartTime > COMMAND_TIMEOUT_MS) {
                                out.println("Таймаут команды");
                                done = true;
                                break;
                            }

                            ByteBuffer chunk = capture.getCapturedData();

                            if (chunk == null || !chunk.hasRemaining()) {
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                                continue;
                            }

                            byte[] data = new byte[chunk.remaining()];
                            chunk.get(data);

                            boolean isFinal = recognizer.acceptAudioChunk(data);
                            if (isFinal) {
                                String text = recognizer.getCleanTextResult();
                                if (!text.trim().isEmpty()) {
                                    out.println("Распознано: " + text);
                                    Command command = parser.parse(text);
                                    handler.handle(command);
                                } else {
                                    out.println("(пустой результат)");
                                }
                                done = true;
                            }
                        }

                        synchronized (lock) {
                            state.set(State.WAITING_FOR_WAKE_WORD);
                            lock.notifyAll();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, "Command-Recognition-Thread");

            wakeThread.setDaemon(true);
            commandThread.setDaemon(true);

            wakeThread.start();
            commandThread.start();

            out.println("Ассистент запущен. Для выхода нажмите Ctrl+C\n");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                running.set(false);
                synchronized (lock) {
                    lock.notifyAll();
                }
                out.println("Завершение работы ассистента...");
            }));

            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                running.set(false);
                synchronized (lock) {
                    lock.notifyAll();
                }
                out.println("Завершение работы ассистента...");
            }
        }
    }
}