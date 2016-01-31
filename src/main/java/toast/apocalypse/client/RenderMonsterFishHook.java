package toast.apocalypse.client;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import toast.apocalypse.entity.EntityMonsterFishHook;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMonsterFishHook extends Render {

    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/particle/particles.png");

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return RenderMonsterFishHook.TEXTURE;
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
    	EntityMonsterFishHook fishHook = (EntityMonsterFishHook) entity;

    	// Render hook and bobber
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y, (float)z);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        this.bindEntityTexture(fishHook);
        Tessellator tessellator = Tessellator.instance;
        float minU = (1 * 8 + 0) / 128.0F;
        float maxU = (1 * 8 + 8) / 128.0F;
        float minV = (2 * 8 + 0) / 128.0F;
        float maxV = (2 * 8 + 8) / 128.0F;
        GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        tessellator.addVertexWithUV(-0.5, -0.5, 0.0, minU, maxV);
        tessellator.addVertexWithUV( 0.5, -0.5, 0.0, maxU, maxV);
        tessellator.addVertexWithUV( 0.5,  0.5, 0.0, maxU, minV);
        tessellator.addVertexWithUV(-0.5,  0.5, 0.0, minU, minV);
        tessellator.draw();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();

        // Render fishing line
        if (fishHook.angler != null) {
            float swing = MathHelper.sin(MathHelper.sqrt_float(fishHook.angler.getSwingProgress(partialTicks)) * (float)Math.PI);
            Vec3 vec = Vec3.createVectorHelper(-0.5, 0.03, 0.8);
            vec.rotateAroundX(-(fishHook.angler.prevRotationPitch + (fishHook.angler.rotationPitch - fishHook.angler.prevRotationPitch) * partialTicks) * (float)Math.PI / 180.0F);
            vec.rotateAroundY(-(fishHook.angler.prevRotationYaw + (fishHook.angler.rotationYaw - fishHook.angler.prevRotationYaw) * partialTicks) * (float)Math.PI / 180.0F);
            vec.rotateAroundY( swing * 0.5F);
            vec.rotateAroundX(-swing * 0.7F);

            double d5 = fishHook.angler.prevPosX + (fishHook.angler.posX - fishHook.angler.prevPosX) * partialTicks + vec.xCoord;
            double d6 = fishHook.angler.prevPosY + (fishHook.angler.posY - fishHook.angler.prevPosY) * partialTicks + vec.yCoord;
            double d8 = fishHook.angler.prevPosZ + (fishHook.angler.posZ - fishHook.angler.prevPosZ) * partialTicks + vec.zCoord;
            double d9 = fishHook.prevPosX + (fishHook.posX - fishHook.prevPosX) * partialTicks;
            double d10 = fishHook.prevPosY + (fishHook.posY - fishHook.prevPosY) * partialTicks + 0.25;
            double d11 = fishHook.prevPosZ + (fishHook.posZ - fishHook.prevPosZ) * partialTicks;
            double d12 = (float)(d5 - d9);
            double d13 = (float)(d6 - d10) + 1.7F;
            double d14 = (float)(d8 - d11);

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            tessellator.startDrawing(3);
            tessellator.setColorOpaque_I(0);
            byte b = 16;
            for (int i = 0; i <= b; i++) {
                float f12 = (float)i / (float)b;
                tessellator.addVertex(x + d12 * f12, y + d13 * (f12 * f12 + f12) * 0.5 + 0.25, z + d14 * f12);
            }
            tessellator.draw();
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
    }
}