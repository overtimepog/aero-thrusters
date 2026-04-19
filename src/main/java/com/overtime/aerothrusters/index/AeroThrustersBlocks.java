package com.overtime.aerothrusters.index;

import com.overtime.aerothrusters.AeroThrusters;
import com.overtime.aerothrusters.content.thruster.ThrusterBlock;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.SoundType;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;

public class AeroThrustersBlocks {

    private static final AeroThrustersRegistrateAlias R = new AeroThrustersRegistrateAlias();

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

    public static void init() {
        // force classloading
    }

    private static final class AeroThrustersRegistrateAlias {}
}
