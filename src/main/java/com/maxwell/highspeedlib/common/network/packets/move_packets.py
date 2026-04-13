import os
import shutil

base_dir = r"C:\Users\stard_i5grhqf\Documents\GitHub\UltraCraft\src\main\java\com\maxwell\highspeedlib\common\network\packets"
sync_dir = os.path.join(base_dir, "sync")
action_dir = os.path.join(base_dir, "action")
effect_dir = os.path.join(base_dir, "effect")

os.makedirs(sync_dir, exist_ok=True)
os.makedirs(action_dir, exist_ok=True)
os.makedirs(effect_dir, exist_ok=True)

packets = {
    "action": ["C2SKeyInputPacket.java", "S2CStartPunchAnimationPacket.java", "S2CStartTossAnimationPacket.java"],
    "effect": ["S2CBloodSplatPacket.java", "S2CParryPacket.java", "S2CScreenShakePacket.java", "S2CSpeedEffectPacket.java"],
    "sync": ["S2CSyncAbilitiesPacket.java", "S2CSyncArmPacket.java", "S2CSyncCoinStockPacket.java", 
             "S2CSyncMobModePacket.java", "S2CSyncPunchEnergyPacket.java", "S2CSyncSlamPacket.java", 
             "S2CSyncSlidePacket.java", "S2CSyncStaminaPacket.java", "S2CSyncWhiplashPacket.java"]
}

for cat, files in packets.items():
    cat_dir = os.path.join(base_dir, cat)
    for f in files:
        src = os.path.join(base_dir, f)
        if os.path.exists(src):
            with open(src, "r", encoding="utf-8") as file:
                content = file.read()
            
            content = content.replace(
                "package com.maxwell.highspeedlib.common.network.packets;",
                f"package com.maxwell.highspeedlib.common.network.packets.{cat};"
            )
            
            dst = os.path.join(cat_dir, f)
            with open(dst, "w", encoding="utf-8") as file:
                file.write(content)
            
            os.remove(src)

print("Packets updated and moved successfully.")
