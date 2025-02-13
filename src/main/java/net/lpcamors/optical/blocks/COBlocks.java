package net.lpcamors.optical.blocks;

import com.simibubi.create.content.kinetics.BlockStressDefaults;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.ModelGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.data.TagGen;
import com.simibubi.create.foundation.utility.Couple;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.lpcamors.optical.COMod;
import net.lpcamors.optical.blocks.absorption_polarizing_filter.AbsorptionPolarizingFilter;
import net.lpcamors.optical.blocks.encased_mirror.EncasedMirrorBlock;
import net.lpcamors.optical.blocks.hologram_source.HologramSourceBlock;
import net.lpcamors.optical.blocks.optical_receptor.OpticalReceptorBlock;
import net.lpcamors.optical.blocks.optical_receptor.OpticalReceptorGenerator;
import net.lpcamors.optical.blocks.optical_sensor.OpticalSensorBlock;
import net.lpcamors.optical.blocks.optical_source.OpticalSourceBlock;
import net.lpcamors.optical.blocks.polarizing_beam_splitter_block.PolarizingBeamSplitterBlock;
import net.lpcamors.optical.blocks.beam_condenser.BeamCondenserBlock;
import net.lpcamors.optical.blocks.beam_focuser.BeamFocuserBlock;
import net.lpcamors.optical.blocks.thermal_optical_source.ThermalOpticalSourceBlock;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.material.MapColor;


public class COBlocks {

    public static final BlockEntry<OpticalSourceBlock> OPTICAL_SOURCE =
            COMod.REGISTRATE.block("optical_source", OpticalSourceBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .transform(TagGen.axeOrPickaxe())
                    .blockstate(BlockStateGen.horizontalBlockProvider(true))
                    .addLayer(() -> RenderType::cutoutMipped)
                    .item()
                    .transform(ModelGen.customItemModel())
                    .transform(BlockStressDefaults.setImpact(8.0))
                    .register();

    public static final BlockEntry<ThermalOpticalSourceBlock> THERMAL_OPTICAL_SOURCE =
            COMod.REGISTRATE.block("thermal_optical_source", ThermalOpticalSourceBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .transform(TagGen.axeOrPickaxe())
                    .blockstate(BlockStateGen.horizontalBlockProvider(true))
                    .addLayer(() -> RenderType::cutout)
                    .item()
                    .transform(ModelGen.customItemModel())
                    .transform(BlockStressDefaults.setImpact(16.0))
                    .register();


    public static final BlockEntry<OpticalReceptorBlock> LIGHT_OPTICAL_RECEPTOR =
            COMod.REGISTRATE.block("optical_receptor", OpticalReceptorBlock::light)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .transform(TagGen.axeOrPickaxe())
                    .blockstate(OpticalReceptorGenerator.LIGHT::generate)
                    .addLayer(() -> RenderType::cutoutMipped)
                    .item()
                    .transform(ModelGen.customItemModel())
                    .transform(BlockStressDefaults.setCapacity(8.0))
                    .transform(BlockStressDefaults.setGeneratorSpeed(() -> Couple.create(0, 256)))
                    .register();

    public static final BlockEntry<OpticalReceptorBlock> HEAVY_OPTICAL_RECEPTOR =
            COMod.REGISTRATE.block("heavy_optical_receptor", OpticalReceptorBlock::heavy)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .transform(TagGen.axeOrPickaxe())
                    .blockstate(OpticalReceptorGenerator.HEAVY::generate)
                    .addLayer(() -> RenderType::cutoutMipped)
                    .item()
                    .transform(ModelGen.customItemModel())
                    .transform(BlockStressDefaults.setCapacity(24.0))
                    .transform(BlockStressDefaults.setGeneratorSpeed(() -> Couple.create(0, 256)))
                    .register();


    public static final BlockEntry<EncasedMirrorBlock> ENCASED_MIRROR =
            COMod.REGISTRATE.block("encased_mirror", EncasedMirrorBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(properties -> properties)
                    .blockstate(BlockStateGen.directionalBlockProvider(true))
                    .transform(TagGen.axeOrPickaxe())
                    .addLayer(() -> RenderType::cutoutMipped)
                    .item()
                    .transform(ModelGen.customItemModel())
                    .register();



    public static final BlockEntry<AbsorptionPolarizingFilter> ABSORPTION_POLARIZING_FILTER =
            COMod.REGISTRATE.block("absorption_polarizing_filter", AbsorptionPolarizingFilter::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(properties -> properties.mapColor(MapColor.COLOR_GRAY))
                    .transform(TagGen.axeOrPickaxe())
                    .blockstate(BlockStateGen.horizontalBlockProvider(true))
                    .addLayer(() -> RenderType::translucent)
                    .item()
                    .transform(ModelGen.customItemModel())
                    .register();
    public static final BlockEntry<PolarizingBeamSplitterBlock> POLARIZING_BEAM_SPLITTER_BLOCK =
            COMod.REGISTRATE.block("polarizing_beam_splitter_block", PolarizingBeamSplitterBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(properties -> properties.mapColor(MapColor.COLOR_GRAY))
                    .transform(TagGen.axeOrPickaxe())
                    .blockstate(BlockStateGen.horizontalBlockProvider(true))
                    .addLayer(() -> RenderType::translucent)
                    .item()
                    .transform(ModelGen.customItemModel())
                    .register();
    public static final BlockEntry<OpticalSensorBlock> OPTICAL_SENSOR =
            COMod.REGISTRATE.block("optical_sensor", OpticalSensorBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .properties(p -> p.mapColor(DyeColor.GRAY).lightLevel(OpticalSensorBlock::getLight))
                    .blockstate(BlockStateGen.directionalBlockProvider(true))
                    .addLayer(() -> RenderType::translucent)
                    .item()
                    .transform(ModelGen.customItemModel())
                    .register();

    public static final BlockEntry<BeamCondenserBlock> BEAM_CONDENSER =
            COMod.REGISTRATE.block("beam_condenser", BeamCondenserBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .transform(TagGen.axeOrPickaxe())
                    .blockstate(BlockStateGen.horizontalBlockProvider(true))
                    .addLayer(() -> RenderType::cutoutMipped)
                    .item()
                    .transform(ModelGen.customItemModel())
                    .register();

    public static final BlockEntry<BeamFocuserBlock> BEAM_FOCUSER =
            COMod.REGISTRATE.block("beam_focuser", BeamFocuserBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_PURPLE))
                    .transform(TagGen.axeOrPickaxe())
                    .blockstate(BlockStateGen.horizontalBlockProvider(true))
                    .addLayer(() -> RenderType::solid)
                    .item()
                    .transform(ModelGen.customItemModel())
                    .transform(BlockStressDefaults.setImpact(4.0))
                    .transform(BlockStressDefaults.setGeneratorSpeed(() -> Couple.create(0, 256)))
                    .register();
    public static final BlockEntry<HologramSourceBlock> HOLOGRAM_SOURCE =
            COMod.REGISTRATE.block("hologram_source", HologramSourceBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .transform(TagGen.axeOrPickaxe())
                    .blockstate((c, p) -> p.horizontalBlock(c.get(), HologramSourceBlock.getBlockModel(c, p)))
                    .addLayer(() -> RenderType::solid)
                    .item()
                    .transform(ModelGen.customItemModel())
                    .register();



    public static void initiate(){}
}
