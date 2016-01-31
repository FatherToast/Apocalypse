package toast.apocalypse.client;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import toast.apocalypse.ApocalypseMod;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderGhost extends RenderLiving {

    public static final ResourceLocation GHOST_TEXTURE = new ResourceLocation(ApocalypseMod.TEXTURE_PATH + "entity/ghost.png");
    public static final ResourceLocation GHOST_TEXTURE_SOLID = new ResourceLocation(ApocalypseMod.TEXTURE_PATH + "entity/ghost_base.png");

    public RenderGhost() {
        super(new ModelGhost(), 0.0F);
        this.setRenderPassModel(this.mainModel);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return RenderGhost.GHOST_TEXTURE_SOLID;
    }

    @Override
    protected int shouldRenderPass(EntityLivingBase entity, int renderPass, float partialTick) {
        if (renderPass == 0) {
            this.bindTexture(RenderGhost.GHOST_TEXTURE);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
            GL11.glDepthMask(!entity.isInvisible());
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            return 1;
        }
        return -1;
    }
}