package net.lpcamors.optical.blocks.absorption_polarizing_filter;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.lpcamors.optical.blocks.IBeamReceiver;
import net.lpcamors.optical.blocks.COBlockEntities;
import net.lpcamors.optical.blocks.IBeamSource;
import net.lpcamors.optical.blocks.optical_source.BeamHelper;
import net.lpcamors.optical.COShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class AbsorptionPolarizingFilter extends HorizontalDirectionalBlock implements IWrenchable, IBeamReceiver, IBE<AbsorptionPolarizingFilterBlockEntity> {

    public static final EnumProperty<BeamHelper.BeamPolarization> POLARIZATION = EnumProperty.create("polarization", BeamHelper.BeamPolarization.class);

    public AbsorptionPolarizingFilter(Properties p_54120_) {
        super(p_54120_);
        registerDefaultState(defaultBlockState().setValue(POLARIZATION, BeamHelper.BeamPolarization.HORIZONTAL));
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState p_60555_, @NotNull BlockGetter p_60556_, @NotNull BlockPos p_60557_, @NotNull CollisionContext p_60558_) {
        return (COShapes.ABSORPTION_POLARIZING_FILTER).get(p_60555_.getValue(FACING));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_49915_) {
        super.createBlockStateDefinition(p_49915_.add(FACING, POLARIZATION));
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection().getClockWise().getOpposite()).setValue(POLARIZATION, BeamHelper.BeamPolarization.VERTICAL);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if(context.getClickedFace().getAxis().isHorizontal()){
            BlockState state1 = state.setValue(POLARIZATION, state.getValue(POLARIZATION).getNextRotated(1));
            context.getLevel().setBlock(context.getClickedPos(), state1, 3);
            return InteractionResult.SUCCESS;
        }
        return IWrenchable.super.onWrenched(state, context);
    }


    @Override
    public boolean useCenteredIncidence() {
        return true;
    }

    @Override
    public void receive(IBeamSource iBeamSource, BlockState state, BlockPos lastPos, BeamHelper.BeamProperties beamProperties, int lastIndex) {
        Direction direction = beamProperties.direction;
        if(direction.getAxis().isVertical() || direction.getAxis().equals(state.getValue(FACING).getAxis())) return;
        BeamHelper.BeamPolarization beamPolarization = state.getValue(POLARIZATION);
        if(beamPolarization.isDiagonal()){
            if(!direction.equals(state.getValue(FACING).getCounterClockWise())){
                beamPolarization = beamPolarization.getNextRotated(2);
            }
        }
        float intensity = beamProperties.beamPolarization.getRemainingIntensity(beamProperties.intensity, beamPolarization);
        if(intensity > 0){
            BeamHelper.BeamProperties beamProperties1 = new BeamHelper.BeamProperties(intensity, beamPolarization, beamProperties.color, direction, beamProperties.spin, beamProperties.beamType);
            IBeamSource.propagateLinearBeamVar(iBeamSource, lastPos, beamProperties1, lastIndex);
        }
    }

    @Override
    public Class<AbsorptionPolarizingFilterBlockEntity> getBlockEntityClass() {
        return AbsorptionPolarizingFilterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends AbsorptionPolarizingFilterBlockEntity> getBlockEntityType() {
        return COBlockEntities.ABSORPTION_POLARIZING_FILTER.get();
    }
}
