package com.overtime.aerothrusters.content.thruster;

import com.mojang.serialization.MapCodec;
import com.overtime.aerothrusters.index.AeroThrustersBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class ThrusterBlock extends DirectionalBlock implements EntityBlock {

    public static final MapCodec<ThrusterBlock> CODEC = simpleCodec(ThrusterBlock::new);

    public ThrusterBlock(Properties props) {
        super(props);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
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
        if (level.isClientSide) return null;
        if (type != AeroThrustersBlockEntityTypes.THRUSTER.get()) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof ThrusterBlockEntity tbe) tbe.serverTick();
        };
    }
}
