package com.maxwell.highspeedlib.mixin;

import com.maxwell.highspeedlib.client.ClientSlideHandler;
import com.maxwell.highspeedlib.client.ClientWhiplashManager;
import com.maxwell.highspeedlib.client.ThirdPersonPunchManager;
import com.maxwell.highspeedlib.common.logic.SlideManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends LivingEntity> {
    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void highspeedlib$injectCustomAnimations(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof Player player)) return;
        HumanoidModel<?> model = (HumanoidModel<?>) (Object) this;
        float punchProgress = ThirdPersonPunchManager.getProgress(player.getId());
        float tossProgress = com.maxwell.highspeedlib.client.ThirdPersonCoinTossManager.getProgress(player.getId());
        com.maxwell.highspeedlib.common.logic.ServerWhiplashManager.HookData hookData = ClientWhiplashManager.getHookData(player.getUUID());
        int whiplashTicks = ClientWhiplashManager.getRenderTicks(player.getUUID());
        boolean hasWhiplash = whiplashTicks > 0;
        boolean isSliding = SlideManager.isSliding(player);
        boolean isFirstPerson = (entity == Minecraft.getInstance().player && Minecraft.getInstance().options.getCameraType().isFirstPerson());
        float progress = 0f;
        if (isSliding) {
            progress = 1.0f;
            if (player.level().isClientSide && player == Minecraft.getInstance().player) {
                progress = ClientSlideHandler.getSlideProgress();
            }
        }
        float offsetY = (!isFirstPerson) ? 10.0f * progress : 0.0f;
        float offsetZ = (!isFirstPerson) ? -4.0f * progress : 0.0f;
        model.body.y = offsetY;
        model.body.z = offsetZ;
        model.head.y = offsetY;
        model.head.z = offsetZ;
        model.leftLeg.y = 12.0f + offsetY;
        model.leftLeg.z = offsetZ;
        model.rightLeg.y = 12.0f + offsetY;
        model.rightLeg.z = offsetZ;
        model.rightArm.y = 2.0f + offsetY;
        model.rightArm.z = offsetZ;
        model.leftArm.y = 2.0f + offsetY;
        model.leftArm.z = offsetZ;
        if (!isSliding && punchProgress < 0 && tossProgress < 0 && !hasWhiplash) {
            syncLayers(model);
            return;
        }
        if (isSliding) {
            model.body.xRot = (float) Math.toRadians(-25.0f);
            model.head.xRot = (float) Math.toRadians(10.0f);
            model.leftLeg.xRot = (float) Math.toRadians(-90.0f);
            model.leftLeg.yRot = (float) Math.toRadians(15.0f);
            model.rightLeg.xRot = (float) Math.toRadians(-85.0f);
            model.rightLeg.yRot = (float) Math.toRadians(-15.0f);
            model.rightArm.xRot = (float) Math.toRadians(65.0f);
            if (punchProgress < 0 && tossProgress < 0 && !hasWhiplash) {
                model.leftArm.xRot = (float) Math.toRadians(65.0f);
            }
        }
        if (punchProgress >= 0) {
            float swing = ThirdPersonPunchManager.getPunchCurve(punchProgress);
            model.leftArm.xRot = (float) Math.toRadians(-90.0f);
            model.leftArm.yRot = (float) Math.toRadians(15.0f * swing);
            model.leftArm.zRot = (float) Math.toRadians(swing * 120.0f);
            model.leftArm.z = offsetZ + (-2.0f + (swing * -5.0f));
        } else if (tossProgress >= 0) {
            float swing = com.maxwell.highspeedlib.client.ThirdPersonCoinTossManager.getTossCurve(tossProgress);
            model.leftArm.xRot = (float) Math.toRadians(-20.0f + (swing * -50.0f));
            model.leftArm.yRot = (float) Math.toRadians(-25.0f);
            model.leftArm.zRot = (float) Math.toRadians(swing * 25.0f);
        } else if (hasWhiplash) {
            model.leftArm.xRot = (float) Math.toRadians(-90.0f);
            model.leftArm.yRot = (float) Math.toRadians(15.0f);
            model.leftArm.zRot = 0f;
            if (whiplashTicks < 5) {
                model.leftArm.xRot -= (float) Math.toRadians(15.0f * (5 - whiplashTicks));
            }
            if (hookData != null && (hookData.state == 2 || hookData.state == 3)) {
                float vIntensity = (hookData.state == 2) ? 0.05f : 0.02f;
                model.leftArm.x += (float) Math.sin(ageInTicks * 1.5) * vIntensity;
                model.leftArm.y += (float) Math.cos(ageInTicks * 1.3) * vIntensity;
            }
            model.leftArm.z = offsetZ - 2.0f;
        }
        if (isFirstPerson) {
            if (punchProgress >= 0 || tossProgress >= 0 || hasWhiplash) {
                model.leftArm.visible = false;
            }
        }
        syncLayers(model);
    }

    private void syncLayers(HumanoidModel<?> model) {
        if (model instanceof PlayerModel<?> playerModel) {
            playerModel.jacket.copyFrom(model.body);
            playerModel.hat.copyFrom(model.head);
            playerModel.leftPants.copyFrom(model.leftLeg);
            playerModel.rightPants.copyFrom(model.rightLeg);
            playerModel.leftSleeve.copyFrom(model.leftArm);
            playerModel.rightSleeve.copyFrom(model.rightArm);
            playerModel.leftSleeve.visible = model.leftArm.visible;
            playerModel.rightSleeve.visible = model.rightArm.visible;
        }
    }
}