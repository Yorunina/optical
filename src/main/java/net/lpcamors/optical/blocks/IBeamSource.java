package net.lpcamors.optical.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.RenderTypes;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;
import net.lpcamors.optical.COMod;
import net.lpcamors.optical.COPartialModels;
import net.lpcamors.optical.COUtils;
import net.lpcamors.optical.blocks.absorption_polarizing_filter.AbsorptionPolarizingFilter;
import net.lpcamors.optical.blocks.optical_source.BeamHelper;
import net.lpcamors.optical.data.COTags;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Map;

public interface IBeamSource {

    BeamHelper.BeamProperties getInitialBeamProperties();
    //void propagateLinearBeamVar(BlockPos initialPos, BeamHelper.BeamProperties beamProperties, int lastIndex);
    void addToBeamBlocks(Vec3i vec, Vec3i vec1, BeamHelper.BeamProperties beamProperties);
    BlockPos getBlockPos();
    Level getLevel();
    Map<Pair<Vec3i, Vec3i>, BeamHelper.BeamProperties> getBeamPropertiesMap();
    boolean isDependent(BlockPos pos);
    void addDependent(BlockPos pos);
    int getTickCount();
    boolean shouldRendererLaserBeam();



    static void propagateLinearBeamVar(IBeamSource iBeamSource, BlockPos initialPos, BeamHelper.BeamProperties beamProperties, int lastIndex) {
        if(iBeamSource.getInitialBeamProperties() == null) return;
        BlockPos lastPos = initialPos;
        Direction direction = beamProperties.direction;
        BeamHelper.BeamType beamType = iBeamSource.getInitialBeamProperties().getType();
        int range = beamType.getRange();
        for (int i = 0; i + lastIndex <= range; i++) {
            lastPos = lastPos.relative(direction);
            Vec3i vec3 = lastPos;
            BlockState state = iBeamSource.getLevel().getBlockState(lastPos);
            boolean penetrable = state.is(COTags.Blocks.PENETRABLE) && !state.is(COTags.Blocks.IMPENETRABLE);

            //Check if there's and living entity in the way
            LivingEntity livingEntity = IBeamReceiver.getNearLivingEntity(iBeamSource.getLevel(), lastPos, IBeamReceiver.LIVING_ENTITY_EXTENDED_RADIUS, direction).orElse(null);
            if(livingEntity != null && (penetrable || state.getBlock() instanceof IBeamReceiver)){
                beamType.livingEntityBiConsumer.accept(livingEntity, beamProperties);
                if(!iBeamSource.getInitialBeamProperties().canPassThroughEntities()) {
                    iBeamSource.addToBeamBlocks(initialPos, vec3, beamProperties);
                    break;
                }
            }

            // Check if the beam passes through a ILaserReceiver
            if(state.getBlock() instanceof IBeamReceiver iBeamReceiver) {
                iBeamSource.addToBeamBlocks(initialPos, vec3, beamProperties);
                iBeamReceiver.receive(iBeamSource, state, lastPos, beamProperties, i + 1);
                iBeamSource.getLevel().sendBlockUpdated(lastPos, state, state, 16);
                break;

                // Check if there is a BeaconBeamBlock in the way(colorizes the beam)
            } else if(state.getBlock() instanceof BeaconBeamBlock beaconBeamBlock) {
                iBeamSource.addToBeamBlocks(initialPos, vec3, beamProperties);
                BeamHelper.BeamProperties beamProperties1 = new BeamHelper.BeamProperties(beamProperties.intensity, beamProperties.beamPolarization, BeamHelper.colorSum(beamProperties.color, COUtils.getColor(beaconBeamBlock.getColor()) ), direction, beamProperties.spin, beamProperties.beamType);
                IBeamSource.propagateLinearBeamVar(iBeamSource, lastPos, beamProperties1, i + 1);
                break;

                // Check if the beam range ended
            } else if(i + lastIndex >= range || !penetrable){
                iBeamSource.addToBeamBlocks(initialPos, vec3, beamProperties);
                beamType.blockStateBiConsumer.accept(iBeamSource.getLevel().getBlockState(lastPos), beamProperties);
                break;
            }
        }
    }


    @OnlyIn(Dist.CLIENT)
    class ClientSide {

