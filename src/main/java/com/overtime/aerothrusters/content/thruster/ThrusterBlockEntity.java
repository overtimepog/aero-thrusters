package com.overtime.aerothrusters.content.thruster;

import com.overtime.aerothrusters.AeroThrusters;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.api.equipment.goggles.IHaveHoveringInformation;
import dev.ryanhcode.sable.api.block.propeller.BlockEntityPropeller;
import dev.ryanhcode.sable.api.block.propeller.BlockEntitySubLevelPropellerActor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.List;

public class ThrusterBlockEntity extends BlockEntity
        implements BlockEntitySubLevelPropellerActor, BlockEntityPropeller,
                   IHaveGoggleInformation, IHaveHoveringInformation {

    public static final TagKey<Fluid> FUEL_TAG =
            TagKey.create(Registries.FLUID, AeroThrusters.asResource("thruster_fuels"));

    private static final String GOGGLE_INDENT = "    ";
    private static final String GOGGLE_INDENT_NESTED = "       ";

    private final FluidTank fuelTank = createFuelTank();

    private boolean active = false;
    private int redstonePower = 0;
    private int burnRate = 0;
    private boolean binary = false;
    private boolean prevClientActive = false;

    public ThrusterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private FluidTank createFuelTank() {
        return new FluidTank(getTier().tankCapacity, fs -> fs.getFluid().is(FUEL_TAG)) {
            @Override
            protected void onContentsChanged() {
                setChanged();
            }
        };
    }

    private ThrusterTier getTier() {
        if (getBlockState().getBlock() instanceof ThrusterBlock block) {
            return block.getTier();
        }
        return ThrusterTier.BASIC;
    }

    public IFluidHandler getFuelTank() {
        return fuelTank;
    }

    public void toggleBinary() {
        binary = !binary;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean isBinary() {
        return binary;
    }

    public void serverTick() {
        if (level == null) return;
        int power = level.getBestNeighborSignal(worldPosition);
        boolean hasFuel = fuelTank.getFluidAmount() >= 1;
        boolean shouldBeActive = power > 0 && hasFuel;

        int newBurn = 0;
        if (shouldBeActive) {
            if (binary) {
                newBurn = getTier().burnRateMax;
            } else {
                newBurn = Math.max(1, (getTier().burnRateMax * power) / 15);
            }
            fuelTank.drain(newBurn, IFluidHandler.FluidAction.EXECUTE);
        }

        if (shouldBeActive != active || power != redstonePower || newBurn != burnRate) {
            active = shouldBeActive;
            redstonePower = power;
            burnRate = newBurn;
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void clientTick() {
        if (level == null) return;
        if (active != prevClientActive) {
            if (active) emitIgnitionBurst();
            prevClientActive = active;
        }
        if (!active) return;

        Direction spout = getBlockState().getValue(DirectionalBlock.FACING);
        double throttle = binary ? 1.0 : (redstonePower / 15.0);
        var rng = level.getRandom();
        ThrusterTier tier = getTier();

        int sx = spout.getStepX(), sy = spout.getStepY(), sz = spout.getStepZ();

        double cx = worldPosition.getX() + 0.5 + sx * 0.78;
        double cy = worldPosition.getY() + 0.5 + sy * 0.78;
        double cz = worldPosition.getZ() + 0.5 + sz * 0.78;

        double[] t1, t2;
        if (sy != 0) { t1 = new double[]{1, 0, 0}; t2 = new double[]{0, 0, 1}; }
        else if (sx != 0) { t1 = new double[]{0, 1, 0}; t2 = new double[]{0, 0, 1}; }
        else { t1 = new double[]{1, 0, 0}; t2 = new double[]{0, 1, 0}; }

        double tierMult = tier == ThrusterTier.BASIC ? 1.0 : tier == ThrusterTier.ADVANCED ? 1.3 : 1.6;
        double fastVel = (0.2 + 0.35 * throttle) * tierMult;
        double medVel = (0.12 + 0.22 * throttle) * tierMult;
        double slowVel = (0.04 + 0.08 * throttle) * tierMult;
        double spread = 0.04 + 0.05 * throttle;

        var coreParticle = tier == ThrusterTier.BASIC ? ParticleTypes.END_ROD :
                tier == ThrusterTier.ADVANCED ? ParticleTypes.END_ROD : ParticleTypes.END_ROD;
        var flameParticle = tier == ThrusterTier.BASIC ? ParticleTypes.FLAME :
                tier == ThrusterTier.ADVANCED ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SOUL_FIRE_FLAME;

        // Bright white-hot core streaks — dense and visible
        int coreCount = (int) ((5 + throttle * 8) * tierMult);
        for (int i = 0; i < coreCount; i++) {
            double dist = rng.nextDouble() * 0.6;
            double u = (rng.nextDouble() - 0.5) * spread * 0.35;
            double v = (rng.nextDouble() - 0.5) * spread * 0.35;
            double velOff = 0.7 + rng.nextDouble() * 0.35;
            level.addParticle(coreParticle,
                    cx + sx * dist + t1[0] * u + t2[0] * v,
                    cy + sy * dist + t1[1] * u + t2[1] * v,
                    cz + sz * dist + t1[2] * u + t2[2] * v,
                    sx * fastVel * velOff, sy * fastVel * velOff, sz * fastVel * velOff);
        }

        // Flame sheath — fills the exhaust cone
        int flameCount = (int) ((6 + throttle * 10) * tierMult);
        for (int i = 0; i < flameCount; i++) {
            double dist = 0.1 + rng.nextDouble() * 0.5;
            double u = (rng.nextDouble() - 0.5) * spread * 1.0;
            double v = (rng.nextDouble() - 0.5) * spread * 1.0;
            double velOff = 0.55 + rng.nextDouble() * 0.3;
            level.addParticle(flameParticle,
                    cx + sx * dist + t1[0] * u + t2[0] * v,
                    cy + sy * dist + t1[1] * u + t2[1] * v,
                    cz + sz * dist + t1[2] * u + t2[2] * v,
                    sx * medVel * velOff, sy * medVel * velOff, sz * medVel * velOff);
        }

        // Spark bursts with sideways scatter
        int sparkCount = (int) ((2 + throttle * 6) * tierMult);
        for (int i = 0; i < sparkCount; i++) {
            double dist = 0.15 + rng.nextDouble() * 0.6;
            double u = (rng.nextDouble() - 0.5) * spread * 2.5;
            double v = (rng.nextDouble() - 0.5) * spread * 2.5;
            double sparkVel = medVel * (0.5 + rng.nextDouble() * 0.5);
            level.addParticle(ParticleTypes.FIREWORK,
                    cx + sx * dist + t1[0] * u + t2[0] * v,
                    cy + sy * dist + t1[1] * u + t2[1] * v,
                    cz + sz * dist + t1[2] * u + t2[2] * v,
                    sx * sparkVel + (rng.nextDouble() - 0.5) * 0.12,
                    sy * sparkVel + (rng.nextDouble() - 0.5) * 0.12,
                    sz * sparkVel + (rng.nextDouble() - 0.5) * 0.12);
        }

        // Molten ember pops
        if (throttle > 0.15 && rng.nextInt(Math.max(1, 3 - tier.ordinal())) == 0) {
            double u = (rng.nextDouble() - 0.5) * spread * 1.5;
            double v = (rng.nextDouble() - 0.5) * spread * 1.5;
            level.addParticle(ParticleTypes.LAVA,
                    cx + t1[0] * u + t2[0] * v,
                    cy + t1[1] * u + t2[1] * v,
                    cz + t1[2] * u + t2[2] * v,
                    sx * slowVel * 0.8, sy * slowVel * 0.8, sz * slowVel * 0.8);
        }

        // Dense exhaust smoke trailing behind
        int smokeCount = (int) ((3 + throttle * 7) / (tier == ThrusterTier.SUPERIOR ? 2.0 : tier == ThrusterTier.ADVANCED ? 1.5 : 1.0));
        for (int i = 0; i < smokeCount; i++) {
            double dist = 0.4 + rng.nextDouble() * 0.9;
            double u = (rng.nextDouble() - 0.5) * spread * 2.5;
            double v = (rng.nextDouble() - 0.5) * spread * 2.5;
            level.addParticle(ParticleTypes.LARGE_SMOKE,
                    cx + sx * dist + t1[0] * u + t2[0] * v,
                    cy + sy * dist + t1[1] * u + t2[1] * v,
                    cz + sz * dist + t1[2] * u + t2[2] * v,
                    sx * slowVel * 0.6, sy * slowVel * 0.6, sz * slowVel * 0.6);
        }

        // Nozzle-edge heat haze
        if (throttle > 0.1 && rng.nextInt(2) == 0) {
            double theta = rng.nextDouble() * Math.PI * 2;
            double r = 0.22 + rng.nextDouble() * 0.12;
            double u = Math.cos(theta) * r;
            double v = Math.sin(theta) * r;
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    cx + t1[0] * u + t2[0] * v - sx * 0.18,
                    cy + t1[1] * u + t2[1] * v - sy * 0.18,
                    cz + t1[2] * u + t2[2] * v - sz * 0.18,
                    sx * 0.015, sy * 0.015, sz * 0.015);
        }

        // Shock diamonds / Mach diamonds for higher tiers
        if (tier != ThrusterTier.BASIC && throttle > 0.4 && rng.nextInt(4) == 0) {
            double dist = 0.8 + rng.nextDouble() * 0.6;
            double theta = rng.nextDouble() * Math.PI * 2;
            double r = (rng.nextDouble() - 0.5) * spread * 0.8;
            double u = Math.cos(theta) * r;
            double v = Math.sin(theta) * r;
            level.addParticle(ParticleTypes.POOF,
                    cx + sx * dist + t1[0] * u + t2[0] * v,
                    cy + sy * dist + t1[1] * u + t2[1] * v,
                    cz + sz * dist + t1[2] * u + t2[2] * v,
                    sx * 0.02, sy * 0.02, sz * 0.02);
        }

        // Superior tier plasma trail
        if (tier == ThrusterTier.SUPERIOR && throttle > 0.2 && rng.nextInt(3) == 0) {
            double dist = 0.3 + rng.nextDouble() * 0.8;
            double theta = rng.nextDouble() * Math.PI * 2;
            double r = (rng.nextDouble() - 0.5) * spread * 0.5;
            double u = Math.cos(theta) * r;
            double v = Math.sin(theta) * r;
            level.addParticle(ParticleTypes.END_ROD,
                    cx + sx * dist + t1[0] * u + t2[0] * v,
                    cy + sy * dist + t1[1] * u + t2[1] * v,
                    cz + sz * dist + t1[2] * u + t2[2] * v,
                    sx * fastVel * 0.5, sy * fastVel * 0.5, sz * fastVel * 0.5);
        }
    }

    private void emitIgnitionBurst() {
        if (level == null) return;
        Direction spout = getBlockState().getValue(DirectionalBlock.FACING);
        int sx = spout.getStepX(), sy = spout.getStepY(), sz = spout.getStepZ();
        double cx = worldPosition.getX() + 0.5 + sx * 0.65;
        double cy = worldPosition.getY() + 0.5 + sy * 0.65;
        double cz = worldPosition.getZ() + 0.5 + sz * 0.65;
        var rng = level.getRandom();
        ThrusterTier tier = getTier();

        double tierMult = tier == ThrusterTier.BASIC ? 1.0 : tier == ThrusterTier.ADVANCED ? 1.4 : 1.8;
        var flameParticle = tier == ThrusterTier.BASIC ? ParticleTypes.FLAME :
                tier == ThrusterTier.ADVANCED ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SOUL_FIRE_FLAME;

        // Explosive flame burst
        for (int i = 0; i < (int)(24 * tierMult); i++) {
            double du = (rng.nextDouble() - 0.5) * 0.8;
            double dv = (rng.nextDouble() - 0.5) * 0.8;
            double dw = (rng.nextDouble() - 0.5) * 0.8;
            level.addParticle(flameParticle,
                    cx + du, cy + dv, cz + dw,
                    sx * 0.35 + du * 0.4, sy * 0.35 + dv * 0.4, sz * 0.35 + dw * 0.4);
        }

        // Bright core flash
        for (int i = 0; i < (int)(14 * tierMult); i++) {
            double du = (rng.nextDouble() - 0.5) * 0.6;
            double dv = (rng.nextDouble() - 0.5) * 0.6;
            double dw = (rng.nextDouble() - 0.5) * 0.6;
            level.addParticle(ParticleTypes.END_ROD,
                    cx + du, cy + dv, cz + dw,
                    sx * 0.45 + du * 0.3, sy * 0.45 + dv * 0.3, sz * 0.45 + dw * 0.3);
        }

        // Sparks
        for (int i = 0; i < (int)(16 * tierMult); i++) {
            double du = (rng.nextDouble() - 0.5) * 0.7;
            double dv = (rng.nextDouble() - 0.5) * 0.7;
            double dw = (rng.nextDouble() - 0.5) * 0.7;
            level.addParticle(ParticleTypes.FIREWORK,
                    cx + du, cy + dv, cz + dw,
                    sx * 0.3 + du * 0.4, sy * 0.3 + dv * 0.4, sz * 0.3 + dw * 0.4);
        }

        // Smoke puff
        int smokeCount = (int)(12 / (tier == ThrusterTier.SUPERIOR ? 2.5 : tier == ThrusterTier.ADVANCED ? 1.8 : 1.0));
        for (int i = 0; i < smokeCount; i++) {
            level.addParticle(ParticleTypes.LARGE_SMOKE,
                    cx + (rng.nextDouble() - 0.5) * 0.6,
                    cy + (rng.nextDouble() - 0.5) * 0.6,
                    cz + (rng.nextDouble() - 0.5) * 0.6,
                    sx * 0.12, sy * 0.12, sz * 0.12);
        }

        // Shockwave ring for advanced/superior
        if (tier != ThrusterTier.BASIC) {
            for (int i = 0; i < 8; i++) {
                double theta = (i / 8.0) * Math.PI * 2;
                double r = 0.3 + rng.nextDouble() * 0.2;
                double du = Math.cos(theta) * r;
                double dv = Math.sin(theta) * r;
                level.addParticle(ParticleTypes.POOF,
                        cx + du, cy + dv, cz,
                        sx * 0.15 + du * 0.2, sy * 0.15 + dv * 0.2, sz * 0.15);
            }
        }
    }

    // --- BlockEntityPropeller ---

    @Override
    public Direction getBlockDirection() {
        return getBlockState().getValue(DirectionalBlock.FACING);
    }

    @Override
    public double getAirflow() {
        return getTier().airflow;
    }

    @Override
    public double getThrust() {
        if (!active) return 0.0;
        if (binary) return getTier().maxThrust;
        return getTier().maxThrust * (redstonePower / 15.0);
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

    // --- Goggle overlay ---

    private static MutableComponent line(String indent, Component... parts) {
        MutableComponent out = Component.literal(indent);
        for (Component c : parts) out.append(c);
        return out;
    }

    private static Component translated(String key, ChatFormatting color, Object... args) {
        return Component.translatable(key, args).withStyle(color);
    }

    private static Component literal(String text, ChatFormatting color) {
        return Component.literal(text).withStyle(color);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(line(GOGGLE_INDENT,
                translated("tooltip.aerothrusters.thruster.header", ChatFormatting.WHITE)));

        String statusKey;
        ChatFormatting statusColor;
        if (active) {
            statusKey = "tooltip.aerothrusters.thruster.status.active";
            statusColor = ChatFormatting.GREEN;
        } else if (fuelTank.getFluidAmount() < 1) {
            statusKey = "tooltip.aerothrusters.thruster.status.no_fuel";
            statusColor = ChatFormatting.RED;
        } else {
            statusKey = "tooltip.aerothrusters.thruster.status.idle";
            statusColor = ChatFormatting.DARK_GRAY;
        }

        tooltip.add(line(GOGGLE_INDENT_NESTED,
                translated("tooltip.aerothrusters.thruster.status", ChatFormatting.GRAY),
                literal(" ", ChatFormatting.GRAY),
                translated(statusKey, statusColor)));

        String modeKey = binary
                ? "tooltip.aerothrusters.thruster.mode.binary"
                : "tooltip.aerothrusters.thruster.mode.analog";
        tooltip.add(line(GOGGLE_INDENT_NESTED,
                translated("tooltip.aerothrusters.thruster.mode", ChatFormatting.GRAY),
                literal(" ", ChatFormatting.GRAY),
                translated(modeKey, ChatFormatting.GOLD)));

        if (!binary) {
            int throttlePct = (int) Math.round(redstonePower / 15.0 * 100);
            tooltip.add(line(GOGGLE_INDENT_NESTED,
                    translated("tooltip.aerothrusters.thruster.throttle", ChatFormatting.GRAY),
                    literal(" " + throttlePct + "%", active ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY),
                    literal(" ", ChatFormatting.DARK_GRAY),
                    translated("tooltip.aerothrusters.thruster.signal", ChatFormatting.DARK_GRAY,
                            String.valueOf(redstonePower))));
        }

        double thrustNow = getThrust();
        double maxThrust = getTier().maxThrust;
        tooltip.add(line(GOGGLE_INDENT_NESTED,
                translated("tooltip.aerothrusters.thruster.thrust", ChatFormatting.GRAY),
                literal(" " + formatNumber(thrustNow) + " pN",
                        active ? ChatFormatting.AQUA : ChatFormatting.DARK_GRAY),
                literal(" / " + formatNumber(maxThrust) + " pN", ChatFormatting.DARK_GRAY)));

        tooltip.add(line(GOGGLE_INDENT_NESTED,
                translated("tooltip.aerothrusters.thruster.burn", ChatFormatting.GRAY),
                literal(" " + burnRate + " mB/t",
                        burnRate > 0 ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY)));

        if (isPlayerSneaking) {
            tooltip.add(line(GOGGLE_INDENT_NESTED,
                    translated("tooltip.aerothrusters.thruster.airflow_limit", ChatFormatting.GRAY),
                    literal(" " + formatNumber(getTier().airflow) + " m/s", ChatFormatting.DARK_GRAY)));
        }

        IFluidHandler fluidHandler = level == null ? null
                : level.getCapability(Capabilities.FluidHandler.BLOCK, worldPosition, null);
        if (fluidHandler == null) fluidHandler = fuelTank;
        addFuelTankTooltip(tooltip, fluidHandler);

        return true;
    }

    private void addFuelTankTooltip(List<Component> tooltip, IFluidHandler handler) {
        tooltip.add(line(GOGGLE_INDENT,
                translated("tooltip.aerothrusters.thruster.fuel", ChatFormatting.WHITE)));

        FluidStack stack = handler.getFluidInTank(0);
        if (stack.isEmpty()) {
            tooltip.add(line(GOGGLE_INDENT_NESTED,
                    translated("tooltip.aerothrusters.thruster.empty", ChatFormatting.DARK_GRAY),
                    literal(" 0 / " + handler.getTankCapacity(0) + " mB", ChatFormatting.DARK_GRAY)));
            return;
        }

        tooltip.add(line(GOGGLE_INDENT_NESTED,
                stack.getHoverName().copy().withStyle(ChatFormatting.GRAY)));
        tooltip.add(line(GOGGLE_INDENT_NESTED,
                literal(stack.getAmount() + " mB", ChatFormatting.GOLD),
                literal(" / " + handler.getTankCapacity(0) + " mB", ChatFormatting.DARK_GRAY)));
    }

    @Override
    public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (binary) {
            tooltip.add(line(GOGGLE_INDENT,
                    translated("tooltip.aerothrusters.thruster.hover_binary",
                            active ? ChatFormatting.GREEN : ChatFormatting.GRAY,
                            String.valueOf(fuelTank.getFluidAmount()),
                            String.valueOf(fuelTank.getCapacity()))));
        } else {
            int throttlePct = (int) Math.round(redstonePower / 15.0 * 100);
            tooltip.add(line(GOGGLE_INDENT,
                    translated("tooltip.aerothrusters.thruster.hover",
                            active ? ChatFormatting.GREEN : ChatFormatting.GRAY,
                            String.valueOf(throttlePct),
                            String.valueOf(fuelTank.getFluidAmount()),
                            String.valueOf(fuelTank.getCapacity()))));
        }
        return true;
    }

    private static String formatNumber(double n) {
        if (n == (long) n) return String.valueOf((long) n);
        return String.format("%.1f", n);
    }

    // --- Persistence + sync ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag tankTag = new CompoundTag();
        fuelTank.writeToNBT(registries, tankTag);
        tag.put("FuelTank", tankTag);
        tag.putInt("RedstonePower", redstonePower);
        tag.putInt("BurnRate", burnRate);
        tag.putBoolean("Active", active);
        tag.putBoolean("Binary", binary);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fuelTank.readFromNBT(registries, tag.getCompound("FuelTank"));
        redstonePower = tag.getInt("RedstonePower");
        burnRate = tag.getInt("BurnRate");
        active = tag.getBoolean("Active");
        binary = tag.getBoolean("Binary");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
