package com.maxwell.highspeedlib.mixin;

import com.maxwell.highspeedlib.client.ThirdPersonPunchManager;
import com.maxwell.highspeedlib.logic.SlideManager;
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
    private void highspeedlib$customPoses(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof Player player)) return;
        HumanoidModel<?> model = (HumanoidModel<?>)(Object)this;

        // 1. 各種状態の取得
        float punchProgress = ThirdPersonPunchManager.getProgress(player.getId());
        boolean isSliding = SlideManager.isSliding(player);

        // 2. スライディングポーズの適用 (足と右腕、胴体)
        if (isSliding) {
            // 足を前に投げ出すポーズ
            model.leftLeg.xRot = (float) Math.toRadians(-90.0f);
            model.leftLeg.yRot = (float) Math.toRadians(10.0f);
            model.rightLeg.xRot = (float) Math.toRadians(-90.0f);
            model.rightLeg.yRot = (float) Math.toRadians(-10.0f);

            // 右腕は後ろに流す（左腕は後でパンチ判定を行う）
            model.rightArm.xRot = (float) Math.toRadians(60.0f);

            // 胴体と頭の角度
            model.body.xRot = (float) Math.toRadians(-20.0f);
            model.head.xRot = (float) Math.toRadians(20.0f);

            // 【重要】ズボンのレイヤーも同期させる
            if (model instanceof PlayerModel<?> playerModel) {
                playerModel.leftPants.copyFrom(model.leftLeg);
                playerModel.rightPants.copyFrom(model.rightLeg);
                playerModel.rightSleeve.copyFrom(model.rightArm);
                playerModel.jacket.copyFrom(model.body);
            }
        }

        // 3. パンチポーズの適用 (左腕)
        // スライディング中であってもパンチを優先する
        if (punchProgress >= 0) {
            float swing = ThirdPersonPunchManager.getPunchCurve(punchProgress);

            model.leftArm.xRot = (float) Math.toRadians(-90.0f);
            model.leftArm.yRot = (float) Math.toRadians(15.0f * swing);
            model.leftArm.zRot = (float) Math.toRadians(swing * 120.0f);
            model.leftArm.z = -2.0f + (swing * -5.0f);

            // 袖のレイヤーを同期
            if (model instanceof PlayerModel<?> playerModel) {
                playerModel.leftSleeve.copyFrom(model.leftArm);
            }
        } else if (isSliding) {
            // パンチしていない時のスライディング中の左腕
            model.leftArm.xRot = (float) Math.toRadians(60.0f);
            if (model instanceof PlayerModel<?> playerModel) {
                playerModel.leftSleeve.copyFrom(model.leftArm);
            }
        }
    }
}