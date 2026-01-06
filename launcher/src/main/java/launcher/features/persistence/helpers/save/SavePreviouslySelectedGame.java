package launcher.features.persistence.helpers.save;

import launcher.features.file_handling.directory_existence.ParentDirectoryExistenceCheck;
import launcher.features.file_handling.file_paths.GetOtherPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Manages saving selected game name to file.
 * 
 * @author Clement Luo
 * @date January 5, 2026
 * @edited January 5, 2026
 * @since 1.0
 */
public final class SavePreviouslySelectedGame {
    
    /**
     * Saves the selected game module's name to file.
     * 
     * <p>This method silently ignores null or "None" game names.
     * Failures are silently ignored as this is not a critical operation.
     * 
     * @param gameName The name of the selected game module
     */
    public static void save(String gameName) {
        try {
            if (gameName == null || gameName.equals("None")) return;
            Path gameFile = Paths.get(GetOtherPaths.SELECTED_GAME_FILE);
            ParentDirectoryExistenceCheck.exists(gameFile);
            Files.writeString(gameFile, gameName);
        } catch (Exception ignored) {
            // Silently fail - not critical
        }
    }
}

