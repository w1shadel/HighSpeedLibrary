package com.maxwell.highspeedlib.common.logic;

import com.maxwell.highspeedlib.common.network.PacketHandler;
import com.maxwell.highspeedlib.common.network.packets.S2CSyncAbilitiesPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbilityAuthority {
    private static final Map<UUID, PlayerSettings> settingsMap = new HashMap<>();

    public static PlayerSettings get(UUID uuid) {
        return settingsMap.computeIfAbsent(uuid, k -> new PlayerSettings());
    }

    public static boolean canDash(UUID uuid) {
        return get(uuid).dash;
    }

    public static boolean canPunch(UUID uuid) {
        return get(uuid).punch;
    }

    public static boolean canWhiplash(UUID uuid) {
        return get(uuid).whiplash;
    }

    public static boolean canWallJump(UUID uuid) {
        return get(uuid).wallJump;
    }

    public static boolean canSlide(UUID uuid) {
        return get(uuid).sliding;
    }

    public static boolean canSlam(UUID uuid) {
        return get(uuid).slam;
    }

    public static void sync(ServerPlayer player) {
        PlayerSettings s = get(player.getUUID());
        PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                new S2CSyncAbilitiesPacket(
                        s.dash,
                        s.punch,
                        s.whiplash,
                        s.sliding,
                        s.slam,
                        s.wallJump,
                        s.maxCoinCount
                ));
    }

    public static class PlayerSettings {
        public boolean punch = true;
        public boolean whiplash = true;
        public boolean dash = true;
        public boolean wallJump = true;
        public boolean sliding = true;
        public boolean slam = true;
        public double punchDamageBase = 4.0;
        public int maxDashCount = 3;
        public int maxWallJumpCount = 3;
        public int maxCoinCount = 4;

        public CompoundTag save() {
            CompoundTag nbt = new CompoundTag();
            nbt.putBoolean("punch", punch);
            nbt.putBoolean("whiplash", whiplash);
            nbt.putBoolean("dash", dash);
            nbt.putBoolean("wallJump", wallJump);
            nbt.putBoolean("sliding", sliding);
            nbt.putBoolean("slam", slam);
            nbt.putDouble("punchDamageBase", punchDamageBase);
            nbt.putInt("maxDashCount", maxDashCount);
            nbt.putInt("maxWallJumpCount", maxWallJumpCount);
            nbt.putInt("maxCoinCount", maxCoinCount);
            return nbt;
        }

        public void load(CompoundTag nbt) {
            if (nbt.contains("punch")) punch = nbt.getBoolean("punch");
            if (nbt.contains("whiplash")) whiplash = nbt.getBoolean("whiplash");
            if (nbt.contains("dash")) dash = nbt.getBoolean("dash");
            if (nbt.contains("wallJump")) wallJump = nbt.getBoolean("wallJump");
            if (nbt.contains("sliding")) sliding = nbt.getBoolean("sliding");
            if (nbt.contains("slam")) slam = nbt.getBoolean("slam");
            if (nbt.contains("punchDamageBase")) punchDamageBase = nbt.getDouble("punchDamageBase");
            if (nbt.contains("maxDashCount")) maxDashCount = nbt.getInt("maxDashCount");
            if (nbt.contains("maxWallJumpCount")) maxWallJumpCount = nbt.getInt("maxWallJumpCount");
            if (nbt.contains("maxCoinCount")) maxCoinCount = nbt.getInt("maxCoinCount");
        }
    }
}