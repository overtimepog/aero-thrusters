package com.overtime.aerothrusters.content.thruster;

import com.mojang.serialization.MapCodec;
import com.overtime.aerothrusters.index.AeroThrustersBlockEntityTypes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class ThrusterBlock extends DirectionalBlock implements EntityBlock, IWrenchable {

    public static final MapCodec<ThrusterBlock> CODEC = simpleCodec(ThrusterBlock::new);

    private final ThrusterTier tier;

    public ThrusterBlock(Properties props) {
        this(props, ThrusterTier.BASIC);
    }

    public ThrusterBlock(Properties props, ThrusterTier tier) {
        super(props);
        this.tier = tier;
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    public ThrusterTier getTier() {
        return tier;
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getNearestLookingDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return AeroThrustersBlockEntityTypes.THRUSTER.create(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != AeroThrustersBlockEntityTypes.THRUSTER.get()) return null;
        if (level.isClientSide) {
            return (lvl, pos, st, be) -> {
                if (be instanceof ThrusterBlockEntity tbe) tbe.clientTick();
            };
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof ThrusterBlockEntity tbe) tbe.serverTick();
        };
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (level.getBlockEntity(pos) instanceof ThrusterBlockEntity be) {
            be.toggleBinary();
            if (!level.isClientSide) {
                level.sendBlockUpdated(pos, state, state, 3);
                IWrenchable.playRotateSound(level, pos);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
