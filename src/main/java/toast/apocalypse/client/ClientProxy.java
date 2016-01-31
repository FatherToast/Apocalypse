package toast.apocalypse.client;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderFireball;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import toast.apocalypse.ApocalypseMod;
import toast.apocalypse.CommonProxy;
import toast.apocalypse.entity.EntityBreecher;
import toast.apocalypse.entity.EntityDestroyer;
import toast.apocalypse.entity.EntityDestroyerFireball;
import toast.apocalypse.entity.EntityGhost;
import toast.apocalypse.entity.EntityGrump;
import toast.apocalypse.entity.EntityMonsterFishHook;
import toast.apocalypse.entity.EntitySeeker;
import toast.apocalypse.entity.EntitySeekerFireball;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Handles tasks that should be done differently depending on the side they are called from in a way that is safe
 * for any task to be called from either side.<br>
 * The ClientProxy is only used client-side, while CommonProxy is used exclusively server-side.<br>
 * Mainly used to prevent errors due to references to client-side classes, which don't exist on the server.
 *
 * @see CommonProxy
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    /** The texture used for the bucket helmet overlay. */
    public static final ResourceLocation BUCKET_OVERLAY_TEXTURE = new ResourceLocation(ApocalypseMod.TEXTURE_PATH + "misc/bucketblur.png");

    @Override
    public void registerRenderers() {
        new GuiDifficulty();

        RenderingRegistry.registerEntityRenderingHandler(EntityBreecher.class, new RenderBreecher());
        RenderingRegistry.registerEntityRenderingHandler(EntityGrump.class, new RenderGrump());
        RenderingRegistry.registerEntityRenderingHandler(EntitySeeker.class, new RenderSeeker());
        RenderingRegistry.registerEntityRenderingHandler(EntityGhost.class, new RenderGhost());
        RenderingRegistry.registerEntityRenderingHandler(EntityDestroyer.class, new RenderDestroyer());

        RenderingRegistry.registerEntityRenderingHandler(EntityDestroyerFireball.class, new RenderFireball(2.0F));
        RenderingRegistry.registerEntityRenderingHandler(EntitySeekerFireball.class, new RenderFireball(1.5F));
        RenderingRegistry.registerEntityRenderingHandler(EntityMonsterFishHook.class, new RenderMonsterFishHook());
    }

    @Override
    public int getRenderIndex(String id, int defaultValue) {
        return RenderingRegistry.addNewArmourRendererPrefix(id);
    }

    @Override
    public void renderBucketBlur(int w, int h) {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(ClientProxy.BUCKET_OVERLAY_TEXTURE);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(0.0,   h, -90.0, 0.0, 1.0);
        tessellator.addVertexWithUV(  w,   h, -90.0, 1.0, 1.0);
        tessellator.addVertexWithUV(  w, 0.0, -90.0, 1.0, 0.0);
        tessellator.addVertexWithUV(0.0, 0.0, -90.0, 0.0, 0.0);
        tessellator.draw();
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }
}