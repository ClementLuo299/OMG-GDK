package gdk;

import java.util.Map;
import java.util.function.Consumer;

public final class MessagingBridge {
    private static volatile Consumer<Map<String, Object>> messageConsumer;

    private MessagingBridge() {}

    public static void setConsumer(Consumer<Map<String, Object>> consumer) {
        messageConsumer = consumer;
    }

    public static void publish(Map<String, Object> message) {
        Consumer<Map<String, Object>> c = messageConsumer;
        if (c != null) {
            try {
                c.accept(message);
            } catch (Exception ignored) {
            }
        }
    }
} 