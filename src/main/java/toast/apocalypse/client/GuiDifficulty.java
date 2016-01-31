package toast.apocalypse.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.opengl.GL11;

import toast.apocalypse.PropHelper;
import toast.apocalypse.Properties;
import toast.apocalypse.WorldDifficultyManager;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiDifficulty {
    /** The color sequence. */
    public static final int[] COLORS = {
        0xffffff, 0x88ffff, 0x88ff88, 0xffff88, 0xffbb88, 0xff8888
    };

    // Rendering properties for quick access.
    public static final long COLOR_CHANGE = (long) (Properties.getDouble(Properties.DISPLAY, "color_change") * PropHelper.DAY_LENGTH);
    public static final int POSITION_X = GuiDifficulty.getPositionX();
    public static final int POSITION_Y = GuiDifficulty.getPositionY();
    public static final int OFFSET_X = Properties.getInt(Properties.DISPLAY, "offset_h") * (GuiDifficulty.POSITION_X == 1 ? -1 : 1);
    public static final int OFFSET_Y = Properties.getInt(Properties.DISPLAY, "offset_v") * (GuiDifficulty.POSITION_Y == 1 ? -1 : 1);

    /** @return The timer's X position code. */
    private static int getPositionX() {
        String pos = Properties.getString(Properties.DISPLAY, "position_h");
        if (pos.equalsIgnoreCase("LEFT"))
            return 0;
        else if (pos.equalsIgnoreCase("RIGHT"))
            return 1;
        else if (pos.equalsIgnoreCase("CENTER"))
            return 2;
        return -1;
    }
    /** @return The timer's Y position code. */
    private static int getPositionY() {
        String pos = Properties.getString(Properties.DISPLAY, "position_v");
        if (pos.equalsIgnoreCase("TOP"))
            return 0;
        else if (pos.equalsIgnoreCase("BOTTOM"))
            return 1;
        else if (pos.equalsIgnoreCase("CENTER"))
            return 2;
        return -1;
    }

    /** The client this gui is registered in. */
    private final Minecraft mc = FMLClientHandler.instance().getClient();

    /** Constructs a GuiDifficulty that automatically registers itself to the game to do everything it needs to
     * render the world difficulty overlay. */
    public GuiDifficulty() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Called by GuiIngameForge.renderGameOverlay().
     * float partialTicks = time since the last game tick.
     * ScaledResolution resolution = the game resolution.
     * int mouseX = the x position of the mouse.
     * int mouseY = the y position of the mouse.
     * RenderGameOverlayEvent.ElementType type = the type of overlay being rendered
     *
     * @param event The event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void afterRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.BOSSHEALTH || GuiDifficulty.POSITION_X < 0 || GuiDifficulty.POSITION_Y < 0)
            return;

        int width = event.resolution.getScaledWidth();
        int height = event.resolution.getScaledHeight();

        // Calculate difficulty level in days with 1 decimal point
        int color = GuiDifficulty.COLORS[0];
        long difficulty = WorldDifficultyManager.getWorldDifficulty();
        int partialDifficulty = (int) (difficulty % 24000L / 2400);
        if (GuiDifficulty.COLOR_CHANGE >= 0L && difficulty >= 0L) {
            if (difficulty >= GuiDifficulty.COLOR_CHANGE) {
                color = GuiDifficulty.COLORS[GuiDifficulty.COLORS.length - 1];
            }
            else {
                color = GuiDifficulty.COLORS[(int) (difficulty / (double) GuiDifficulty.COLOR_CHANGE * GuiDifficulty.COLORS.length)];
            }
        }
        difficulty /= 24000L;

        String difficultyInfo = "Difficulty level: " + difficulty + "." + partialDifficulty;

        // Calculate % of increase in difficulty rate
        double difficultyRate = WorldDifficultyManager.getDifficultyRate();
        if (difficultyRate != 1.0) {
            difficultyInfo = difficultyInfo + " Rate: " + (int)(difficultyRate * 100.0) + "%";
        }

        int x, y;
        switch (GuiDifficulty.POSITION_X) {
            case 0:
                x = 2;
                break;
            case 1:
                x = width - this.mc.fontRenderer.getStringWidth(difficultyInfo) - 2;
                break;
            case 2:
                x = (width >> 1) - (this.mc.fontRenderer.getStringWidth(difficultyInfo) >> 1);
                break;
            default:
                return;
        }
        switch (GuiDifficulty.POSITION_Y) {
            case 0:
                y = 2;
                break;
            case 1:
                y = height - 10;
                break;
            case 2:
                y = (height >> 1) - 4;
                break;
            default:
                return;
        }
        x += GuiDifficulty.OFFSET_X;
        y += GuiDifficulty.OFFSET_Y;

        this.mc.fontRenderer.drawStringWithShadow(difficultyInfo, x, y, color);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
