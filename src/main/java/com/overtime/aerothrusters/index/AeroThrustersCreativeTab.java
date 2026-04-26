package com.overtime.aerothrusters.index;

import com.overtime.aerothrusters.AeroThrusters;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

public class AeroThrustersCreativeTab {

    public static final RegistryEntry<CreativeModeTab, CreativeModeTab> MAIN =
            AeroThrusters.registrate()
                    .defaultCreativeTab("main", tab -> tab
                            .title(net.minecraft.network.chat.Component.translatable("itemGroup.aerothrusters"))
                            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                            .icon(() -> AeroThrustersBlocks.THRUSTER.asStack()))
                    .register();

    public static void init() {
        // force classloading; Registrate's defaultCreativeTab() marks every later
        // item/block in this Registrate as belonging to this tab by default
    }
}
