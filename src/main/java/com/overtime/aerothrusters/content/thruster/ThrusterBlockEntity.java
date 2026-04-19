package com.overtime.aerothrusters.content.thruster;

import com.overtime.aerothrusters.AeroThrusters;
import dev.ryanhcode.sable.api.block.propeller.BlockEntityPropeller;
import dev.ryanhcode.sable.api.block.propeller.BlockEntitySubLevelPropellerActor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class ThrusterBlockEntity extends BlockEntity
        implements BlockEntitySubLevelPropellerActor, BlockEntityPropeller {

    public static final TagKey<Fluid> FUEL_TAG =
            TagKey.create(Registries.FLUID, AeroThrusters.asResource("thruster_fuels"));

    private static final int TANK_CAPACITY = 4000;      // mB
    private static final int BURN_RATE_MAX = 2;         // mB/tick at full signal
    private static final double CONFIG_THRUST = 500.0;  // Sable thrust units
    private static final double CONFIG_AIRFLOW = 20.0;  // m/s — above this, backwind kills thrust

    private final FluidTank fuelTank = new FluidTank(TANK_CAPACITY, fs -> fs.getFluid().is(FUEL_TAG)) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private boolean active = false;
    private int redstonePower = 0;

    public ThrusterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public IFluidHandler getFuelTank() {
        return fuelTank;
    }

    public void serverTick() {
        if (level == null) return;
        int power = level.getBestNeighborSignal(worldPosition);
        boolean hasFuel = fuelTank.getFluidAmount() >= 1;
        boolean shouldBeActive = power > 0 && hasFuel;

        if (shouldBeActive) {
            int burn = Math.max(1, (BURN_RATE_MAX * power) / 15);
            fuelTank.drain(burn, IFluidHandler.FluidAction.EXECUTE);
        }

        if (shouldBeActive != active || power != redstonePower) {
            active = shouldBeActive;
            redstonePower = power;
            setChanged();
        }
    }

    // --- BlockEntityPropeller ---

    @Override
    public Direction getBlockDirection() {
        return getBlockState().getValue(DirectionalBlock.FACING);
    }

    @Override
    public double getAirflow() {
        return CONFIG_AIRFLOW;
    }

    @Override
    public double getThrust() {
        if (!active) return 0.0;
        return CONFIG_THRUST * (redstonePower / 15.0);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    // --- BlockEntitySubLevelPropellerActor ---

    @Override
    public BlockEntityPropeller getPropeller() {
        return this;
    }

    // --- Persistence ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag tankTag = new CompoundTag();
        fuelTank.writeToNBT(registries, tankTag);
        tag.put("FuelTank", tankTag);
        tag.putInt("RedstonePower", redstonePower);
        tag.putBoolean("Active", active);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fuelTank.readFromNBT(registries, tag.getCompound("FuelTank"));
        redstonePower = tag.getInt("RedstonePower");
        active = tag.getBoolean("Active");
    }
}
