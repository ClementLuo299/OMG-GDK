package launcher.features.file_handling.access;

import java.io.File;

/**
 * Checks directory listing capability.
 * 
 * <p>This class provides methods for testing if a directory can be listed,
 * measuring listing performance, and retrieving directory contents.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @since Beta 1.0
 */
public final class DirectoryLister {
    
    private DirectoryLister() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Result of a directory listing operation.
     */
    public static class ListingResult {
        private final boolean success;
        private final File[] contents;
        private final long listTimeMs;
        
        private ListingResult(boolean success, File[] contents, long listTimeMs) {
            this.success = success;
            this.contents = contents;
            this.listTimeMs = listTimeMs;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public File[] getContents() {
            return contents;
        }
        
        public long getListTimeMs() {
            return listTimeMs;
        }
    }
    
    /**
     * Attempts to list the contents of a directory.
     * 
     * <p>This method measures the time taken to list the directory contents
     * and returns a result object containing the listing status, contents, and timing.
     * 
     * @param directory The directory to list
     * @return ListingResult containing success status, contents array, and timing
     */
    public static ListingResult listContents(File directory) {
        long startTime = System.currentTimeMillis();
        File[] contents = directory.listFiles();
        long listTime = System.currentTimeMillis() - startTime;
        
        boolean success = contents != null;
        return new ListingResult(success, contents, listTime);
    }
    
    /**
     * Checks if a directory can be listed.
     * 
     * @param directory The directory to check
     * @return true if the directory can be listed (listFiles() returns non-null), false otherwise
     */
    public static boolean canList(File directory) {
        File[] contents = directory.listFiles();
        return contents != null;
    }
}

