package toast.apocalypse.client;

import net.minecraft.client.renderer.entity.RenderGhast;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import toast.apocalypse.ApocalypseMod;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderGrump extends RenderGhast {

    public static final ResourceLocation GRUMP_TEXTURE = new ResourceLocation(ApocalypseMod.TEXTURE_PATH + "entity/grump.png");

    public RenderGrump() {
        super();
        this.shadowSize = 0.25F;
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return RenderGrump.GRUMP_TEXTURE;
    }

    @Override
    protected void preRenderCallback(EntityLivingBase entity, float partialTick) {
        // Overriding the default ghast render scaling
    }
}