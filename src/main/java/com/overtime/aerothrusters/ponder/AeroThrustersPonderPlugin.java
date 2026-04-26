package com.overtime.aerothrusters.ponder;

import com.overtime.aerothrusters.AeroThrusters;
import com.overtime.aerothrusters.index.AeroThrustersBlocks;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class AeroThrustersPonderPlugin implements PonderPlugin {

    @Override
    public String getModId() {
        return AeroThrusters.ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<RegistryEntry<?, ?>> HELPER =
                helper.withKeyFunction(RegistryEntry::getId);

        HELPER.addStoryBoard(AeroThrustersBlocks.THRUSTER,
                "thruster/overview", ThrusterScenes::thrusterOverview);
        HELPER.addStoryBoard(AeroThrustersBlocks.ADVANCED_THRUSTER,
                "thruster/overview", ThrusterScenes::thrusterOverview);
        HELPER.addStoryBoard(AeroThrustersBlocks.SUPERIOR_THRUSTER,
                "thruster/overview", ThrusterScenes::thrusterOverview);
    }
}
