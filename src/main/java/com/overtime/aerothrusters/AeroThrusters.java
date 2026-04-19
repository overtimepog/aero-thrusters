package com.overtime.aerothrusters;

import com.overtime.aerothrusters.index.AeroThrustersBlockEntityTypes;
import com.overtime.aerothrusters.index.AeroThrustersBlocks;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.minecraft.resources.ResourceLocation;

@Mod(AeroThrusters.ID)
public class AeroThrusters {

    public static final String ID = "aerothrusters";

    private static final NonNullSupplier<AeroThrustersRegistrate> REGISTRATE =
            NonNullSupplier.lazy(() -> AeroThrustersRegistrate.create(ID));

    public AeroThrusters(IEventBus modBus) {
        AeroThrustersBlocks.init();
        AeroThrustersBlockEntityTypes.init();

        registrate().registerEventListeners(modBus);
        modBus.addListener(AeroThrusters::registerCapabilities);
    }

    public static AeroThrustersRegistrate registrate() {
        return REGISTRATE.get();
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                AeroThrustersBlockEntityTypes.THRUSTER.get(),
                (be, side) -> be.getFuelTank()
        );
    }
}
