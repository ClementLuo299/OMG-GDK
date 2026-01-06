package launcher.features.transcript_recording.transcript_saving.save_formats.helpers;

import java.time.Instant;

/**
 * Formats timestamps for transcript display.
 * 
 * @author Clement Luo
 * @date January 5, 2026
 * @edited January 5, 2026
 * @since Beta 1.0
 */
public final class FormatTimestamp {
    
    private FormatTimestamp() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Formats an Instant object into a human-readable string.
     * 
     * @param instant The instant to format
     * @return A formatted timestamp string
     */
    public static String format(Instant instant) {
        String timestamp = instant.toString();
        timestamp = timestamp.replace("T", " ").replace("Z", "");
        if (timestamp.contains(".")) {
            String[] parts = timestamp.split("\\.");
            if (parts.length == 2 && parts[1].length() > 3) {
                timestamp = parts[0] + "." + parts[1].substring(0, 3);
            }
        }
        return timestamp;
    }
}

