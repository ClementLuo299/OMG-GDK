package launcher.features.module_handling.extract_metadata;

import gdk.api.GameModule;
import gdk.api.GameMetadata;
import gdk.internal.Logging;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for extracting and processing extract_metadata from game modules.
 *
 * 
 * @author Clement Luo
 * @date January 2, 2026
 * @edited January 4, 2026
 * @since Beta 1.0
 */
public final class ModuleMetadataExtractor {
    
    private ModuleMetadataExtractor() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    // ==================== PUBLIC METHODS - METADATA EXTRACTION ====================
    
    /**
     * Retrieves metadata from game modules.
     * 
     * @param gameModules List of game modules
     * @return List of GameMetadata objects from the modules
     */
    public static List<GameMetadata> getMetadataFromModules(List<GameModule> gameModules) {
        List<GameMetadata> metadataList = new ArrayList<>();
        
        for (GameModule gameModule : gameModules) {
            if (gameModule == null) {
                continue;
            }
            
            try {
                GameMetadata metadata = gameModule.getMetadata();
                if (metadata != null) {
                    metadataList.add(metadata);
                }
            } catch (Exception e) {
                Logging.error("Error getting metadata from game module: " + e.getMessage(), e);
            }
        }
        
        return metadataList;
    }
}

