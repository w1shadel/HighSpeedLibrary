package com.maxwell.highspeedlib.mixin;

import com.maxwell.highspeedlib.client.logic.ThirdPersonPunchManager;
import com.maxwell.highspeedlib.client.logic.ThirdPersonCoinTossManager;
import com.maxwell.highspeedlib.client.logic.ClientWhiplashManager;
import com.maxwell.highspeedlib.common.logic.movement.SlideManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class PlayerAnimationMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @Inject(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"
            )
    )
    private void highspeedlib$overridePoseBeforeRender(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (!(entity instanceof Player player)) return;
        if (!SlideManager.isSliding(player)) return;

        LivingEntityRenderer<T, M> renderer = (LivingEntityRenderer<T, M>) (Object) this;
        if (!(renderer.getModel() instanceof PlayerModel<?> model)) return;

        float punchProgress = ThirdPersonPunchManager.getProgress(player.getId());
        float tossProgress = ThirdPersonCoinTossManager.getProgress(player.getId());
        int whiplashTicks = ClientWhiplashManager.getRenderTicks(player.getUUID());
        boolean hasWhiplash = whiplashTicks > 0;




        model.body.xRot = 0.0f;
        model.body.yRot = (float) Math.toRadians(15.0f); 
        model.body.zRot = 0.0f;

        model.head.yRot = (float) Math.toRadians(-8.0f);

        model.rightArm.xRot = (float) Math.toRadians(-35.0f); 
        model.rightArm.yRot = (float) Math.toRadians(-10.0f);
        model.rightArm.zRot = (float) Math.toRadians(15.0f);

        if (punchProgress < 0 && tossProgress < 0 && !hasWhiplash) {
            model.leftArm.xRot = (float) Math.toRadians(30.0f); 
            model.leftArm.yRot = (float) Math.toRadians(10.0f);
            model.leftArm.zRot = (float) Math.toRadians(-35.0f); 
        }
        model.rightLeg.xRot = (float) Math.toRadians(-92.0f); 
        model.rightLeg.yRot = 0.0f;                          
        model.rightLeg.zRot = 0.0f;                          
        model.rightLeg.x = -1.9f;                            

        model.leftLeg.xRot = (float) Math.toRadians(-75.0f);  
        model.leftLeg.yRot = (float) Math.toRadians(15.0f);   
        model.leftLeg.zRot = (float) Math.toRadians(32.0f);   
        model.leftLeg.x = 2.5f;

        model.hat.copyFrom(model.head);
        model.jacket.copyFrom(model.body);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightSleeve.copyFrom(model.rightArm);
        model.leftPants.copyFrom(model.leftLeg);
        model.rightPants.copyFrom(model.rightLeg);
    }
}