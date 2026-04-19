package com.overtime.aerothrusters.index;

import com.overtime.aerothrusters.AeroThrusters;
import com.overtime.aerothrusters.content.thruster.ThrusterBlockEntity;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

public class AeroThrustersBlockEntityTypes {

    public static final BlockEntityEntry<ThrusterBlockEntity> THRUSTER =
            AeroThrusters.registrate()
                    .blockEntity("thruster", ThrusterBlockEntity::new)
                    .validBlocks(AeroThrustersBlocks.THRUSTER)
                    .register();

    public static void init() {
        // force classloading
    }
}
