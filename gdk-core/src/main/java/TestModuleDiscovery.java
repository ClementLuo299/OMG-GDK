import com.utils.ModuleLoader;
import com.game.GameModule;
import java.util.List;

public class TestModuleDiscovery {
    public static void main(String[] args) {
        System.out.println("🔍 Testing module discovery...");
        
        List<GameModule> modules = ModuleLoader.discoverModules("modules");
        
        System.out.println("📦 Found " + modules.size() + " modules:");
        for (GameModule module : modules) {
            System.out.println("  ✅ " + module.getGameName() + " (" + module.getGameId() + ")");
        }
    }
} 