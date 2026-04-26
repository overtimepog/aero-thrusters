package com.overtime.aerothrusters.index;

import com.overtime.aerothrusters.AeroThrusters;
import com.overtime.aerothrusters.content.thruster.ThrusterBlock;
import com.overtime.aerothrusters.content.thruster.ThrusterTier;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.SoundType;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;

public class AeroThrustersBlocks {

    public static final BlockEntry<ThrusterBlock> THRUSTER =
            AeroThrusters.registrate()
                    .block("thruster", ThrusterBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .transform(axeOrPickaxe())
                    .properties(p -> p.sound(SoundType.METAL).noOcclusion())
                    .blockstate((c, p) -> p.directionalBlock(c.get(),
                            p.models().getExistingFile(p.modLoc("block/thruster"))))
                    .item()
                    .transform(customItemModel())
                    .register();

    public static final BlockEntry<ThrusterBlock> ADVANCED_THRUSTER =
            AeroThrusters.registrate()
                    .block("advanced_thruster", p -> new ThrusterBlock(p, ThrusterTier.ADVANCED))
                    .initialProperties(SharedProperties::stone)
                    .transform(axeOrPickaxe())
                    .properties(p -> p.sound(SoundType.METAL).noOcclusion())
                    .blockstate((c, p) -> p.directionalBlock(c.get(),
                            p.models().getExistingFile(p.modLoc("block/advanced_thruster"))))
                    .item()
                    .transform(customItemModel())
                    .register();

    public static final BlockEntry<ThrusterBlock> SUPERIOR_THRUSTER =
            AeroThrusters.registrate()
                    .block("superior_thruster", p -> new ThrusterBlock(p, ThrusterTier.SUPERIOR))
                    .initialProperties(SharedProperties::stone)
                    .transform(axeOrPickaxe())
                    .properties(p -> p.sound(SoundType.METAL).noOcclusion())
                    .blockstate((c, p) -> p.directionalBlock(c.get(),
                            p.models().getExistingFile(p.modLoc("block/superior_thruster"))))
                    .item()
                    .transform(customItemModel())
                    .register();

    public static void init() {
        // force classloading
    }
}
