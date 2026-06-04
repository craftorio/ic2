// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core;

import ic2.core.block.beam.RenderBeam;
import ic2.core.item.logistics.ItemPumpCover;
import ic2.core.item.logistics.PumpCoverType;
import java.util.Objects;
import net.minecraft.block.properties.IProperty;
import ic2.core.ref.MetaTeBlock;
import ic2.core.block.transport.items.PipeType;
import ic2.core.item.block.ItemFluidPipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import ic2.core.item.ElectricItemTooltipHandler;
import ic2.core.network.RpcHandler;
import net.minecraftforge.common.MinecraftForge;
import ic2.core.audio.PositionSpec;
import java.io.File;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import javax.swing.JOptionPane;
import java.awt.Component;
import javax.swing.JFrame;
import org.lwjgl.opengl.Display;
import net.minecraftforge.fml.client.SplashProgress;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fluids.Fluid;
import java.util.Iterator;
import ic2.core.gui.GlTexture;
import ic2.core.block.beam.EntityParticle;
import ic2.core.util.Util;
import net.minecraft.client.renderer.entity.RenderSnowball;
import ic2.core.block.EntityDynamite;
import ic2.core.item.RenderIC2Boat;
import net.minecraft.entity.item.EntityBoat;
import ic2.core.item.EntityIC2Boat;
import ic2.core.item.tool.RenderCrossed;
import net.minecraft.util.ResourceLocation;
import ic2.core.item.tool.EntityMiningLaser;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import ic2.core.block.RenderExplosiveBlock;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import ic2.core.block.EntityIC2Explosive;
import ic2.core.block.generator.tileentity.TileEntityWaterGenerator;
import ic2.core.block.generator.tileentity.TileEntityWindGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityWaterKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityWindKineticGenerator;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.KineticGeneratorRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import ic2.core.block.personal.TileEntityPersonalChestRenderer;
import ic2.core.block.personal.TileEntityPersonalChest;
import ic2.core.profile.ProfileManager;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import ic2.core.item.tool.RenderObscurator;
import ic2.core.item.FluidCellModel;
import ic2.core.block.transport.items.PipeModel;
import ic2.core.block.wiring.CableModel;
import ic2.core.crop.CropModel;
import ic2.core.model.IReloadableModel;
import ic2.core.block.RenderBlockWall;
import ic2.core.model.Ic2ModelLoader;
import ic2.core.ref.IFluidModelProvider;
import ic2.core.ref.FluidName;
import ic2.core.ref.IItemModelProvider;
import ic2.core.ref.ItemName;
import ic2.core.block.BlockTileEntity;
import ic2.core.block.TeBlockRegistry;
import ic2.core.ref.IBlockModelProvider;
import ic2.core.util.LogCategory;
import ic2.core.ref.BlockName;
import net.minecraft.command.ICommand;
import ic2.core.command.CommandIc2c;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PlatformClient extends Platform
{
    private final Minecraft mc;
    
    public PlatformClient() {
        this.mc = Minecraft.getMinecraft();
    }
    
    @Override
    public boolean isRendering() {
        return Minecraft.getMinecraft().isCallingFromMinecraftThread();
    }
    
    @Override
    public void preInit() {
        ClientCommandHandler.instance.registerCommand((ICommand)new CommandIc2c());
        for (final BlockName name : BlockName.values) {
            if (!name.hasInstance()) {
                IC2.log.warn(LogCategory.Block, "The block " + name + " is not initialized.");
            }
            else {
                name.getInstance().registerModels(name);
            }
        }
        for (final BlockTileEntity block : TeBlockRegistry.getAllBlocks()) {
            if (!block.isIC2()) {
                block.registerModels(null);
            }
        }
        for (final ItemName name2 : ItemName.values) {
            if (!name2.hasInstance()) {
                IC2.log.warn(LogCategory.Item, "The item " + name2 + " is not initialized.");
            }
            else {
                name2.getInstance().registerModels(name2);
            }
        }
        for (final FluidName name3 : FluidName.values) {
            if (!name3.hasInstance()) {
                IC2.log.warn(LogCategory.Block, "The fluid " + name3 + " is not initialized.");
            }
            else {
                final Fluid provider = name3.getInstance();
                if (provider instanceof IFluidModelProvider) {
                    ((IFluidModelProvider)provider).registerModels(name3);
                }
            }
        }
        final Ic2ModelLoader loader = new Ic2ModelLoader();
        loader.register("models/block/cf/wall", new RenderBlockWall());
        loader.register("models/block/crop/crop", new CropModel());
        loader.register("models/block/wiring/cable", new CableModel());
        loader.register("models/block/transport/item_pipe", new PipeModel());
        loader.register("models/item/cell/fluid_cell", new FluidCellModel());
        loader.register("models/item/tool/electric/obscurator", new RenderObscurator());
        ModelLoaderRegistry.registerLoader((ICustomModelLoader)loader);
        ProfileManager.doTextureChanges();
        ClientRegistry.bindTileEntitySpecialRenderer((Class)TileEntityPersonalChest.class, (TileEntitySpecialRenderer)new TileEntityPersonalChestRenderer());
        final KineticGeneratorRenderer<TileEntityInventory> kineticRenderer = new KineticGeneratorRenderer<TileEntityInventory>();
        ClientRegistry.bindTileEntitySpecialRenderer((Class)TileEntityWindKineticGenerator.class, (TileEntitySpecialRenderer)kineticRenderer);
        ClientRegistry.bindTileEntitySpecialRenderer((Class)TileEntityWaterKineticGenerator.class, (TileEntitySpecialRenderer)kineticRenderer);
        ClientRegistry.bindTileEntitySpecialRenderer((Class)TileEntityWindGenerator.class, (TileEntitySpecialRenderer)kineticRenderer);
        ClientRegistry.bindTileEntitySpecialRenderer((Class)TileEntityWaterGenerator.class, (TileEntitySpecialRenderer)kineticRenderer);
        RenderingRegistry.registerEntityRenderingHandler((Class)EntityIC2Explosive.class, (IRenderFactory)new IRenderFactory<EntityIC2Explosive>() {
            public Render<EntityIC2Explosive> createRenderFor(final RenderManager manager) {
                return new RenderExplosiveBlock(manager);
            }
        });
        RenderingRegistry.registerEntityRenderingHandler((Class)EntityMiningLaser.class, (IRenderFactory)new IRenderFactory<EntityMiningLaser>() {
            public Render<EntityMiningLaser> createRenderFor(final RenderManager manager) {
                return new RenderCrossed(manager, new ResourceLocation("ic2", "textures/models/laser.png"));
            }
        });
        RenderingRegistry.registerEntityRenderingHandler((Class)EntityIC2Boat.class, (IRenderFactory)new IRenderFactory<EntityBoat>() {
            public Render<EntityBoat> createRenderFor(final RenderManager manager) {
                return (Render<EntityBoat>)new RenderIC2Boat(manager);
            }
        });
        RenderingRegistry.registerEntityRenderingHandler((Class)EntityDynamite.class, (IRenderFactory)new IRenderFactory<EntityDynamite>() {
            public Render<EntityDynamite> createRenderFor(final RenderManager manager) {
                return (Render<EntityDynamite>)new RenderSnowball(manager, ItemName.dynamite.getInstance(), PlatformClient.this.mc.getRenderItem());
            }
        });
        if (Util.inDev()) {
            RenderingRegistry.registerEntityRenderingHandler((Class)EntityParticle.class, manager -> new RenderBeam(manager));
        }
        GlTexture.init();
    }
    
    @Override
    public void displayError(String error, final Object... args) {
        if (!this.mc.isCallingFromMinecraftThread()) {
            super.displayError(error, args);
            return;
        }
        if (args.length > 0) {
            error = String.format(error, args);
        }
        error = "IndustrialCraft 2 Error\n\n" + error;
        String dialogError = error.replaceAll("([^\n]{80,}?) ", "$1\n");
        error = error.replace("\n", System.getProperty("line.separator"));
        dialogError = dialogError.replace("\n", System.getProperty("line.separator"));
        IC2.log.error(LogCategory.General, "%s", error);
        this.mc.setIngameNotInFocus();
        try {
            if (!Loader.instance().hasReachedState(LoaderState.AVAILABLE)) {
                SplashProgress.finish();
            }
            Display.destroy();
            final JFrame frame = new JFrame("IndustrialCraft 2 Error");
            frame.setUndecorated(true);
            frame.setVisible(true);
            frame.setLocationRelativeTo(null);
            JOptionPane.showMessageDialog(frame, dialogError, "IndustrialCraft 2 Error", 0);
        }
        catch (final Throwable t) {
            IC2.log.error(LogCategory.General, t, "Exception caught while showing an error.");
        }
        Util.exit(1);
    }
    
    @Override
    public EntityPlayer getPlayerInstance() {
        return (EntityPlayer)this.mc.player;
    }
    
    @Override
    public World getWorld(final int dimId) {
        if (this.isSimulating()) {
            return super.getWorld(dimId);
        }
        final World world = (World)this.mc.world;
        return (world.provider.getDimension() == dimId) ? world : null;
    }
    
    @Override
    public World getPlayerWorld() {
        return (World)this.mc.world;
    }
    
    @Override
    public void messagePlayer(final EntityPlayer player, final String message, final Object... args) {
        if (args.length > 0) {
            this.mc.ingameGUI.getChatGUI().printChatMessage((ITextComponent)new TextComponentTranslation(message, (Object[])this.getMessageComponents(args)));
        }
        else {
            this.mc.ingameGUI.getChatGUI().printChatMessage((ITextComponent)new TextComponentString(message));
        }
    }
    
    @Override
    public boolean launchGuiClient(final EntityPlayer player, final IHasGui inventory, final boolean isAdmin) {
        this.mc.displayGuiScreen(inventory.getGui(player, isAdmin));
        return true;
    }
    
    @Override
    public void profilerStartSection(final String section) {
        if (this.isRendering()) {
            this.mc.mcProfiler.startSection(section);
        }
        else {
            super.profilerStartSection(section);
        }
    }
    
    @Override
    public void profilerEndSection() {
        if (this.isRendering()) {
            this.mc.mcProfiler.endSection();
        }
        else {
            super.profilerEndSection();
        }
    }
    
    @Override
    public void profilerEndStartSection(final String section) {
        if (this.isRendering()) {
            this.mc.mcProfiler.endStartSection(section);
        }
        else {
            super.profilerEndStartSection(section);
        }
    }
    
    @Override
    public File getMinecraftDir() {
        return this.mc.mcDataDir;
    }
    
    @Override
    public void playSoundSp(final String sound, final float f, final float g) {
        IC2.audioManager.playOnce(this.getPlayerInstance(), PositionSpec.Hand, sound, true, IC2.audioManager.getDefaultVolume());
    }
    
    @Override
    public void onPostInit() {
        MinecraftForge.EVENT_BUS.register((Object)new GuiOverlayer(this.mc));
        new RpcHandler();
        new ElectricItemTooltipHandler();
        final Block leaves = BlockName.leaves.getInstance();
        this.mc.getBlockColors().registerBlockColorHandler((IBlockColor)new IBlockColor() {
            public int colorMultiplier(final IBlockState state, final IBlockAccess worldIn, final BlockPos pos, final int tintIndex) {
                return 6723908;
            }
        }, new Block[] { leaves });
        this.mc.getItemColors().registerItemColorHandler((IItemColor)new IItemColor() {
            public int colorMultiplier(final ItemStack stack, final int tintIndex) {
                return 6723908;
            }
        }, new Block[] { leaves });
        this.mc.getItemColors().registerItemColorHandler((IItemColor)new IItemColor() {
            public int colorMultiplier(final ItemStack stack, final int tintIndex) {
                return (tintIndex > 0) ? -1 : ((ItemArmor)stack.getItem()).getColor(stack);
            }
        }, new Item[] { ItemName.quantum_helmet.getInstance(), ItemName.quantum_chestplate.getInstance(), ItemName.quantum_leggings.getInstance(), ItemName.quantum_boots.getInstance() });
        this.mc.getItemColors().registerItemColorHandler((IItemColor)new IItemColor() {
            public int colorMultiplier(final ItemStack stack, final int tintIndex) {
                final PipeType type = ItemFluidPipe.getPipeType(stack);
                return (type.red & 0xFF) << 16 | (type.green & 0xFF) << 8 | (type.blue & 0xFF);
            }
        }, new Item[] { ItemName.pipe.getInstance() });
        this.mc.getBlockColors().registerBlockColorHandler((IBlockColor)new IBlockColor() {
            public int colorMultiplier(final IBlockState state, final IBlockAccess worldIn, final BlockPos pos, final int tintIndex) {
                final String variant = ((MetaTeBlock)state.getValue((IProperty)((BlockTileEntity)state.getBlock()).typeProperty)).teBlock.getName();
                if (!variant.endsWith("_storage_box")) {
                    return 16777215;
                }
                final String s = variant;
                switch (s) {
                    case "wooden_storage_box": {
                        return 10454093;
                    }
                    case "iron_storage_box": {
                        return 13158600;
                    }
                    case "bronze_storage_box": {
                        return 16744448;
                    }
                    case "steel_storage_box": {
                        return 8421504;
                    }
                    default: {
                        return 16777215;
                    }
                }
            }
        }, new Block[] { BlockName.te.getInstance() });
        this.mc.getItemColors().registerItemColorHandler((IItemColor)new IItemColor() {
            public int colorMultiplier(final ItemStack stack, final int tintIndex) {
                final String variant = Objects.requireNonNull(BlockName.te.getVariant(stack));
                if (!variant.endsWith("_storage_box")) {
                    return 16777215;
                }
                final String s = variant;
                switch (s) {
                    case "wooden_storage_box": {
                        return 10454093;
                    }
                    case "iron_storage_box": {
                        return 13158600;
                    }
                    case "bronze_storage_box": {
                        return 16744448;
                    }
                    case "steel_storage_box": {
                        return 8421504;
                    }
                    default: {
                        return 16777215;
                    }
                }
            }
        }, new Block[] { BlockName.te.getInstance() });
        this.mc.getItemColors().registerItemColorHandler((stack, tintIndex) -> {
            final PumpCoverType type = ((ItemPumpCover)stack.getItem()).getType(stack);
            return (tintIndex == 1) ? type.color : 16777215;
        }, new Item[] { ItemName.cover.getInstance() });
    }
    
    @Override
    public void requestTick(final boolean simulating, final Runnable runnable) {
        if (simulating) {
            super.requestTick(simulating, runnable);
        }
        else {
            this.mc.addScheduledTask(runnable);
        }
    }
    
    @Override
    public int getColorMultiplier(final IBlockState state, final IBlockAccess world, final BlockPos pos, final int tint) {
        return this.mc.getBlockColors().colorMultiplier(state, world, pos, tint);
    }
}
