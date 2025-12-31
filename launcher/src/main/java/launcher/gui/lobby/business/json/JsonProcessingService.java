package launcher.gui.lobby.business.json;

import launcher.gui.lobby.business.GDKViewModel;
import launcher.gui.lobby.business.JsonFormatter;

import java.util.Map;

/**
 * Business logic service for JSON processing operations.
 * 
 * <p>This service handles:
 * <ul>
 *   <li>Parsing JSON configuration data</li>
 *   <li>Formatting JSON responses</li>
 * </ul>
 * 
 * <p>This service does NOT handle UI updates.
 * Those responsibilities belong to UI logic classes.
 * 
 * @author Clement Luo
 * @date December 30, 2025
 * @since Beta 1.0
 */
public class JsonProcessingService {
    
    // ==================== DEPENDENCIES ====================
    
    /** ViewModel for business logic operations. */
    private final GDKViewModel viewModel;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Creates a new JsonProcessingService.
     * 
     * @param viewModel The ViewModel for business logic (may be null initially)
     */
    public JsonProcessingService(GDKViewModel viewModel) {
        this.viewModel = viewModel;
    }
    
    // ==================== PUBLIC METHODS ====================
    
    /**
     * Parses JSON configuration text into a map.
     * 
     * @param jsonText The JSON text to parse
     * @return Parsed configuration map, or null if parsing fails or ViewModel is unavailable
     */
    public Map<String, Object> parseJsonConfiguration(String jsonText) {
        if (viewModel == null || jsonText == null || jsonText.trim().isEmpty()) {
            return null;
        }
        return viewModel.parseJsonConfiguration(jsonText);
    }
    
    /**
     * Formats a JSON response for display.
     * 
     * @param response The response map to format
     * @return Formatted JSON string
     */
    public String formatJsonResponse(Map<String, Object> response) {
        return JsonFormatter.formatJsonResponse(response);
    }
}

