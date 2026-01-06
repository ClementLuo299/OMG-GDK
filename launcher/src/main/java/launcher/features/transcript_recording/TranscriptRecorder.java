package launcher.features.game_messaging.recording;

import java.nio.file.Path;
import java.util.Map;

/**
 * Compatibility wrapper for TranscriptRecorder.
 * Delegates to specialized classes for session management, message recording, and saving.
 * 
 * @author Clement Luo
 * @date August 9, 2025
 * @edited January 5, 2026
 * @since 1.0
 */
public final class TranscriptRecorder {
    
    private TranscriptRecorder() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    // ==================== SESSION MANAGEMENT ====================
    
    public static void startSession() {
        TranscriptSessionManager.startSession();
    }
    
    public static void startSession(String gameName, String gameVersion) {
        TranscriptSessionManager.startSession(gameName, gameVersion);
    }
    
    public static void endSessionIfEndDetected(Map<String, Object> message) {
        TranscriptMessageRecorder.checkAndHandleEndMessage(message);
    }
    
    // ==================== MESSAGE RECORDING ====================
    
    public static void recordToGame(Map<String, Object> message) {
        TranscriptMessageRecorder.recordToGame(message);
    }
    
    public static void recordFromGame(Map<String, Object> message) {
        TranscriptMessageRecorder.recordFromGame(message);
    }
    
    // ==================== TRANSCRIPT SAVING ====================
    
    public static Path saveTranscript(Path targetFile) {
        return TranscriptSaver.saveTranscript(targetFile);
    }
    
    public static Path saveTranscriptAsText(Path targetFile) {
        return TranscriptSaver.saveTranscriptAsText(targetFile);
    }
    
    public static Path[] saveTranscriptBothFormats(String baseFileName) {
        return TranscriptSaver.saveTranscriptBothFormats(baseFileName);
    }
    
    public static Path[] saveCurrentTranscript() {
        return TranscriptSaver.saveCurrentTranscript();
    }
}
