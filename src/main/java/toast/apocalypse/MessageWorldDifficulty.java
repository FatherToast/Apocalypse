package toast.apocalypse;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * A message sent by the server to clients, alerting them to changes in the difficulty level, difficulty rate, or both.
 */
public class MessageWorldDifficulty implements IMessage {

	/** Contains a constant for each world difficulty message type possible. */
	public static class Type {
	    public static final byte ALL = -1;
	    public static final byte DIFFICULTY = 0;
	    public static final byte RATE = 1;
	}

    /** The type of this world difficulty message.
     * @see Type */
    public byte type;
    /** The world difficulty level (in ticks). */
    public long difficulty;
    /** The difficulty increase rate. */
    public double difficultyRate;

    /** Creates an empty message, which will then usually have data loaded to it by {@link #fromBytes(ByteBuf)}. */
    public MessageWorldDifficulty() {}

    /**
     * Creates a message containing both the difficulty level and rate.
     * @param difficulty The difficulty to send.
     * @param difficultyRate The difficulty rate to send.
     */
    public MessageWorldDifficulty(long difficulty, double difficultyRate) {
        this.type = MessageWorldDifficulty.Type.ALL;
        this.difficulty = difficulty;
        this.difficultyRate = difficultyRate;
    }
    /**
     * Creates a message containing only the difficulty level.
     * @param difficulty The difficulty to send.
     */
    public MessageWorldDifficulty(long difficulty) {
        this.type = MessageWorldDifficulty.Type.DIFFICULTY;
        this.difficulty = difficulty;
    }
    /**
     * Creates a message containing only the difficulty rate.
     * @param difficultyRate The difficulty rate to send.
     */
    public MessageWorldDifficulty(double difficultyRate) {
        this.type = MessageWorldDifficulty.Type.RATE;
        this.difficultyRate = difficultyRate;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.type);
        if (this.type == MessageWorldDifficulty.Type.DIFFICULTY || this.type == MessageWorldDifficulty.Type.ALL) {
            buf.writeLong(this.difficulty);
        }
        if (this.type == MessageWorldDifficulty.Type.RATE || this.type == MessageWorldDifficulty.Type.ALL) {
            buf.writeDouble(this.difficultyRate);
        }
    }
    @Override
    public void fromBytes(ByteBuf buf) {
        this.type = buf.readByte();
        if (this.type == MessageWorldDifficulty.Type.DIFFICULTY || this.type == MessageWorldDifficulty.Type.ALL) {
            this.difficulty = buf.readLong();
        }
        if (this.type == MessageWorldDifficulty.Type.RATE || this.type == MessageWorldDifficulty.Type.ALL) {
            this.difficultyRate = buf.readDouble();
        }
    }

    /** Handles the enclosing message type. */
    public static class Handler implements IMessageHandler<MessageWorldDifficulty, IMessage> {

        @Override
        public IMessage onMessage(MessageWorldDifficulty message, MessageContext ctx) {
            if (message.type == MessageWorldDifficulty.Type.DIFFICULTY || message.type == MessageWorldDifficulty.Type.ALL) {
                WorldDifficultyManager.setWorldDifficulty(message.difficulty);
            }
            if (message.type == MessageWorldDifficulty.Type.RATE || message.type == MessageWorldDifficulty.Type.ALL) {
                WorldDifficultyManager.setDifficultyRate(message.difficultyRate);
            }
            return null;
        }

    }
}
