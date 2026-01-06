package launcher.features.transcript_recording;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Shared state for transcript recording.
 * 
 * @author Clement Luo
 * @date January 5, 2026
 * @edited January 5, 2026
 * @since Beta 1.0
 */
public final class Transcript {
    
    /** Flag indicating whether a transcript session is currently active. */
    public static volatile boolean inSession = false;
    
    /** Thread-safe list of transcript entries. */
    public static final List<Map<String, Object>> entries = Collections.synchronizedList(new ArrayList<>());
    
    private Transcript() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}

