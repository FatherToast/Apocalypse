package toast.apocalypse.client;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelGhost extends ModelBase {

    ModelRenderer head;
    ModelRenderer body;
    ModelRenderer armRight;
    ModelRenderer armLeft;

    public ModelGhost() {
        this.head = new ModelRenderer(this, 0, 0).addBox(-2.0F, -10.0F, -2.0F, 4, 10, 4);
        this.body = new ModelRenderer(this, 16, 10).addBox(-4.0F, 0.0F, -2.0F, 8, 18, 4);
        this.armRight = new ModelRenderer(this, 0, 16).addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4);
        this.armRight.setRotationPoint(-5.0F, 2.0F, 0.0F);
        this.armLeft = new ModelRenderer(this, 0, 16).addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4);
        this.armLeft.setRotationPoint(5.0F, 2.0F, 0.0F);
        this.armLeft.mirror = true;
    }

    @Override
    public void render(Entity entity, float time, float moveSpeed, float rotationFloat, float rotationYaw, float rotationPitch, float scale) {
        this.head.rotateAngleY = rotationYaw * (float) Math.PI / 180.0F;
        this.head.rotateAngleX = rotationPitch * (float) Math.PI / 180.0F;
        this.armLeft.rotateAngleX = -(float) Math.PI / 2.1F;
        this.armRight.rotateAngleX = this.armLeft.rotateAngleX;
        this.head.render(scale);
        this.body.render(scale);
        this.armRight.render(scale);
        this.armLeft.render(scale);
    }
}