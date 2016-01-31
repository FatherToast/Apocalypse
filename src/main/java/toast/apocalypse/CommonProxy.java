package toast.apocalypse;

/**
 * Handles tasks that should be done differently depending on the side they are called from in a way that is safe
 * for any task to be called from either side.<br>
 * The CommonProxy class itself is exclusively used server-side, while ClientProxy is only used client-side.<br>
 * Mainly used to prevent errors due to references to client-side classes, which don't exist on the server.
 *
 * @see toast.apocalypse.client.ClientProxy ClientProxy
 */
public class CommonProxy {

    /** Registers renderers if this is the client side. */
    public void registerRenderers() {
        // Client-side method
    }

    /**
     * Returns a new render index to use and registers it with the game.
     * @param id The id of the texture. The filenames become id_1 (non-leg armor) and id_2 (leg armor).
     * @param defaultValue The render index to use if possible.
     * @return The render index assigned. This will be defaultValue if this is not the client side or if that index was available.
     */
    public int getRenderIndex(String id, int defaultValue) {
        return defaultValue;
    }

    /**
     * Renders the bucket helmet overlay if this is the client side.
     * @param w Width of the screen.
     * @param h Height of the screen.
     */
    public void renderBucketBlur(int w, int h) {
        // Client-side method
    }
}