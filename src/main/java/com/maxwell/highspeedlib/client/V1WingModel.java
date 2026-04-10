package com.maxwell.highspeedlib.client;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.common.logic.SlideManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

@SuppressWarnings("removal")
public class V1WingModel<T extends LivingEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(HighSpeedLib.MODID, "v1wingmodel"), "main");
    private final ModelPart root;
    private final ModelPart wing_base;
    private final ModelPart wing_right;
    private final ModelPart part_r4;
    private final ModelPart part_r3;
    private final ModelPart part_r2;
    private final ModelPart part_r1;
    private final ModelPart wing_left;
    private final ModelPart part_l1;
    private final ModelPart part_l2;
    private final ModelPart part_l3;
    private final ModelPart part_l4;

    public V1WingModel(ModelPart root) {
        this.root = root.getChild("root");
        this.wing_base = this.root.getChild("wing_base");
        this.wing_right = this.wing_base.getChild("wing_right");
        this.part_r4 = this.wing_right.getChild("part_r4");
        this.part_r3 = this.wing_right.getChild("part_r3");
        this.part_r2 = this.wing_right.getChild("part_r2");
        this.part_r1 = this.wing_right.getChild("part_r1");
        this.wing_left = this.wing_base.getChild("wing_left");
        this.part_l1 = this.wing_left.getChild("part_l1");
        this.part_l2 = this.wing_left.getChild("part_l2");
        this.part_l3 = this.wing_left.getChild("part_l3");
        this.part_l4 = this.wing_left.getChild("part_l4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 6.6778F, 5.2778F));
        PartDefinition wing_base = root.addOrReplaceChild("wing_base", CubeListBuilder.create().texOffs(0, 0).addBox(-1.9222F, -3.8806F, 1.8194F, 4.0F, 8.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(22, 10).addBox(-0.9222F, -2.8806F, -0.1806F, 2.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(16, 10).addBox(-0.9222F, -3.8806F, 0.8194F, 2.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(14, 3).addBox(-2.9222F, 2.4194F, 2.3194F, 6.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 11).addBox(-2.9222F, 0.4194F, 2.3194F, 6.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 14).addBox(-2.9222F, -1.5806F, 2.3194F, 6.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(14, 0).addBox(-2.9222F, -3.5806F, 2.3194F, 6.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.0778F, -1.0972F, -3.0972F));
        PartDefinition wing_right = wing_base.addOrReplaceChild("wing_right", CubeListBuilder.create(), PartPose.offset(-1.9942F, -0.0972F, 3.3194F));
        PartDefinition part_r4 = wing_right.addOrReplaceChild("part_r4", CubeListBuilder.create(), PartPose.offset(-0.0057F, 2.9809F, 0.0F));
        PartDefinition cube_r1 = part_r4.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 29).addBox(-12.0F, -1.5F, 0.5F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 17).addBox(-9.0F, -1.5F, 0.5F, 6.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 23).addBox(-3.0F, -1.5F, -0.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(12, 17).addBox(-1.0F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5223F, -0.0643F, -0.5F, 0.0F, 0.0F, -0.829F));
        PartDefinition part_r3 = wing_right.addOrReplaceChild("part_r3", CubeListBuilder.create(), PartPose.offset(-0.0078F, 0.9724F, 0.0F));
        PartDefinition cube_r2 = part_r3.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(28, 16).addBox(-12.0F, -1.5F, 0.5F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(12, 21).addBox(-9.0F, -1.5F, 0.5F, 6.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(28, 6).addBox(-3.0F, -1.5F, -0.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(6, 29).addBox(-1.0F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.7202F, 0.0442F, -0.5F, 0.0F, 0.0F, -0.3927F));
        PartDefinition part_r2 = wing_right.addOrReplaceChild("part_r2", CubeListBuilder.create(), PartPose.offset(0.0285F, -1.0066F, 0.0F));
        PartDefinition cube_r3 = part_r2.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(28, 15).addBox(-13.0F, -1.5F, 0.5F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(14, 8).addBox(-10.0F, -1.5F, 0.5F, 7.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(10, 27).addBox(-3.0F, -1.5F, -0.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(18, 29).addBox(-1.0F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.7565F, 0.1232F, -0.5F, 0.0F, 0.0F, 0.3927F));
        PartDefinition part_r1 = wing_right.addOrReplaceChild("part_r1", CubeListBuilder.create(), PartPose.offset(-0.0151F, -2.9468F, 0.0F));
        PartDefinition cube_r4 = part_r1.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(30, 0).addBox(-1.0F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(28, 14).addBox(-11.0F, -1.5F, 0.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 27).addBox(-8.0F, -1.5F, 0.0F, 5.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(8, 23).addBox(-3.0F, -1.5F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5129F, 0.0634F, 0.0F, 0.0F, 0.0F, 0.829F));
        PartDefinition wing_left = wing_base.addOrReplaceChild("wing_left", CubeListBuilder.create(), PartPose.offset(2.0909F, -0.1322F, 3.3194F));
        PartDefinition part_l1 = wing_left.addOrReplaceChild("part_l1", CubeListBuilder.create(), PartPose.offset(0.0517F, -3.0509F, 0.0F));
        PartDefinition cube_r5 = part_l1.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(28, 10).addBox(-10.0F, 0.5F, -0.5F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 19).addBox(-7.0F, -0.5F, -0.5F, 6.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5352F, -0.7974F, 0.5F, 0.0F, 0.0F, 2.3126F));
        PartDefinition cube_r6 = part_l1.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(16, 23).addBox(-3.0F, -0.5F, -0.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 30).addBox(-1.0F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5352F, 0.2026F, -0.5F, 0.0F, 0.0F, 2.3126F));
        PartDefinition part_l2 = wing_left.addOrReplaceChild("part_l2", CubeListBuilder.create(), PartPose.offset(0.0304F, -0.9715F, 0.0F));
        PartDefinition cube_r7 = part_l2.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(28, 11).addBox(-13.0F, 0.5F, -0.5F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(14, 6).addBox(-10.0F, -0.5F, -0.5F, 7.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(24, 17).addBox(-3.0F, -0.5F, -1.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(26, 29).addBox(-1.0F, -0.5F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.7565F, 0.1232F, 0.5F, 0.0F, 0.0F, 2.7489F));
        PartDefinition part_l3 = wing_left.addOrReplaceChild("part_l3", CubeListBuilder.create(), PartPose.offset(-0.0334F, 1.0075F, 0.0F));
        PartDefinition cube_r8 = part_l3.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(28, 12).addBox(-12.0F, 0.5F, -0.5F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(12, 19).addBox(-9.0F, -0.5F, -0.5F, 6.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(24, 21).addBox(-3.0F, -0.5F, -1.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(22, 29).addBox(-1.0F, -0.5F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.8202F, 0.0442F, 0.5F, 0.0F, 0.0F, -2.7489F));
        PartDefinition part_l4 = wing_left.addOrReplaceChild("part_l4", CubeListBuilder.create(), PartPose.offset(-0.0487F, 3.0149F, 0.0F));
        PartDefinition cube_r9 = part_l4.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(28, 13).addBox(-12.0F, 0.5F, -0.5F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 21).addBox(-9.0F, -0.5F, -0.5F, 6.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(24, 25).addBox(-3.0F, -0.5F, -1.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(19, 27).addBox(-1.0F, -0.5F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6356F, -0.0632F, 0.5F, 0.0F, 0.0F, -2.2253F));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        float flap = (float) Math.sin(ageInTicks * 0.1f) * 0.1f;
        boolean isSliding = SlideManager.isSliding((Player) entity);
        if (isSliding) {
            this.wing_right.yRot = (float) Math.toRadians(45.0f);
            this.wing_left.yRot = (float) Math.toRadians(-45.0f);
            this.wing_base.xRot = (float) Math.toRadians(20.0f);
        } else {
            this.wing_right.zRot = flap;
            this.wing_left.zRot = -flap;
            float spread = (float) Math.sin(ageInTicks * 0.15f) * 0.05f;
            this.part_r1.zRot = spread;
            this.part_r4.zRot = -spread;
            this.part_l1.zRot = -spread;
            this.part_l4.zRot = spread;
        }
        if (entity.isCrouching()) {
            this.root.y -= 1.0f;
            this.root.z += 2.0f;
        }
    }

}