        public static final ResourceLocation LASER_BEAM_LOCATION = new ResourceLocation(COMod.ID, "textures/block/optical_source/optical_source_laser_beam.png");
        static final RenderType LASER_BEAM_RENDER_TYPE = RenderType.entityTranslucentEmissive(LASER_BEAM_LOCATION, true);

        @Deprecated
        public static void renderLaserBeam(IBeamSource iBeamSource, float partialTicks, PoseStack ms, MultiBufferSource multiBufferSource, int light) {
            Vec3 pos = iBeamSource.getBlockPos().getCenter();
            List<Pair<Vec3i, Vec3i>> blockPosToBeam = iBeamSource.getBeamPropertiesMap().keySet().stream().toList();
            for (int i = 0; i < blockPosToBeam.size(); i++) {
                Pair<Vec3i, Vec3i> pair = blockPosToBeam.get(i);
                BeamHelper.BeamProperties beamProperties = iBeamSource.getBeamPropertiesMap().get(pair);
                Vec3i rgb = beamProperties.color;
                int alpha = (int) (beamProperties.intensity * 255);
                Vec3 vec = Vec3.atCenterOf(pair.getFirst());
                Vec3 vec1 = Vec3.atCenterOf(pair.getSecond());

                Vec3 v = IBeamReceiver.getLaserIrradiatedFaceOffset(beamProperties.direction, new BlockPos(pair.getFirst()), iBeamSource.getLevel());
                Vec3 v1 = IBeamReceiver.getLaserIrradiatedFaceOffset(beamProperties.direction, new BlockPos(pair.getSecond()), iBeamSource.getLevel());
                vec = vec.add(v);
                vec1 = vec1.add(v1);

                double x = vec1.x() - vec.x();
                double y = vec1.y() - vec.y();
                double z = vec1.z() - vec.z();
                float f = Mth.sqrt((float) (x * x + z * z));
                float f1 = Mth.sqrt((float) (x * x + y * y + z * z));
                ms.pushPose();
                ms.translate(0.5f, 0.5F, 0.5F);
                ms.translate(vec.x - pos.x, vec.y - pos.y, vec.z - pos.z);

                ms.mulPose(Axis.YP.rotation((float) (-Math.atan2(z, x) + Math.PI / 2)));
                ms.mulPose(Axis.XP.rotation((float) (Math.atan2(f, y) - Math.PI / 2)));
                VertexConsumer vertexconsumer = multiBufferSource.getBuffer(RenderTypes.getGlowingTranslucent(LASER_BEAM_LOCATION));
                int intensityMultiplier = 1 + (int) Math.floor(beamProperties.intensity / 16);
                float t = (partialTicks + iBeamSource.getTickCount()) * intensityMultiplier;
                float f2 = 0.0F - t * 1e-2F;
                float f3 = Mth.sqrt((float) (x * x + y * y + z * z)) / 32.0F - t * 0.01F;
                float radius = 0.05f;
                float f4 = 0.0F;
                float f5 = radius;
                float f6 = 0.0F;
                PoseStack.Pose posestack$pose = ms.last();
                Matrix4f matrix4f = posestack$pose.pose();
                Matrix3f matrix3f = posestack$pose.normal();
                for (int k = 0; k < 3 + intensityMultiplier; k++) {
                    if (k > 0) {
                        radius *= 1.2F;
                        vertexconsumer = multiBufferSource.getBuffer(RenderTypes.getOutlineTranslucent(LASER_BEAM_LOCATION, true));
                        alpha = (int) (alpha * 0.75F);
                    }
                    for (int j = 1; j <= 8; ++j) {
                        float f7 = Mth.sin((float) j * ((float) Math.PI * 2F) / 8.0F) * radius;
                        float f8 = Mth.cos((float) j * ((float) Math.PI * 2F) / 8.0F) * radius;
                        float f9 = (float) j / 8.0F;

                        vertexconsumer.vertex(matrix4f, f4, f5, 0.0F).color(rgb.getX(), rgb.getY(), rgb.getZ(), alpha).uv(f6, f2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
                        vertexconsumer.vertex(matrix4f, f4, f5, f1).color(rgb.getX(), rgb.getY(), rgb.getZ(), alpha).uv(f6, f3).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
                        vertexconsumer.vertex(matrix4f, f7, f8, f1).color(rgb.getX(), rgb.getY(), rgb.getZ(), alpha).uv(f9, f3).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
                        vertexconsumer.vertex(matrix4f, f7, f8, 0.0F).color(rgb.getX(), rgb.getY(), rgb.getZ(), alpha).uv(f9, f2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();

                        f4 = f7;
                        f5 = f8;
                        f6 = f9;
                    }
                }

                ms.popPose();
            }
        }

        public static void renderLaserBeam(IBeamSource be, BlockState state, PoseStack ms, MultiBufferSource buffer) {

            Vec3 pos = be.getBlockPos().getCenter();
            //IBeamSource.ClientSide.renderLaserBeam(opticalLaserSourceBlockEntity, partialTicks, ms, buffer, light);

            List<Pair<Vec3i, Vec3i>> blockPosToBeam = be.getBeamPropertiesMap().keySet().stream().toList();

            Direction direction = state.getValue(AbsorptionPolarizingFilter.FACING);

            for (int i = 0; i < blockPosToBeam.size(); i++) {

                Pair<Vec3i, Vec3i> pair = blockPosToBeam.get(i);

                BeamHelper.BeamProperties beamProperties = be.getBeamPropertiesMap().get(pair);
                direction = beamProperties.direction;
                Vec3 start0 = Vec3.atCenterOf(pair.getFirst());
                Vec3 end0 = Vec3.atCenterOf(pair.getSecond());

                Vec3 start = start0.subtract(IBeamReceiver.getLaserIrradiatedFaceOffsetVar(beamProperties.direction, new BlockPos(pair.getFirst()), be.getLevel()));
                Vec3 end = end0.add(IBeamReceiver.getLaserIrradiatedFaceOffsetVar(beamProperties.direction, new BlockPos(pair.getSecond()), be.getLevel()));

                ms.pushPose();

                translateForVec(ms, start0.subtract(pos));
                translateForVec(ms, start.subtract(start0));
                translateForVec(ms, end.subtract(start).multiply(0.5D, 0.5D, 0.5D));
                //Vec3 vec2 = end.subtract(start);
                //ms.translate(vec2.x / 2, vec3.y / 2,vec3.z / 2);


                float f = (float) end.subtract(start).length();
                float f1 = (float) end.subtract(start).length();

                SuperByteBuffer laser = CachedBufferer.partial(COPartialModels.LASER_BEAM, state)
                        .light(LightTexture.FULL_BRIGHT)
                        .disableDiffuse();


                Vec3 dir = direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ?
                        Vec3.atLowerCornerOf(direction.getNormal()) :
                        Vec3.atLowerCornerOf(direction.getNormal()).scale(-1);
                Vec3 nDir = new Vec3(1, 1, 1).subtract(dir);

                Vec3i color = beamProperties.color;
                int jMax = 3 + (int) Math.floor(beamProperties.intensity / 32);
                int jRest = Math.max(0, jMax - 10);
                jMax = Math.min(10, jMax);
                for (int j = 0; j < jMax; j++) {
                    SuperByteBuffer laser0 = laser;
                    double radius = 0.8 + (j + jRest) * 0.2;
                    int alpha = (int) (255 * (1 - j / 10F));
                    laser0.color(color.getX(), color.getY(), color.getZ(), alpha);
                    scaleForVec(laser0, dir.scale(f).add(nDir));
                    scaleForVec(laser0, nDir.scale(radius).add(dir));
                    rotateDirection(laser0, direction);
                    laser0.renderInto(ms, j == 0 ? buffer.getBuffer(RenderTypes.getAdditive()) : buffer.getBuffer(RenderType.translucentNoCrumbling()));
                }
                ms.popPose();
            }
        }

        static void translateForVec(PoseStack ms, Vec3 vec3) {
            ms.translate(vec3.x, vec3.y, vec3.z);
        }

        static void scaleForVec(SuperByteBuffer s, Vec3 vec3) {
            s.centre().scale((float) vec3.x, (float) vec3.y, (float) vec3.z).unCentre();
        }

        static void rotateDirection(SuperByteBuffer buffer, Direction direction) {
            float yRot = (float) (AngleHelper.horizontalAngle(direction) * Math.PI / 180f);
            float xRot = direction.getStepY() * (float) Math.PI / 2F;
            buffer.rotateCentered(Direction.UP, yRot);
            buffer.rotateCentered(Direction.EAST, xRot);

        }
    }


}
