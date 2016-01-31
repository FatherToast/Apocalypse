package toast.apocalypse.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.WorldServer;

/**
 * This represents a basic event that can occur using Apocalypse's event system and will persist through
 * save data until ended or interrupted by a higher-priority event (such as a {@link #fullMoon}).
 */
public abstract class EventBase {

    /** Array of all event objects. */
    public static final EventBase[] EVENTS = new EventBase[64];

    // All event objects.
    /** Full moon event. Prevents the player from sleeping and spawns full moon mobs.<br>
     * Can occur every eight days in game. */
    public static final EventBase fullMoon = new EventFullMoon(0);

    /** The index of this event. */
    public final byte EVENT_ID;

    /**
     * Constructs an EventBase and registers it with a savegame id.
     * @param id (0-63) The savegame id for the event. Must be unique.
     */
    public EventBase(int id) {
        this.EVENT_ID = (byte) id;
        if (EventBase.EVENTS[id] != null)
            throw new IllegalArgumentException("Duplicate event id! " + id);
        EventBase.EVENTS[id] = this;
    }

    /** Returns the unlocalized message to send when the event starts. */
    public abstract String getStartMessage();

    /** Returns true if the passed event can interrupt this one. All events should be interruptable by {@link #fullMoon}. */
    public boolean canBeInterrupted(EventBase event) {
        return event == EventBase.fullMoon;
    }

    /** Called when the event starts. Variables should all be set to default values here. */
    public abstract void onStart();

    /** Called every 5 ticks to update the event. */
    public void update() {
        // To be overridden
    }

    /** Called every 5 ticks for each world to update the event.
     * @param world The world to update for this event. */
    public void update(WorldServer world) {
        // To be overridden
    }

    /** Called every 5 ticks for each player to update the event.
     * @param player The player to update for this event. */
    public void update(EntityPlayer player) {
        // To be overridden
    }

    /** Called when the event ends. */
    public void onEnd() {
        // To be overridden
    }

    /**
     * Saves this event.
     * @param out The output stream to save to.
     * @throws IOException If an exception occurs while trying to write to the stream.
     */
    public void save(DataOutputStream out) throws IOException {
        // To be overridden
    }

    /**
     * Loads this event.
     * @param in The input stream to load from.
     * @throws IOException If an exception occurs while trying to read from the stream.
     */
    public void load(DataInputStream in) throws IOException {
        // To be overridden
    }
}
