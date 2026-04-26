package com.overtime.aerothrusters.client;

import com.overtime.aerothrusters.ponder.AeroThrustersPonderPlugin;
import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public final class AeroThrustersClient {

    private AeroThrustersClient() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(AeroThrustersClient::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> PonderIndex.addPlugin(new AeroThrustersPonderPlugin()));
    }
}
