package toast.apocalypse.client;

import net.minecraft.client.renderer.entity.RenderGhast;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import toast.apocalypse.ApocalypseMod;
import toast.apocalypse.entity.EntityDestroyer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderDestroyer extends RenderGhast {

    public static final ResourceLocation DESTROYER_TEXTURE = new ResourceLocation(ApocalypseMod.TEXTURE_PATH + "entity/destroyer.png");
    public static final ResourceLocation DESTROYER_FIRE_TEXTURE = new ResourceLocation(ApocalypseMod.TEXTURE_PATH + "entity/destroyer_fire.png");

    public RenderDestroyer() {
        super();
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        if (((EntityDestroyer)entity).getFireTexture() == 1)
            return RenderDestroyer.DESTROYER_FIRE_TEXTURE;
        return RenderDestroyer.DESTROYER_TEXTURE;
    }

    @Override
    protected void preRenderCallback(EntityLivingBase entity, float partialTick) {
        GL11.glScalef(4.0F, 4.0F, 4.0F);
    }
}