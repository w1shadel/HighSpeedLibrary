package com.maxwell.highspeedlib.client.state;

import com.maxwell.highspeedlib.api.main.mob.MobModeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * クライアント側でアクティブなボスバーを持つエンティティを管理するクラス。
 */
public class UltraBossBarManager {
    private static final Set<UUID> trackedEntities = new HashSet<>();
    private static LivingEntity currentBoss = null;

    /**
     * 特定のエンティティをボスバーの追跡対象に追加します。
     * コンストラクタ等から呼び出すことで、タグがなくてもボスバーを表示できます。
     */
    public static void track(Entity entity) {
        if (entity instanceof LivingEntity living) {
            trackedEntities.add(living.getUUID());
        }
    }

    public static LivingEntity getCurrentBoss() {
        return currentBoss;
    }

    /**
     * クライアントの毎ティック実行され、表示すべきボスを選定します。
     */
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            currentBoss = null;
            return;
        }

        List<LivingEntity> candidates = new ArrayList<>();
        
        // 周囲のエンティティを走査し、タグ保持者または追跡対象をリストアップ
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof LivingEntity living) {
                if (living.getTags().contains(MobModeManager.BOSS_TAG) || trackedEntities.contains(living.getUUID())) {
                    if (living.isAlive() && living.distanceToSqr(mc.player) < 4096) { // 64ブロック以内
                        candidates.add(living);
                    } else if (!living.isAlive()) {
                        trackedEntities.remove(living.getUUID());
                    }
                }
            }
        }

        if (candidates.isEmpty()) {
            currentBoss = null;
            return;
        }

        // 重なりを防止するため、1体だけを選定
        // ロジック：視界の中心に最も近いものを優先、いなければ距離が近いものを優先
        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;

        for (LivingEntity e : candidates) {
            double distSqr = e.distanceToSqr(mc.player);
            
            // プレイヤーの視線ベクトルとエンティティへの方向ベクトルのドット積
            var viewVec = mc.player.getViewVector(1.0f);
            var toEntity = e.position().add(0, e.getEyeHeight() * 0.5, 0).subtract(mc.player.getEyePosition(1.0f)).normalize();
            double dot = viewVec.dot(toEntity);

            // スコア計算：ドット積が大きい（視界の中心）ほど、また距離が近いほどスコアを小さく（良く）する
            // dotは最大1.0なので、重みをつけて距離から引く
            double score = distSqr - (dot * 500.0);

            if (score < bestScore) {
                bestScore = score;
                best = e;
            }
        }

        currentBoss = best;
    }
}
