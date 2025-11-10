package gdk.api;

/**
 * Platform-agnostic representation of a game's root view or context.
 * This allows the GDK to remain independent from any specific UI toolkit.
 */
public interface GameView {
    /**
     * @return The underlying platform object (e.g. a Scene, Canvas, Component, or Node)
     */
    Object getNativeView();
}

