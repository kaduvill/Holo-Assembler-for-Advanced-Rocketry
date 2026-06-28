package com.kaduvill.holoassemblerar.item;

import com.kaduvill.holoassemblerar.compat.AE2Compat;
import com.kaduvill.holoassemblerar.compat.ProjectECompat;
import com.kaduvill.holoassemblerar.config.HoloAssemblerConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zmaster587.advancedRocketry.api.AdvancedRocketryBlocks;
import zmaster587.advancedRocketry.tile.TileRocketAssemblingMachine;
import zmaster587.advancedRocketry.tile.TileStationAssembler;
import zmaster587.advancedRocketry.tile.TileUnmannedVehicleAssembler;
import zmaster587.libVulpes.LibVulpes;
import zmaster587.libVulpes.api.LibVulpesBlocks;
import zmaster587.libVulpes.block.BlockMeta;
import zmaster587.libVulpes.block.RotatableBlock;
import zmaster587.libVulpes.block.multiblock.BlockMultiblockMachine;
import zmaster587.libVulpes.inventory.GuiHandler;
import zmaster587.libVulpes.inventory.TextureResources;
import zmaster587.libVulpes.inventory.modules.IButtonInventory;
import zmaster587.libVulpes.inventory.modules.IGuiCallback;
import zmaster587.libVulpes.inventory.modules.IModularInventory;
import zmaster587.libVulpes.inventory.modules.ModuleBase;
import zmaster587.libVulpes.inventory.modules.ModuleButton;
import zmaster587.libVulpes.inventory.modules.ModuleContainerPan;
import zmaster587.libVulpes.inventory.modules.ModuleText;
import zmaster587.libVulpes.network.INetworkItem;
import zmaster587.libVulpes.network.PacketHandler;
import zmaster587.libVulpes.network.PacketItemModifcation;
import zmaster587.libVulpes.tile.TileSchematic;
import zmaster587.libVulpes.tile.multiblock.TileMultiBlock;
import zmaster587.libVulpes.tile.multiblock.TilePlaceholder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;

public class ItemHoloAssembler extends Item implements IModularInventory, IButtonInventory, IGuiCallback, INetworkItem {

    private static final String NBT_USE_INVENTORY = "UseInventory";
    private static final String NBT_USE_EMC = "UseEmc";
    private static final String NBT_USE_ME = "UseMe";
    private static final String NBT_DEBUG = "Debug";

    private static final byte PACKET_SETTINGS = 0;
    private static final byte PACKET_ASSEMBLER_GUI = 1;

    private static final String NBT_ASSEMBLER_MODE = "AssemblerMode";
    private static final String NBT_ASSEMBLER_X = "AssemblerX";
    private static final String NBT_ASSEMBLER_Y = "AssemblerY";
    private static final String NBT_ASSEMBLER_Z = "AssemblerZ";
    private static final String NBT_ASSEMBLER_DIM = "AssemblerDim";
    private static final String NBT_LEFT_OFFSET = "LeftOffset";
    private static final String NBT_RIGHT_OFFSET = "RightOffset";
    private static final String NBT_LENGTH = "AssemblerLength";
    private static final String NBT_HEIGHT = "AssemblerHeight";
    private static final String NBT_PREVIEW_ACTIVE = "AssemblerPreviewActive";
    private static final String NBT_BUILD_REQUESTED = "AssemblerBuildRequested";
    private static final String NBT_TOWER_SIDE = "TowerSide";

    private static final int TOWER_SIDE_OPPOSITE = 0;
    private static final int TOWER_SIDE_LEFT = 1;
    private static final int TOWER_SIDE_RIGHT = 2;

    private static final int BTN_TOWER_SIDE = 108;
    private static final int MODE_NONE = 0;
    private static final int MODE_ROCKET = 1;
    private static final int MODE_STATION = 2;
    private static final int MODE_UNMANNED = 3;

    private static final int BTN_LEFT_MINUS = 100;
    private static final int BTN_LEFT_PLUS = 101;
    private static final int BTN_RIGHT_MINUS = 102;
    private static final int BTN_RIGHT_PLUS = 103;
    private static final int BTN_LENGTH_MINUS = 104;
    private static final int BTN_LENGTH_PLUS = 105;
    private static final int BTN_HEIGHT_MINUS = 106;
    private static final int BTN_HEIGHT_PLUS = 107;
    private static final int BTN_PREVIEW = 120;
    private static final int BTN_BUILD = 121;

    private static final String LANG_GUI_SOURCE_INVENTORY = "gui.holoassemblerar.source.inventory";
    private static final String LANG_GUI_SOURCE_EMC = "gui.holoassemblerar.source.emc";
    private static final String LANG_GUI_SOURCE_ME = "gui.holoassemblerar.source.me";
    private static final String LANG_GUI_SOURCE_DEBUG = "gui.holoassemblerar.source.debug";
    private static final String LANG_GUI_BUTTON_ON = "gui.holoassemblerar.button.on";
    private static final String LANG_GUI_BUTTON_OFF = "gui.holoassemblerar.button.off";
    private static final String LANG_GUI_BUTTON_PREVIEW = "gui.holoassemblerar.button.preview";
    private static final String LANG_GUI_BUTTON_BUILD = "gui.holoassemblerar.button.build";
    private static final String LANG_GUI_BUILDER_WIDTH = "gui.holoassemblerar.builder.width";
    private static final String LANG_GUI_BUILDER_LEFT_OFFSET = "gui.holoassemblerar.builder.left_offset";
    private static final String LANG_GUI_BUILDER_RIGHT_OFFSET = "gui.holoassemblerar.builder.right_offset";
    private static final String LANG_GUI_BUILDER_PAD_LENGTH = "gui.holoassemblerar.builder.pad_length";
    private static final String LANG_GUI_BUILDER_FORWARD_SPAN = "gui.holoassemblerar.builder.forward_span";
    private static final String LANG_GUI_BUILDER_TOWER_HEIGHT = "gui.holoassemblerar.builder.tower_height";
    private static final String LANG_GUI_BUILDER_FRAME_HEIGHT = "gui.holoassemblerar.builder.frame_height";
    private static final String LANG_GUI_BUILDER_TOWER_SIDE = "gui.holoassemblerar.builder.tower_side";
    private static final String LANG_GUI_TOWER_SIDE_OPPOSITE = "gui.holoassemblerar.tower_side.opposite";
    private static final String LANG_GUI_TOWER_SIDE_LEFT = "gui.holoassemblerar.tower_side.left";
    private static final String LANG_GUI_TOWER_SIDE_RIGHT = "gui.holoassemblerar.tower_side.right";
    private static final String LANG_GUI_ACTIVE_SOURCES_NONE = "gui.holoassemblerar.active_sources.none";

    private static final String LANG_MSG_NO_STRUCTURE = "message.holoassemblerar.no_structure";
    private static final String LANG_MSG_NO_CONTROLLER = "message.holoassemblerar.no_controller_marker";
    private static final String LANG_MSG_STRUCTURE_COMPLETE = "message.holoassemblerar.structure_complete";
    private static final String LANG_MSG_STRUCTURE_COMPLETE_DEBUG = "message.holoassemblerar.structure_complete.debug";
    private static final String LANG_MSG_ASSEMBLY_COMPLETE = "message.holoassemblerar.assembly_complete";
    private static final String LANG_MSG_ASSEMBLY_COMPLETE_DEBUG = "message.holoassemblerar.assembly_complete.debug";
    private static final String LANG_MSG_COULD_NOT_PLACE = "message.holoassemblerar.could_not_place";
    private static final String LANG_MSG_COULD_NOT_PLACE_DEBUG = "message.holoassemblerar.could_not_place.debug";
    private static final String LANG_MSG_UNKNOWN_BLOCK = "message.holoassemblerar.unknown_block";
    private static final String LANG_MSG_ASSEMBLER_WRONG_DIMENSION = "message.holoassemblerar.assembler_wrong_dimension";
    private static final String LANG_MSG_ASSEMBLER_TOO_FAR = "message.holoassemblerar.assembler_too_far";
    private static final String LANG_MSG_ASSEMBLER_NOT_LOADED = "message.holoassemblerar.assembler_not_loaded";
    private static final String LANG_MSG_ASSEMBLER_INVALID = "message.holoassemblerar.assembler_invalid";
    private static final String LANG_MSG_ASSEMBLER_INVALID_PLAN = "message.holoassemblerar.assembler_invalid_plan";
    private static final String LANG_MSG_ASSEMBLER_BLOCKED = "message.holoassemblerar.assembler_blocked";
    private static final String LANG_MSG_ASSEMBLER_BUILD_COMPLETE = "message.holoassemblerar.assembler_build_complete";
    private static final String LANG_MSG_ASSEMBLER_BUILD_INCOMPLETE = "message.holoassemblerar.assembler_build_incomplete";

    private static final int BTN_INVENTORY = 0;
    private static final int BTN_EMC = 1;
    private static final int BTN_ME = 2;
    private static final int BTN_DEBUG = 3;

    private static final int COLOR_ON = 0xFF55FF55;
    private static final int COLOR_OFF = 0xFFFF5555;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int MUTED_TEXT_COLOR = 0xDCDCDC;

    private static final int PANEL_X = 6;
    private static final int PANEL_Y = 17;
    private static final int PANEL_WIDTH = 158;
    private static final int PANEL_HEIGHT = 108;

    private static final int LABEL_X = 8;
    private static final int BUTTON_X = 72;
    private static final int FIRST_ROW_Y = 4;
    private static final int ROW_SPACING = 22;

    private static final int BUTTON_BG_NORMAL = 0xFFFFFFFF;
    private static final int BUTTON_BG_SELECTED = 0xFF444444;

    private static final String NBT_PREVIEW_EXPIRE = "AssemblerPreviewExpire";
    private static final int PREVIEW_TIMEOUT_TICKS = 20 * 30;
    private static final double PREVIEW_MAX_DISTANCE_SQ = 64.0D * 64.0D;
    private static final Map<UUID, ActivePreview> ACTIVE_PREVIEWS = new HashMap<>();

    @SideOnly(Side.CLIENT)
    private static List<AxisAlignedBB> createRocketStationPreviewBoxes(BlockPos assemblerPos,
                                                                       EnumFacing forward,
                                                                       int leftOffset,
                                                                       int rightOffset,
                                                                       int length,
                                                                       int towerHeight,
                                                                       int towerSide) {
        List<AxisAlignedBB> boxes = new LinkedList<>();

        EnumFacing left = forward.rotateYCCW();
        EnumFacing right = forward.rotateY();

        BlockPos origin = assemblerPos.offset(forward).down();

        // Whole pad footprint as one big box.
        BlockPos padStart = offsetSide(origin, left, right, -leftOffset);
        BlockPos padEnd = offsetSide(origin.offset(forward, length - 1), left, right, rightOffset);
        boxes.add(makeBlockBox(padStart, padEnd));

        // Tower as one column box.
        int towerForwardIndex = (length - 1) / 2;
        int towerSideOffset = getCenteredSideOffset(leftOffset, rightOffset);

        BlockPos towerBase;

        if (towerSide == TOWER_SIDE_OPPOSITE) {
            towerBase = offsetSide(origin.offset(forward, length), left, right, towerSideOffset);
        } else if (towerSide == TOWER_SIDE_LEFT) {
            towerBase = offsetSide(origin.offset(forward, towerForwardIndex), left, right, -leftOffset - 1);
        } else if (towerSide == TOWER_SIDE_RIGHT) {
            towerBase = offsetSide(origin.offset(forward, towerForwardIndex), left, right, rightOffset + 1);
        } else {
            towerBase = offsetSide(origin.offset(forward, length), left, right, towerSideOffset);
        }

        boxes.add(makeBlockBox(towerBase, towerBase.up(towerHeight - 1)));

        return boxes;
    }

    @SideOnly(Side.CLIENT)
    private static List<AxisAlignedBB> createUnmannedPreviewBoxes(BlockPos assemblerPos,
                                                                  EnumFacing forward,
                                                                  int leftOffset,
                                                                  int rightOffset,
                                                                  int forwardSpan,
                                                                  int height) {
        List<AxisAlignedBB> boxes = new LinkedList<>();

        EnumFacing left = forward.rotateYCCW();
        EnumFacing right = forward.rotateY();

        // Width line.
        BlockPos widthStart = offsetSide(assemblerPos, left, right, -leftOffset);
        BlockPos widthEnd = offsetSide(assemblerPos, left, right, rightOffset);
        boxes.add(makeBlockBox(widthStart, widthEnd));

        // Vertical column above assembler.
        boxes.add(makeBlockBox(assemblerPos.up(1), assemblerPos.up(height)));

        // Top forward run.
        boxes.add(makeBlockBox(assemblerPos.up(height), assemblerPos.up(height).offset(forward, forwardSpan - 1)));

        return boxes;
    }

    @SideOnly(Side.CLIENT)
    private static AxisAlignedBB makeBlockBox(BlockPos a, BlockPos b) {
        int minX = Math.min(a.getX(), b.getX());
        int minY = Math.min(a.getY(), b.getY());
        int minZ = Math.min(a.getZ(), b.getZ());
        int maxX = Math.max(a.getX(), b.getX());
        int maxY = Math.max(a.getY(), b.getY());
        int maxZ = Math.max(a.getZ(), b.getZ());

        return new AxisAlignedBB(
                minX,
                minY,
                minZ,
                maxX + 1,
                maxY + 1,
                maxZ + 1
        );
    }

    private static class ActivePreview {
        final int dimension;
        final BlockPos assemblerPos;
        final List<BlockPos> phantomPositions;
        final long expireWorldTime;

        ActivePreview(int dimension,
                      BlockPos assemblerPos,
                      List<BlockPos> phantomPositions,
                      long expireWorldTime) {
            this.dimension = dimension;
            this.assemblerPos = assemblerPos;
            this.phantomPositions = phantomPositions;
            this.expireWorldTime = expireWorldTime;
        }
    }

    private static class GeneratedPlan {
        final List<GeneratedCell> cells = new LinkedList<>();
        final java.util.Set<BlockPos> usedPositions = new java.util.LinkedHashSet<>();
    }

    private static class GeneratedCell {
        final BlockPos pos;
        final List<BlockMeta> allowed;

        GeneratedCell(BlockPos pos, BlockMeta blockMeta) {
            this.pos = pos;
            this.allowed = new LinkedList<>();
            this.allowed.add(blockMeta);
        }
    }

    private static GeneratedPlan createGeneratedPlan(World world, BlockPos assemblerPos, int mode, ItemStack stack) {
        int leftOffset = getLeftOffset(stack);
        int rightOffset = getRightOffset(stack);
        int length = getAssemblerLength(stack);
        int height = getAssemblerHeight(stack);

        EnumFacing forward = RotatableBlock.getFront(world.getBlockState(assemblerPos)).getOpposite();

        if (forward.getAxis() == EnumFacing.Axis.Y) {
            return null;
        }

        if (mode == MODE_ROCKET || mode == MODE_STATION) {
            return createRocketStationPadPlan(assemblerPos, forward, leftOffset, rightOffset, length, height, getTowerSide(stack));
        }

        if (mode == MODE_UNMANNED) {
            return createUnmannedFramePlan(assemblerPos, forward, leftOffset, rightOffset, length, height);
        }

        return null;
    }

    private static GeneratedPlan createRocketStationPadPlan(
            BlockPos assemblerPos,
            EnumFacing forward,
            int leftOffset,
            int rightOffset,
            int length,
            int towerHeight,
            int towerSide
    ) {
        GeneratedPlan plan = new GeneratedPlan();

        EnumFacing left = forward.rotateYCCW();
        EnumFacing right = forward.rotateY();

        BlockMeta launchPad = new BlockMeta(AdvancedRocketryBlocks.blockLaunchpad, 0);
        BlockMeta tower = new BlockMeta(AdvancedRocketryBlocks.blockStructureTower, 0);

        BlockPos origin = assemblerPos.offset(forward).down();

        // Pad rectangle.
        for (int f = 0; f < length; f++) {
            for (int s = -leftOffset; s <= rightOffset; s++) {
                BlockPos pos = offsetSide(origin.offset(forward, f), left, right, s);
                addGeneratedCell(plan, assemblerPos, pos, launchPad);
            }
        }

        // Rocket/Station tower:
        // Exactly one vertical tower column.
        // Starts at launchpad Y.
        // Never generates tower on assembler side.
        int towerForwardIndex = (length - 1) / 2;
        int towerSideOffset = getCenteredSideOffset(leftOffset, rightOffset);

        BlockPos towerBase;

        if (towerSide == TOWER_SIDE_OPPOSITE) {
            // Far side of the pad, centered across pad width.
            towerBase = offsetSide(origin.offset(forward, length), left, right, towerSideOffset);
        } else if (towerSide == TOWER_SIDE_LEFT) {
            // Left side of the pad, centered along pad length.
            towerBase = offsetSide(origin.offset(forward, towerForwardIndex), left, right, -leftOffset - 1);
        } else if (towerSide == TOWER_SIDE_RIGHT) {
            // Right side of the pad, centered along pad length.
            towerBase = offsetSide(origin.offset(forward, towerForwardIndex), left, right, rightOffset + 1);
        } else {
            // Safe fallback.
            towerBase = offsetSide(origin.offset(forward, length), left, right, towerSideOffset);
        }

        for (int y = 0; y < towerHeight; y++) {
            addGeneratedCell(plan, assemblerPos, towerBase.up(y), tower);
        }

        return plan;
    }

    private static GeneratedPlan createUnmannedFramePlan(
            BlockPos assemblerPos,
            EnumFacing forward,
            int leftOffset,
            int rightOffset,
            int forwardSpan,
            int height
    ) {
        GeneratedPlan plan = new GeneratedPlan();

        EnumFacing left = forward.rotateYCCW();
        EnumFacing right = forward.rotateY();

        BlockMeta tower = new BlockMeta(AdvancedRocketryBlocks.blockStructureTower, 0);

        // Side width line at assembler Y. Do not place on the assembler itself.
        for (int s = -leftOffset; s <= rightOffset; s++) {
            if (s == 0) {
                continue;
            }
            addGeneratedCell(plan, assemblerPos, offsetSide(assemblerPos, left, right, s), tower);
        }

        // Vertical tower above assembler. AR scans from getPos().add(0, 1, 0).
        for (int y = 1; y <= height; y++) {
            addGeneratedCell(plan, assemblerPos, assemblerPos.up(y), tower);
        }

        // Top forward run. AR scans from pos2.add(0, yMax, 0) in the forward direction.
        for (int f = 0; f < forwardSpan; f++) {
            addGeneratedCell(plan, assemblerPos, assemblerPos.up(height).offset(forward, f), tower);
        }

        return plan;
    }

    private static BlockPos offsetSide(BlockPos origin, EnumFacing left, EnumFacing right, int sideOffset) {
        if (sideOffset < 0) {
            return origin.offset(left, -sideOffset);
        }
        if (sideOffset > 0) {
            return origin.offset(right, sideOffset);
        }
        return origin;
    }

    private static int getCenteredSideOffset(int leftOffset, int rightOffset) {
        int width = leftOffset + 1 + rightOffset;

        // Odd width:
        //   width 3 -> index 1
        //   width 5 -> index 2
        //
        // Even width:
        //   width 4 -> index 1, left-of-center
        //   width 6 -> index 2, left-of-center
        int middleIndex = (width - 1) / 2;

        return -leftOffset + middleIndex;
    }

    public ItemHoloAssembler() {
        setMaxStackSize(1);
        setUnlocalizedName("holoassemblerar.holo_assembler");
    }

    @Override
    public List<ModuleBase> getModules(int ID, EntityPlayer player) {
        List<ModuleBase> modules = new LinkedList<>();
        List<ModuleBase> panelModules = new LinkedList<>();

        ItemStack stack = player == null ? ItemStack.EMPTY : player.getHeldItem(EnumHand.MAIN_HAND);

        if (!stack.isEmpty() && stack.getItem() == this && getAssemblerMode(stack) != MODE_NONE) {
            return getAssemblerBuilderModules(player, stack);
        }
        boolean isClient = player != null && player.world.isRemote;

        int y = FIRST_ROW_Y;

        panelModules.add(new ModuleText(LABEL_X, y + 4, tr(LANG_GUI_SOURCE_INVENTORY), TEXT_COLOR));
        panelModules.add(makeToggleButton(BTN_INVENTORY, BUTTON_X, y, useInventory(stack), isClient));
        y += ROW_SPACING;

        if (isEmcAvailable()) {
            panelModules.add(new ModuleText(LABEL_X, y + 4, tr(LANG_GUI_SOURCE_EMC), TEXT_COLOR));
            panelModules.add(makeToggleButton(BTN_EMC, BUTTON_X, y, useEmc(stack), isClient));
            y += ROW_SPACING;
        }

        if (isMeAvailable()) {
            panelModules.add(new ModuleText(LABEL_X, y + 4, tr(LANG_GUI_SOURCE_ME), TEXT_COLOR));
            panelModules.add(makeToggleButton(BTN_ME, BUTTON_X, y, useMe(stack), isClient));
            y += ROW_SPACING;
        }

        if (HoloAssemblerConfig.enableDebugMode) {
            panelModules.add(new ModuleText(LABEL_X, y + 4, tr(LANG_GUI_SOURCE_DEBUG), TEXT_COLOR));
            panelModules.add(makeToggleButton(BTN_DEBUG, BUTTON_X, y, isDebugEnabled(stack), isClient));
        }
        modules.add(makeStarryPanel(panelModules));
        return modules;
    }

    private List getAssemblerBuilderModules(EntityPlayer player, ItemStack stack) {
        List modules = new LinkedList<>();
        List panelModules = new LinkedList<>();

        int mode = getAssemblerMode(stack);

        clampAssemblerSettings(stack);

        int y = 2;

        // Row 1: Width summary.
        panelModules.add(new ModuleAssemblerDynamicText(8, y + 4, ModuleAssemblerDynamicText.TYPE_WIDTH));
        y += 15;

        // Left offset.
        panelModules.add(new ModuleAssemblerDynamicText(8, y + 4, ModuleAssemblerDynamicText.TYPE_LEFT_OFFSET));
        panelModules.add(makeSmallButton(BTN_LEFT_MINUS, 96, y, "-"));
        panelModules.add(makeSmallButton(BTN_LEFT_PLUS, 124, y, "+"));
        y += 17;

        // Right offset.
        panelModules.add(new ModuleAssemblerDynamicText(8, y + 4, ModuleAssemblerDynamicText.TYPE_RIGHT_OFFSET));
        panelModules.add(makeSmallButton(BTN_RIGHT_MINUS, 96, y, "-"));
        panelModules.add(makeSmallButton(BTN_RIGHT_PLUS, 124, y, "+"));
        y += 17;

        // Length.
        panelModules.add(new ModuleAssemblerDynamicText(8, y + 4, ModuleAssemblerDynamicText.TYPE_LENGTH));
        panelModules.add(makeSmallButton(BTN_LENGTH_MINUS, 96, y, "-"));
        panelModules.add(makeSmallButton(BTN_LENGTH_PLUS, 124, y, "+"));
        y += 17;

        // Height.
        panelModules.add(new ModuleAssemblerDynamicText(8, y + 4, ModuleAssemblerDynamicText.TYPE_HEIGHT));
        panelModules.add(makeSmallButton(BTN_HEIGHT_MINUS, 96, y, "-"));
        panelModules.add(makeSmallButton(BTN_HEIGHT_PLUS, 124, y, "+"));
        y += 17;

        // Row 6: Tower side, Rocket/Station only.
        if (mode == MODE_ROCKET || mode == MODE_STATION) {
            panelModules.add(new ModuleText(8, y + 4, tr(LANG_GUI_BUILDER_TOWER_SIDE), TEXT_COLOR));
            panelModules.add(new ModuleTowerSideButton(82, y, BTN_TOWER_SIDE, this));
            y += 19;
        }

        // Bottom row: Preview / Build.
        panelModules.add(new ModuleButton(8, y, BTN_PREVIEW, tr(LANG_GUI_BUTTON_PREVIEW), this, TextureResources.buttonScan));
        panelModules.add(new ModuleButton(82, y, BTN_BUILD, tr(LANG_GUI_BUTTON_BUILD), this, TextureResources.buttonBuild));
        modules.add(makeStarryPanel(panelModules));

        return modules;
    }

    private ModuleStaticStarryPanel makeStarryPanel(List<ModuleBase> panelModules) {
        return new ModuleStaticStarryPanel(
                PANEL_X, PANEL_Y, panelModules,
                new LinkedList<>(), TextureResources.starryBG,
                PANEL_WIDTH, PANEL_HEIGHT,
                0, 500
        );
    }

    private ModuleButton makeSmallButton(int buttonId, int x, int y, String text) {
        ModuleButton button = new ModuleButton(x, y, buttonId, text, this, TextureResources.buttonBuild, 20, 16
        );
        button.setColor(COLOR_ON);
        return button;
    }

    private static void addGeneratedCell(GeneratedPlan plan,
                                         BlockPos assemblerPos,
                                         BlockPos pos,
                                         BlockMeta blockMeta) {
        if (pos.equals(assemblerPos)) {
            return;
        }

        if (!plan.usedPositions.add(pos)) {
            return;
        }

        plan.cells.add(new GeneratedCell(pos, blockMeta));
    }

    private ModuleButton makeToggleButton(int buttonId,
                                          int x,
                                          int y,
                                          boolean enabled,
                                          boolean isClient) {
        String text = getButtonText(enabled);

        if (isClient) {
            ModuleButton button = new ModuleHoloAssemblerToggleButton(
                    x,
                    y,
                    buttonId,
                    text,
                    this
            );
            button.setColor(enabled ? COLOR_ON : COLOR_OFF);
            button.setBGColor(enabled ? BUTTON_BG_SELECTED : BUTTON_BG_NORMAL);

            return button;
        }

        return new ModuleButton(
                x,
                y,
                buttonId,
                text,
                this,
                TextureResources.buttonBuild
        );
    }

    private static String getButtonText(boolean enabled) {
        return tr(enabled ? LANG_GUI_BUTTON_ON : LANG_GUI_BUTTON_OFF);
    }

    private static String tr(String key) {
        return LibVulpes.proxy.getLocalizedString(key);
    }

    private static class ModuleTowerSideButton extends ModuleButton {

        public ModuleTowerSideButton(int offsetX,
                                     int offsetY,
                                     int buttonId,
                                     IButtonInventory tile) {
            super(offsetX, offsetY, buttonId, "", tile, TextureResources.buttonBuild);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void renderForeground(int guiOffsetX,
                                     int guiOffsetY,
                                     int mouseX,
                                     int mouseY,
                                     float zLevel,
                                     GuiContainer gui,
                                     FontRenderer font) {
            EntityPlayer player = Minecraft.getMinecraft().player;

            if (player != null) {
                ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);

                if (!stack.isEmpty() && stack.getItem() instanceof ItemHoloAssembler) {
                    String text = getTowerSideLabel(stack);
                    setText(text);

                    if (button != null) {
                        button.displayString = text;
                    }
                }
            }

            super.renderForeground(guiOffsetX, guiOffsetY, mouseX, mouseY, zLevel, gui, font);
        }
    }

    private static class ModuleAssemblerDynamicText extends ModuleText {

        private final int textType;

        private static final int TYPE_WIDTH = 0;
        private static final int TYPE_LEFT_OFFSET = 1;
        private static final int TYPE_RIGHT_OFFSET = 2;
        private static final int TYPE_LENGTH = 3;
        private static final int TYPE_HEIGHT = 4;

        public ModuleAssemblerDynamicText(int offsetX, int offsetY, int textType) {
            super(offsetX, offsetY, "", textType == TYPE_LEFT_OFFSET || textType == TYPE_RIGHT_OFFSET ? MUTED_TEXT_COLOR : TEXT_COLOR);
            this.textType = textType;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void renderForeground(int guiOffsetX,
                                     int guiOffsetY,
                                     int mouseX,
                                     int mouseY,
                                     float zLevel,
                                     GuiContainer gui,
                                     FontRenderer font) {
            EntityPlayer player = Minecraft.getMinecraft().player;

            if (player == null) {
                return;
            }

            ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);

            if (stack.isEmpty() || !(stack.getItem() instanceof ItemHoloAssembler)) {
                return;
            }

            int mode = getAssemblerMode(stack);
            int left = getLeftOffset(stack);
            int right = getRightOffset(stack);
            int total = left + 1 + right;

            switch (textType) {
                case TYPE_WIDTH:
                    setText(I18n.format(LANG_GUI_BUILDER_WIDTH, total));
                    break;

                case TYPE_LEFT_OFFSET:
                    setText(I18n.format(LANG_GUI_BUILDER_LEFT_OFFSET, left));
                    break;

                case TYPE_RIGHT_OFFSET:
                    setText(I18n.format(LANG_GUI_BUILDER_RIGHT_OFFSET, right));
                    break;

                case TYPE_LENGTH:
                    setText(I18n.format(
                            mode == MODE_UNMANNED ? LANG_GUI_BUILDER_FORWARD_SPAN : LANG_GUI_BUILDER_PAD_LENGTH,
                            getAssemblerLength(stack)
                    ));
                    break;

                case TYPE_HEIGHT:
                    setText(I18n.format(
                            mode == MODE_UNMANNED ? LANG_GUI_BUILDER_FRAME_HEIGHT : LANG_GUI_BUILDER_TOWER_HEIGHT,
                            getAssemblerHeight(stack)
                    ));
                    break;
            }

            super.renderForeground(guiOffsetX, guiOffsetY, mouseX, mouseY, zLevel, gui, font);
        }
    }

    private static class ModuleHoloAssemblerToggleButton extends ModuleButton {

        public ModuleHoloAssemblerToggleButton(int offsetX,
                                               int offsetY,
                                               int buttonId,
                                               String label,
                                               IButtonInventory tile) {
            super(offsetX, offsetY, buttonId, label, tile, TextureResources.buttonBuild);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void renderForeground(int guiOffsetX,
                                     int guiOffsetY,
                                     int mouseX,
                                     int mouseY,
                                     float zLevel,
                                     GuiContainer gui,
                                     FontRenderer font) {
            if (button != null && !button.visible) {
                return;
            }

            EntityPlayer player = Minecraft.getMinecraft().player;
            if (player == null) {
                return;
            }

            ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
            boolean enabled = getToggleState(stack);

            String text = getButtonText(enabled);
            setText(text);

            if (button != null) {
                button.displayString = text;
            }

            setColor(enabled ? COLOR_ON : COLOR_OFF);
            setBGColor(enabled ? BUTTON_BG_SELECTED : BUTTON_BG_NORMAL);

            super.renderForeground(guiOffsetX, guiOffsetY, mouseX, mouseY, zLevel, gui, font);
        }

        private boolean getToggleState(ItemStack stack) {
            if (stack.isEmpty() || !(stack.getItem() instanceof ItemHoloAssembler)) {
                return false;
            }

            switch (buttonId) {
                case BTN_INVENTORY:
                    return useInventory(stack);

                case BTN_EMC:
                    return useEmc(stack) && isEmcAvailable();

                case BTN_ME:
                    return useMe(stack) && isMeAvailable();

                case BTN_DEBUG:
                    return isDebugEnabled(stack) && HoloAssemblerConfig.enableDebugMode;

                default:
                    return false;
            }
        }
    }

    @Override
    public String getModularInventoryName() {
        return "item.holoassemblerar.holo_assembler.name";
    }

    @Override
    public boolean canInteractWithContainer(EntityPlayer player) {
        return player != null
                && !player.isDead
                && !player.getHeldItem(EnumHand.MAIN_HAND).isEmpty()
                && player.getHeldItem(EnumHand.MAIN_HAND).getItem() == this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onInventoryButtonPressed(int buttonId) {
        EntityPlayer player = Minecraft.getMinecraft().player;

        if (player == null) {
            return;
        }

        ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
        int assemblerMode = getAssemblerMode(stack);
        if (assemblerMode != MODE_NONE && handleAssemblerGuiButton(stack, buttonId)) {
            PacketHandler.sendToServer(new PacketItemModifcation(this, player, PACKET_ASSEMBLER_GUI));
            return;
        }
        if (stack.isEmpty() || stack.getItem() != this) {
            return;
        }

        switch (buttonId) {

            case BTN_INVENTORY:
                setUseInventory(stack, !useInventory(stack));
                break;

            case BTN_EMC:
                if (isEmcAvailable()) {
                    setUseEmc(stack, !useEmc(stack));
                }
                break;

            case BTN_ME:
                if (isMeAvailable()) {
                    setUseMe(stack, !useMe(stack));
                }
                break;

            case BTN_DEBUG:
                if (HoloAssemblerConfig.enableDebugMode) {
                    setDebugEnabled(stack, !isDebugEnabled(stack));
                }
                break;

            default:
                return;
        }

        PacketHandler.sendToServer(new PacketItemModifcation(this, player, PACKET_SETTINGS));
    }

    @SideOnly(Side.CLIENT)
    private boolean handleAssemblerGuiButton(ItemStack stack, int buttonId) {
        NBTTagCompound tag = getOrCreateTag(stack);
        tag.setBoolean(NBT_BUILD_REQUESTED, false);
        if (buttonId != BTN_PREVIEW) {
            tag.setBoolean(NBT_PREVIEW_ACTIVE, false);
            tag.removeTag(NBT_PREVIEW_EXPIRE);
        }

        switch (buttonId) {
            case BTN_LEFT_MINUS:
                setLeftOffset(stack, getLeftOffset(stack) - 1);
                break;

            case BTN_LEFT_PLUS:
                setLeftOffset(stack, getLeftOffset(stack) + 1);
                break;

            case BTN_RIGHT_MINUS:
                setRightOffset(stack, getRightOffset(stack) - 1);
                break;

            case BTN_RIGHT_PLUS:
                setRightOffset(stack, getRightOffset(stack) + 1);
                break;

            case BTN_LENGTH_MINUS:
                setAssemblerLength(stack, getAssemblerLength(stack) - 1);
                break;

            case BTN_LENGTH_PLUS:
                setAssemblerLength(stack, getAssemblerLength(stack) + 1);
                break;

            case BTN_HEIGHT_MINUS:
                setAssemblerHeight(stack, getAssemblerHeight(stack) - 1);
                break;

            case BTN_HEIGHT_PLUS:
                setAssemblerHeight(stack, getAssemblerHeight(stack) + 1);
                break;

            case BTN_PREVIEW:
                tag.setBoolean(NBT_PREVIEW_ACTIVE, true);
                if (Minecraft.getMinecraft().world != null) {
                    tag.setLong(
                            NBT_PREVIEW_EXPIRE,
                            Minecraft.getMinecraft().world.getTotalWorldTime() + PREVIEW_TIMEOUT_TICKS
                    );
                }
                break;

            case BTN_BUILD:
                tag.setBoolean(NBT_BUILD_REQUESTED, true);
                break;

            case BTN_TOWER_SIDE:
                cycleTowerSide(stack);
                break;

            default:
                return false;
        }

        clampAssemblerSettings(stack);
        return true;
    }

    @Override
    public void onModuleUpdated(ModuleBase module) {
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player,
                                      World world,
                                      BlockPos controllerPos,
                                      EnumHand hand,
                                      EnumFacing facing,
                                      float hitX,
                                      float hitY,
                                      float hitZ) {
        ItemStack heldStack = player.getHeldItem(hand);

        if (player.isSneaking()) {
            clearPreview(player);

            TileEntity clickedTile = world.getTileEntity(controllerPos);
            int assemblerMode = getAssemblerMode(clickedTile);

            if (assemblerMode != MODE_NONE) {
                // Important: run on BOTH client and server so getModules() sees the context.
                setAssemblerBuilderContext(heldStack, world, controllerPos, assemblerMode);

                if (!world.isRemote) {
                    player.openGui(
                            LibVulpes.instance,
                            GuiHandler.guiId.MODULARNOINV.ordinal(),
                            world,
                            -1,
                            -1,
                            0
                    );
                }

                return EnumActionResult.SUCCESS;
            }
            clearAssemblerContext(heldStack);
            if (!world.isRemote) {
                player.openGui(
                        LibVulpes.instance,
                        GuiHandler.guiId.MODULARNOINV.ordinal(),
                        world,
                        -1,
                        -1,
                        0
                );
            }
            return EnumActionResult.SUCCESS;
        }
        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        }
        TileEntity tile = world.getTileEntity(controllerPos);

        // IMPORTANT:
        // No special assembler handling here.
        // Normal right-click should stay AR's normal behavior.

        if (!(tile instanceof TileMultiBlock)) {
            return EnumActionResult.PASS;
        }

        TileMultiBlock multiblock = (TileMultiBlock) tile;
        Object[][][] structure = multiblock.getStructure();

        if (structure == null) {
            send(player, LANG_MSG_NO_STRUCTURE);
            return EnumActionResult.FAIL;
        }

        BlockPos offset = getControllerOffset(structure);

        if (offset == null) {
            send(player, LANG_MSG_NO_CONTROLLER);
            return EnumActionResult.FAIL;
        }

        EnumFacing dir = BlockMultiblockMachine.getFront(world.getBlockState(controllerPos)).getOpposite();

        int blocked = 0;
        int alreadyValid = 0;
        int ignored = 0;
        int placed = 0;
        int mePlaced = 0;
        int emcPlaced = 0;
        int missingItems = 0;

        boolean useInventorySource = useInventory(heldStack);
        boolean useEmcSource = useEmc(heldStack) && isEmcAvailable();
        boolean useMeSource = useMe(heldStack) && isMeAvailable();
        boolean debugOutput = HoloAssemblerConfig.enableDebugMode && isDebugEnabled(heldStack);

        for (int y = 0; y < structure.length; y++) {
            for (int z = 0; z < structure[0].length; z++) {
                for (int x = 0; x < structure[0][0].length; x++) {
                    Object cell = structure[y][z][x];

                    if (cell == null) {
                        ignored++;
                        continue;
                    }

                    if (cell instanceof Character && (Character) cell == 'c') {
                        ignored++;
                        continue;
                    }

                    List<BlockMeta> allowed = multiblock.getAllowableBlocks(cell);

                    if (allowed == null || allowed.isEmpty()) {
                        ignored++;
                        continue;
                    }

                    if (isAirRequirement(allowed)) {
                        ignored++;
                        continue;
                    }

                    BlockPos targetPos = toWorldPos(controllerPos, offset, structure.length, dir, x, y, z);

                    if (matchesAllowed(world, targetPos, allowed)) {
                        alreadyValid++;
                        continue;
                    }

                    if (!canReplaceForAssembly(world, targetPos)) {
                        blocked++;
                        continue;
                    }

                    if (player.capabilities.isCreativeMode) {
                        List<BlockMeta> creativeAllowed = getAllowedForCreativeAutomaticPlacement(cell, allowed);

                        if (placeFromInventory(world, targetPos, creativeAllowed, player)) {
                            placed++;
                        } else {
                            missingItems++;
                        }

                        continue;
                    }

                    List<BlockMeta> survivalAllowed = getAllowedForSurvivalAutomaticPlacement(allowed);

                    if (survivalAllowed.isEmpty()) {
                        missingItems++;
                        continue;
                    }

                    boolean didPlace = false;

                    if (useInventorySource) {
                        didPlace = placeFromInventory(world, targetPos, survivalAllowed, player);
                    }

                    if (didPlace) {
                        placed++;
                    } else if (useEmcSource && ProjectECompat.tryPlaceFromEMC(
                            player,
                            survivalAllowed,
                            (block, meta) -> placeBlock(world, targetPos, block, meta)
                    )) {
                        placed++;
                        emcPlaced++;
                    } else if (useMeSource && AE2Compat.tryPlaceFromME(
                            heldStack,
                            player,
                            survivalAllowed,
                            (block, meta) -> placeBlock(world, targetPos, block, meta)
                    )) {
                        placed++;
                        mePlaced++;
                    } else {
                        missingItems++;
                    }
                }
            }
        }

        Map<String, Integer> stillIncorrect = new LinkedHashMap<>();

        int remainingIncorrect = collectRemainingIncorrect(
                world,
                controllerPos,
                offset,
                structure.length,
                dir,
                multiblock,
                structure,
                stillIncorrect
        );

        if (remainingIncorrect == 0) {
            if (placed == 0) {
                if (debugOutput) {
                    send(
                            player,
                            LANG_MSG_STRUCTURE_COMPLETE_DEBUG,
                            alreadyValid,
                            ignored,
                            formatActiveSources(useInventorySource, useEmcSource, useMeSource)
                    );
                } else {
                    send(player, LANG_MSG_STRUCTURE_COMPLETE);
                }
            } else {
                if (debugOutput) {
                    send(
                            player,
                            LANG_MSG_ASSEMBLY_COMPLETE_DEBUG,
                            placed,
                            emcPlaced,
                            mePlaced
                    );
                } else {
                    send(player, LANG_MSG_ASSEMBLY_COMPLETE);
                }
            }
        } else {
            if (debugOutput) {
                send(
                        player,
                        LANG_MSG_COULD_NOT_PLACE_DEBUG,
                        formatCouldNotPlace(stillIncorrect),
                        placed,
                        emcPlaced,
                        mePlaced,
                        remainingIncorrect,
                        blocked,
                        missingItems,
                        formatActiveSources(useInventorySource, useEmcSource, useMeSource)
                );
            } else {
                send(player, LANG_MSG_COULD_NOT_PLACE, formatCouldNotPlace(stillIncorrect));
            }
        }

        return EnumActionResult.SUCCESS;
    }

    private static int getAssemblerMode(TileEntity tile) {
        // Order matters: Station/Unmanned are special cases before Rocket.
        if (tile instanceof TileStationAssembler) {
            return MODE_STATION;
        }
        if (tile instanceof TileUnmannedVehicleAssembler) {
            return MODE_UNMANNED;
        }
        if (tile instanceof TileRocketAssemblingMachine) {
            return MODE_ROCKET;
        }
        return MODE_NONE;
    }

    private static void setAssemblerBuilderContext(ItemStack stack, World world, BlockPos pos, int mode) {
        NBTTagCompound tag = getOrCreateTag(stack);

        tag.setInteger(NBT_ASSEMBLER_MODE, mode);
        tag.setInteger(NBT_ASSEMBLER_X, pos.getX());
        tag.setInteger(NBT_ASSEMBLER_Y, pos.getY());
        tag.setInteger(NBT_ASSEMBLER_Z, pos.getZ());
        tag.setInteger(NBT_ASSEMBLER_DIM, world.provider.getDimension());

        if (!tag.hasKey(NBT_LEFT_OFFSET)) {
            tag.setInteger(NBT_LEFT_OFFSET, 1);
        }

        if (!tag.hasKey(NBT_RIGHT_OFFSET)) {
            tag.setInteger(NBT_RIGHT_OFFSET, 1);
        }

        if (!tag.hasKey(NBT_LENGTH)) {
            tag.setInteger(NBT_LENGTH, 3);
        }

        if (!tag.hasKey(NBT_HEIGHT)) {
            tag.setInteger(NBT_HEIGHT, mode == MODE_UNMANNED ? 3 : 4);
        }
        if (!tag.hasKey(NBT_TOWER_SIDE)) {
            tag.setInteger(NBT_TOWER_SIDE, TOWER_SIDE_OPPOSITE);
        }
        tag.setBoolean(NBT_PREVIEW_ACTIVE, false);
        tag.setBoolean(NBT_BUILD_REQUESTED, false);
        clampAssemblerSettings(stack);
    }

    private static int collectRemainingIncorrect(World world,
                                                 BlockPos controllerPos,
                                                 BlockPos offset,
                                                 int structureHeight,
                                                 EnumFacing dir,
                                                 TileMultiBlock multiblock,
                                                 Object[][][] structure,
                                                 Map<String, Integer> stillIncorrect) {
        int remaining = 0;

        for (int y = 0; y < structure.length; y++) {
            for (int z = 0; z < structure[0].length; z++) {
                for (int x = 0; x < structure[0][0].length; x++) {
                    Object cell = structure[y][z][x];

                    if (cell == null) {
                        continue;
                    }

                    if (cell instanceof Character && (Character) cell == 'c') {
                        continue;
                    }

                    List<BlockMeta> allowed = multiblock.getAllowableBlocks(cell);

                    if (allowed == null || allowed.isEmpty()) {
                        continue;
                    }

                    if (isAirRequirement(allowed)) {
                        continue;
                    }

                    BlockPos targetPos = toWorldPos(controllerPos, offset, structureHeight, dir, x, y, z);

                    if (!matchesAllowed(world, targetPos, allowed)) {
                        remaining++;
                        addCouldNotPlace(stillIncorrect, allowed);
                    }
                }
            }
        }

        return remaining;
    }

    private static List<BlockMeta> getAllowedForCreativeAutomaticPlacement(Object cell,
                                                                           List<BlockMeta> allowed) {
        if (!isPowerInputCell(cell)) {
            return allowed;
        }

        List<BlockMeta> preferred = new LinkedList<>();

        for (BlockMeta blockMeta : allowed) {
            if (blockMeta != null && isCreativePowerInput(blockMeta)) {
                preferred.add(blockMeta);
            }
        }

        for (BlockMeta blockMeta : allowed) {
            if (blockMeta != null && !isCreativePowerInput(blockMeta)) {
                preferred.add(blockMeta);
            }
        }

        return preferred;
    }

    private static boolean isPowerInputCell(Object cell) {
        return cell instanceof Character && (Character) cell == 'P';
    }

    private static List<BlockMeta> getAllowedForSurvivalAutomaticPlacement(List<BlockMeta> allowed) {
        List<BlockMeta> filtered = new LinkedList<>();

        for (BlockMeta blockMeta : allowed) {
            if (blockMeta == null) {
                continue;
            }

            if (isCreativePowerInput(blockMeta)) {
                continue;
            }

            filtered.add(blockMeta);
        }

        return filtered;
    }

    private static boolean isCreativePowerInput(BlockMeta blockMeta) {
        return LibVulpesBlocks.blockCreativeInputPlug != null
                && blockMeta.getBlock() == LibVulpesBlocks.blockCreativeInputPlug;
    }

    private static boolean canReplaceForAssembly(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (world.isAirBlock(pos)) {
            return true;
        }

        if (state.getMaterial().isLiquid()) {
            return isFlowingLiquid(state, block);
        }
        return block.isReplaceable(world, pos);
    }

    private static boolean isFlowingLiquid(IBlockState state, Block block) {
        if (!state.getMaterial().isLiquid()) {
            return false;
        }

        if (block instanceof BlockLiquid && state.getProperties().containsKey(BlockLiquid.LEVEL)) {
            return state.getValue(BlockLiquid.LEVEL) > 0;
        }

        return block.getMetaFromState(state) > 0;
    }

    private static boolean placeFromInventory(World world,
                                              BlockPos pos,
                                              List<BlockMeta> allowed,
                                              EntityPlayer player) {
        for (BlockMeta blockMeta : allowed) {
            if (blockMeta == null) {
                continue;
            }

            Block block = blockMeta.getBlock();
            if (block == null || block == Blocks.AIR) {
                continue;
            }

            if (player.capabilities.isCreativeMode) {
                IBlockState state = blockMeta.getBlockState();

                if (placeBlockState(world, pos, state)) {
                    return true;
                }
                continue;
            }

            int slot = findInventorySlot(player, blockMeta);
            if (slot < 0) {
                continue;
            }

            ItemStack stack = player.inventory.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }

            if (!placeBlock(world, pos, block, stack.getMetadata())) {
                continue;
            }

            stack.shrink(1);

            if (stack.getCount() <= 0) {
                player.inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
            }

            player.inventory.markDirty();

            return true;
        }

        return false;
    }

    private static boolean placeBlock(World world,
                                      BlockPos pos,
                                      Block block,
                                      int meta) {
        IBlockState state;

        try {
            state = block.getStateFromMeta(meta);
        } catch (Exception ignored) {
            state = block.getDefaultState();
        }

        if (!canReplaceForAssembly(world, pos)) {
            return false;
        }

        return world.setBlockState(pos, state, 3);
    }

    private static int findInventorySlot(EntityPlayer player, BlockMeta wantedBlockMeta) {
        Block block = wantedBlockMeta.getBlock();
        Item wantedItem = Item.getItemFromBlock(block);

        if (wantedItem == null || wantedItem == Item.getItemFromBlock(Blocks.AIR)) {
            return -1;
        }

        for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++) {
            ItemStack stack = player.inventory.getStackInSlot(slot);

            if (stack.isEmpty()) {
                continue;
            }

            if (!(stack.getItem() instanceof ItemBlock)) {
                continue;
            }

            if (stack.getItem() != wantedItem) {
                continue;
            }

            BlockMeta actual = new BlockMeta(block, stack.getMetadata());

            if (wantedBlockMeta.equals(actual)) {
                return slot;
            }
        }

        return -1;
    }

    private static boolean placeBlockState(World world, BlockPos pos, IBlockState state) {
        if (!canReplaceForAssembly(world, pos)) {
            return false;
        }

        return world.setBlockState(pos, state, 3);
    }

    private static BlockPos getControllerOffset(Object[][][] structure) {
        for (int y = 0; y < structure.length; y++) {
            for (int z = 0; z < structure[0].length; z++) {
                for (int x = 0; x < structure[0][0].length; x++) {
                    Object cell = structure[y][z][x];

                    if (cell instanceof Character && (Character) cell == 'c') {
                        return new BlockPos(x, y, z);
                    }
                }
            }
        }

        return null;
    }

    private static BlockPos toWorldPos(BlockPos controllerPos,
                                       BlockPos offset,
                                       int structureHeight,
                                       EnumFacing dir,
                                       int x,
                                       int y,
                                       int z) {
        int controllerYFromBottom = structureHeight - offset.getY();

        int baseX = controllerPos.getX()
                - (-offset.getX() * dir.getFrontOffsetZ() + offset.getZ() * dir.getFrontOffsetX());

        int baseY = controllerPos.getY() - controllerYFromBottom + 1;

        int baseZ = controllerPos.getZ()
                - ((offset.getX() * dir.getFrontOffsetX()) + (offset.getZ() * dir.getFrontOffsetZ()));

        int globalX = baseX - x * dir.getFrontOffsetZ() + z * dir.getFrontOffsetX();
        int globalY = -y + structureHeight + baseY - 1;
        int globalZ = baseZ + x * dir.getFrontOffsetX() + z * dir.getFrontOffsetZ();

        return new BlockPos(globalX, globalY, globalZ);
    }

    private static boolean matchesAllowed(World world, BlockPos pos, List<BlockMeta> allowed) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        int meta = block.getMetaFromState(state);

        BlockMeta actual = new BlockMeta(block, meta);
        for (BlockMeta allowedBlock : allowed) {
            if (allowedBlock == null) {
                continue;
            }
            if (allowedBlock.equals(actual)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isAirRequirement(List<BlockMeta> allowed) {
        for (BlockMeta blockMeta : allowed) {
            if (blockMeta == null) {
                continue;
            }

            if (blockMeta.getBlock() != Blocks.AIR) {
                return false;
            }
        }

        return true;
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World world,
                                                    EntityPlayer player,
                                                    EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking()) {
            clearPreview(player);
            clearAssemblerContext(stack);
            if (!world.isRemote) {
                player.openGui(
                        LibVulpes.instance,
                        GuiHandler.guiId.MODULARNOINV.ordinal(),
                        world,
                        -1,
                        -1,
                        0
                );
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    private static boolean isWildcardMeta(int meta) {
        return meta == Short.MAX_VALUE || meta == 32767 || meta == -1;
    }

    private static void addCouldNotPlace(Map<String, Integer> map, List<BlockMeta> allowed) {
        String name = getDisplayNameForAllowedBlock(allowed);
        map.put(name, map.getOrDefault(name, 0) + 1);
    }

    private static String getDisplayNameForAllowedBlock(List<BlockMeta> allowed) {
        List<BlockMeta> preferredAllowed = getAllowedForSurvivalAutomaticPlacement(allowed);

        if (!preferredAllowed.isEmpty()) {
            allowed = preferredAllowed;
        }

        for (BlockMeta blockMeta : allowed) {
            if (blockMeta == null) {
                continue;
            }

            Block block = blockMeta.getBlock();

            if (block == null || block == Blocks.AIR) {
                continue;
            }

            Item item = Item.getItemFromBlock(block);

            if (item == null || item == Item.getItemFromBlock(Blocks.AIR)) {
                return simplifyFeedbackName(block.getLocalizedName());
            }

            int meta = blockMeta.getMeta();
            int displayMeta = isWildcardMeta(meta) ? 0 : meta;

            return simplifyFeedbackName(new ItemStack(item, 1, displayMeta).getDisplayName());
        }

        return tr(LANG_MSG_UNKNOWN_BLOCK);
    }

    private static String formatCouldNotPlace(Map<String, Integer> map) {
        StringJoiner joiner = new StringJoiner(", ");

        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            joiner.add(entry.getKey() + " x" + entry.getValue());
        }

        return joiner.toString();
    }

    private static String formatActiveSources(boolean inventory, boolean emc, boolean me) {
        StringJoiner joiner = new StringJoiner(" > ");

        if (inventory) {joiner.add(tr(LANG_GUI_SOURCE_INVENTORY));}
        if (emc) {joiner.add(tr(LANG_GUI_SOURCE_EMC));}
        if (me) {joiner.add(tr(LANG_GUI_SOURCE_ME));}
        String result = joiner.toString();
        return result.isEmpty() ? tr(LANG_GUI_ACTIVE_SOURCES_NONE) : result;
    }

    private static void send(EntityPlayer player, String langKey, Object... args) {
        player.sendMessage(new TextComponentTranslation(langKey, args));
    }

    private static boolean useInventory(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();

        if (tag == null || !tag.hasKey(NBT_USE_INVENTORY)) {
            return true;
        }

        return tag.getBoolean(NBT_USE_INVENTORY);
    }

    private static void setUseInventory(ItemStack stack, boolean value) {
        getOrCreateTag(stack).setBoolean(NBT_USE_INVENTORY, value);
    }

    private static boolean useEmc(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.getBoolean(NBT_USE_EMC);
    }

    private static void setUseEmc(ItemStack stack, boolean value) {
        getOrCreateTag(stack).setBoolean(NBT_USE_EMC, value);
    }

    private static boolean useMe(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.getBoolean(NBT_USE_ME);
    }

    private static void setUseMe(ItemStack stack, boolean value) {
        getOrCreateTag(stack).setBoolean(NBT_USE_ME, value);
    }

    private static boolean isDebugEnabled(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.getBoolean(NBT_DEBUG);
    }

    private static void setDebugEnabled(ItemStack stack, boolean value) {
        getOrCreateTag(stack).setBoolean(NBT_DEBUG, value);
    }

    private static NBTTagCompound getOrCreateTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();

        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }

        return tag;
    }

    private static boolean isEmcAvailable() {
        return ProjectECompat.isEnabled();
    }

    private static boolean isMeAvailable() {
        return AE2Compat.isEnabled();
    }

    @Override
    public void writeDataToNetwork(ByteBuf out, byte id, @Nonnull ItemStack stack) {
        if (id == PACKET_SETTINGS) {
            out.writeBoolean(useInventory(stack));
            out.writeBoolean(useEmc(stack));
            out.writeBoolean(useMe(stack));
            out.writeBoolean(isDebugEnabled(stack));
        }
        else if (id == PACKET_ASSEMBLER_GUI) {
            NBTTagCompound tag = stack.getTagCompound();

            out.writeInt(tag == null ? MODE_NONE : tag.getInteger(NBT_ASSEMBLER_MODE));
            out.writeInt(tag == null ? 0 : tag.getInteger(NBT_ASSEMBLER_X));
            out.writeInt(tag == null ? 0 : tag.getInteger(NBT_ASSEMBLER_Y));
            out.writeInt(tag == null ? 0 : tag.getInteger(NBT_ASSEMBLER_Z));
            out.writeInt(tag == null ? 0 : tag.getInteger(NBT_ASSEMBLER_DIM));

            out.writeInt(getLeftOffset(stack));
            out.writeInt(getRightOffset(stack));
            out.writeInt(getAssemblerLength(stack));
            out.writeInt(getAssemblerHeight(stack));
            out.writeInt(getTowerSide(stack));
            out.writeBoolean(tag != null && tag.getBoolean(NBT_PREVIEW_ACTIVE));
            out.writeBoolean(tag != null && tag.getBoolean(NBT_BUILD_REQUESTED));
        }
    }

    @Override
    public void readDataFromNetwork(ByteBuf in, byte packetId, NBTTagCompound nbt, @Nonnull ItemStack stack) {
        if (packetId == PACKET_SETTINGS) {
            nbt.setBoolean(NBT_USE_INVENTORY, in.readBoolean());
            nbt.setBoolean(NBT_USE_EMC, in.readBoolean());
            nbt.setBoolean(NBT_USE_ME, in.readBoolean());
            nbt.setBoolean(NBT_DEBUG, in.readBoolean());
        }
        else if (packetId == PACKET_ASSEMBLER_GUI) {
            nbt.setInteger(NBT_ASSEMBLER_MODE, in.readInt());
            nbt.setInteger(NBT_ASSEMBLER_X, in.readInt());
            nbt.setInteger(NBT_ASSEMBLER_Y, in.readInt());
            nbt.setInteger(NBT_ASSEMBLER_Z, in.readInt());
            nbt.setInteger(NBT_ASSEMBLER_DIM, in.readInt());

            nbt.setInteger(NBT_LEFT_OFFSET, in.readInt());
            nbt.setInteger(NBT_RIGHT_OFFSET, in.readInt());
            nbt.setInteger(NBT_LENGTH, in.readInt());
            nbt.setInteger(NBT_HEIGHT, in.readInt());
            nbt.setInteger(NBT_TOWER_SIDE, in.readInt());

            nbt.setBoolean(NBT_PREVIEW_ACTIVE, in.readBoolean());
            nbt.setBoolean(NBT_BUILD_REQUESTED, in.readBoolean());
        }
    }

    private static String simplifyFeedbackName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        String lowerName = name.toLowerCase(Locale.ROOT);

        if (lowerName.endsWith(" hatch")) {
            return name.substring(0, name.length() - " hatch".length()).trim();
        }

        if (lowerName.endsWith(" plug")) {
            return name.substring(0, name.length() - " plug".length()).trim();
        }

        return name;
    }

    @Override
    public void useNetworkData(EntityPlayer player, Side side, byte id, NBTTagCompound nbt, @Nonnull ItemStack stack) {
        if (side != Side.SERVER) {
            return;
        }
        if (id == PACKET_SETTINGS) {
            setUseInventory(stack, nbt.getBoolean(NBT_USE_INVENTORY));

            if (isEmcAvailable()) {
                setUseEmc(stack, nbt.getBoolean(NBT_USE_EMC));
            } else {
                setUseEmc(stack, false);
            }

            if (isMeAvailable()) {
                setUseMe(stack, nbt.getBoolean(NBT_USE_ME));
            } else {
                setUseMe(stack, false);
            }

            if (HoloAssemblerConfig.enableDebugMode) {
                setDebugEnabled(stack, nbt.getBoolean(NBT_DEBUG));
            } else {
                setDebugEnabled(stack, false);
            }
        }
        else if (id == PACKET_ASSEMBLER_GUI) {
            applyAssemblerGuiPacket(player, stack, nbt);
        }
    }

    private void applyAssemblerGuiPacket(EntityPlayer player, ItemStack stack, NBTTagCompound nbt) {
        if (player == null || player.world == null || player.world.isRemote) {
            return;
        }

        NBTTagCompound tag = getOrCreateTag(stack);

        tag.setInteger(NBT_ASSEMBLER_MODE, nbt.getInteger(NBT_ASSEMBLER_MODE));
        tag.setInteger(NBT_ASSEMBLER_X, nbt.getInteger(NBT_ASSEMBLER_X));
        tag.setInteger(NBT_ASSEMBLER_Y, nbt.getInteger(NBT_ASSEMBLER_Y));
        tag.setInteger(NBT_ASSEMBLER_Z, nbt.getInteger(NBT_ASSEMBLER_Z));
        tag.setInteger(NBT_ASSEMBLER_DIM, nbt.getInteger(NBT_ASSEMBLER_DIM));
        tag.setInteger(NBT_LEFT_OFFSET, nbt.getInteger(NBT_LEFT_OFFSET));
        tag.setInteger(NBT_RIGHT_OFFSET, nbt.getInteger(NBT_RIGHT_OFFSET));
        tag.setInteger(NBT_LENGTH, nbt.getInteger(NBT_LENGTH));
        tag.setInteger(NBT_HEIGHT, nbt.getInteger(NBT_HEIGHT));
        tag.setBoolean(NBT_PREVIEW_ACTIVE, nbt.getBoolean(NBT_PREVIEW_ACTIVE));
        tag.setInteger(NBT_TOWER_SIDE, nbt.getInteger(NBT_TOWER_SIDE));
        clampAssemblerSettings(stack);

        boolean previewRequested = nbt.getBoolean(NBT_PREVIEW_ACTIVE);
        tag.setBoolean(NBT_PREVIEW_ACTIVE, false);
        if (previewRequested) {
            executeGeneratedAssemblerPreview(player, stack);
        } else {
            clearPreview(player);
        }
        boolean buildRequested = nbt.getBoolean(NBT_BUILD_REQUESTED);
        tag.setBoolean(NBT_BUILD_REQUESTED, false);

        if (buildRequested) {
            clearPreview(player);
            executeGeneratedAssemblerBuild(player, stack);
        }
    }

    private void executeGeneratedAssemblerBuild(EntityPlayer player, ItemStack stack) {
        World world = player.world;
        NBTTagCompound tag = stack.getTagCompound();

        if (tag == null) {
            return;
        }

        if (tag.getInteger(NBT_ASSEMBLER_DIM) != world.provider.getDimension()) {
            send(player, LANG_MSG_ASSEMBLER_WRONG_DIMENSION);
            return;
        }

        BlockPos assemblerPos = new BlockPos(
                tag.getInteger(NBT_ASSEMBLER_X),
                tag.getInteger(NBT_ASSEMBLER_Y),
                tag.getInteger(NBT_ASSEMBLER_Z)
        );

        if (player.getDistanceSq(assemblerPos) > 64.0D) {
            send(player, LANG_MSG_ASSEMBLER_TOO_FAR);
            return;
        }

        if (!world.isBlockLoaded(assemblerPos)) {
            send(player, LANG_MSG_ASSEMBLER_NOT_LOADED);
            return;
        }

        TileEntity tile = world.getTileEntity(assemblerPos);
        int expectedMode = getAssemblerMode(stack);
        int actualMode = getAssemblerMode(tile);

        if (expectedMode == MODE_NONE || actualMode != expectedMode) {
            send(player, LANG_MSG_ASSEMBLER_INVALID);
            clearAssemblerContext(stack);
            return;
        }

        GeneratedPlan plan = createGeneratedPlan(world, assemblerPos, expectedMode, stack);
        if (plan == null || plan.cells.isEmpty()) {
            send(player, LANG_MSG_ASSEMBLER_INVALID_PLAN);
            return;
        }

        int blocked = 0;
        int alreadyValid = 0;

        for (GeneratedCell cell : plan.cells) {
            if (matchesAllowed(world, cell.pos, cell.allowed)) {
                alreadyValid++;
                continue;
            }

            if (!canReplaceForAssembly(world, cell.pos)) {
                blocked++;
            }
        }

        if (blocked > 0) {
            send(player, LANG_MSG_ASSEMBLER_BLOCKED, blocked);
            // Later: also feed these positions into your preview/red-X client handler.
            return;
        }

        int placed = 0;
        int missing = 0;
        int emcPlaced = 0;
        int mePlaced = 0;

        boolean useInventorySource = useInventory(stack);
        boolean useEmcSource = useEmc(stack) && isEmcAvailable();
        boolean useMeSource = useMe(stack) && isMeAvailable();

        for (GeneratedCell cell : plan.cells) {
            if (matchesAllowed(world, cell.pos, cell.allowed)) {
                continue;
            }

            boolean didPlace = false;

            if (player.capabilities.isCreativeMode) {
                didPlace = placeFromInventory(world, cell.pos, cell.allowed, player);
            } else {
                List survivalAllowed = getAllowedForSurvivalAutomaticPlacement(cell.allowed);

                if (useInventorySource) {
                    didPlace = placeFromInventory(world, cell.pos, survivalAllowed, player);
                }

                if (!didPlace && useEmcSource && ProjectECompat.tryPlaceFromEMC(
                        player,
                        survivalAllowed,
                        (block, meta) -> placeBlock(world, cell.pos, block, meta)
                )) {
                    didPlace = true;
                    emcPlaced++;
                }

                if (!didPlace && useMeSource && AE2Compat.tryPlaceFromME(
                        stack,
                        player,
                        survivalAllowed,
                        (block, meta) -> placeBlock(world, cell.pos, block, meta)
                )) {
                    didPlace = true;
                    mePlaced++;
                }
            }

            if (didPlace) {
                placed++;
            } else {
                missing++;
            }
        }

        if (missing == 0) {
            send(player, LANG_MSG_ASSEMBLER_BUILD_COMPLETE, placed, alreadyValid, emcPlaced, mePlaced);
        } else {
            send(player, LANG_MSG_ASSEMBLER_BUILD_INCOMPLETE, placed, missing);
        }
    }

    private void executeGeneratedAssemblerPreview(EntityPlayer player, ItemStack stack) {
        if (player == null || player.world == null || player.world.isRemote) {
            return;
        }

        clearPreview(player);

        World world = player.world;
        NBTTagCompound tag = stack.getTagCompound();

        if (tag == null) {
            return;
        }

        if (tag.getInteger(NBT_ASSEMBLER_DIM) != world.provider.getDimension()) {
            send(player, "message.holoassemblerar.assembler_wrong_dimension");
            return;
        }

        BlockPos assemblerPos = new BlockPos(
                tag.getInteger(NBT_ASSEMBLER_X),
                tag.getInteger(NBT_ASSEMBLER_Y),
                tag.getInteger(NBT_ASSEMBLER_Z)
        );

        if (player.getDistanceSq(assemblerPos) > PREVIEW_MAX_DISTANCE_SQ) {
            send(player, "message.holoassemblerar.assembler_too_far");
            return;
        }

        if (!world.isBlockLoaded(assemblerPos)) {
            send(player, "message.holoassemblerar.assembler_not_loaded");
            return;
        }

        TileEntity tile = world.getTileEntity(assemblerPos);
        int expectedMode = getAssemblerMode(stack);
        int actualMode = getAssemblerMode(tile);

        if (expectedMode == MODE_NONE || actualMode != expectedMode) {
            send(player, "message.holoassemblerar.assembler_invalid");
            clearAssemblerContext(stack);
            return;
        }

        GeneratedPlan plan = createGeneratedPlan(world, assemblerPos, expectedMode, stack);

        if (plan == null || plan.cells.isEmpty()) {
            send(player, "message.holoassemblerar.assembler_invalid_plan");
            return;
        }

        List<BlockPos> phantomPositions = new ArrayList<>();

        for (GeneratedCell cell : plan.cells) {
            if (matchesAllowed(world, cell.pos, cell.allowed)) {
                continue;
            }

            if (!canReplaceForAssembly(world, cell.pos)) {
                continue;
            }

            if (placePreviewPhantom(world, cell.pos, cell.allowed)) {
                phantomPositions.add(cell.pos);
            }
        }

        long expireWorldTime = world.getTotalWorldTime() + PREVIEW_TIMEOUT_TICKS;

        ACTIVE_PREVIEWS.put(
                player.getUniqueID(),
                new ActivePreview(
                        world.provider.getDimension(),
                        assemblerPos,
                        phantomPositions,
                        expireWorldTime
                )
        );
        tag.setBoolean(NBT_PREVIEW_ACTIVE, true);
        tag.setLong(NBT_PREVIEW_EXPIRE, expireWorldTime);
    }

    private static boolean placePreviewPhantom(World world, BlockPos pos, List<BlockMeta> allowed) {
        if (!canReplaceForAssembly(world, pos)) {
            return false;
        }

        BlockMeta previewBlock = getPreviewBlock(allowed);

        if (previewBlock == null) {
            return false;
        }

        world.setBlockState(
                pos,
                LibVulpesBlocks.blockPhantom.getStateFromMeta(previewBlock.getMeta()),
                3
        );

        TileEntity newTile = world.getTileEntity(pos);

        if (newTile instanceof TilePlaceholder) {
            ((TileSchematic) newTile).setReplacedBlock(allowed);
            ((TilePlaceholder) newTile).setReplacedTileEntity(
                    previewBlock.getBlock().createTileEntity(
                            world,
                            previewBlock.getBlock().getDefaultState()
                    )
            );
        }

        return true;
    }

    private static BlockMeta getPreviewBlock(List<BlockMeta> allowed) {
        for (BlockMeta blockMeta : allowed) {
            if (blockMeta == null) {
                continue;
            }

            Block block = blockMeta.getBlock();

            if (block == null || block == Blocks.AIR) {
                continue;
            }

            return blockMeta;
        }

        return null;
    }

    private static void clearPreview(EntityPlayer player) {
        if (player == null) {
            return;
        }

        ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);

        if (!stack.isEmpty() && stack.getItem() instanceof ItemHoloAssembler) {
            NBTTagCompound tag = stack.getTagCompound();

            if (tag != null) {
                tag.setBoolean(NBT_PREVIEW_ACTIVE, false);
                tag.removeTag(NBT_PREVIEW_EXPIRE);
            }
        }

        if (player.world == null || player.world.isRemote) {
            return;
        }

        ActivePreview preview = ACTIVE_PREVIEWS.remove(player.getUniqueID());

        if (preview == null) {
            return;
        }

        if (player.getServer() == null) {
            return;
        }

        WorldServer world = player.getServer().getWorld(preview.dimension);

        if (world == null) {
            return;
        }

        for (BlockPos pos : preview.phantomPositions) {
            if (world.getBlockState(pos).getBlock() == LibVulpesBlocks.blockPhantom) {
                world.setBlockToAir(pos);
            }
        }
    }

    private static void tickPreview(EntityPlayer player) {
        if (player == null || player.world == null || player.world.isRemote) {
            return;
        }

        ActivePreview preview = ACTIVE_PREVIEWS.get(player.getUniqueID());

        if (preview == null) {
            return;
        }

        if (player.world.provider.getDimension() != preview.dimension) {
            clearPreview(player);
            return;
        }

        if (player.world.getTotalWorldTime() >= preview.expireWorldTime) {
            clearPreview(player);
            return;
        }

        if (player.getDistanceSq(preview.assemblerPos) > PREVIEW_MAX_DISTANCE_SQ) {
            clearPreview(player);
        }
    }

    @Override
    public void onUpdate(ItemStack stack,
                         World world,
                         Entity entity,
                         int itemSlot,
                         boolean isSelected) {
        super.onUpdate(stack, world, entity, itemSlot, isSelected);

        if (!world.isRemote && entity instanceof EntityPlayer) {
            tickPreview((EntityPlayer) entity);
        }
    }

    private static int getAssemblerMode(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null ? MODE_NONE : tag.getInteger(NBT_ASSEMBLER_MODE);
    }

    private static void clearAssemblerContext(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            return;
        }

        tag.setInteger(NBT_ASSEMBLER_MODE, MODE_NONE);
        tag.removeTag(NBT_ASSEMBLER_X);
        tag.removeTag(NBT_ASSEMBLER_Y);
        tag.removeTag(NBT_ASSEMBLER_Z);
        tag.removeTag(NBT_ASSEMBLER_DIM);
        tag.removeTag(NBT_PREVIEW_ACTIVE);
        tag.removeTag(NBT_TOWER_SIDE);
        tag.removeTag(NBT_BUILD_REQUESTED);
    }

    private static int getLeftOffset(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null ? 1 : tag.getInteger(NBT_LEFT_OFFSET);
    }

    private static void setLeftOffset(ItemStack stack, int value) {
        getOrCreateTag(stack).setInteger(NBT_LEFT_OFFSET, value);
    }

    private static int getRightOffset(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null ? 1 : tag.getInteger(NBT_RIGHT_OFFSET);
    }

    private static void setRightOffset(ItemStack stack, int value) {
        getOrCreateTag(stack).setInteger(NBT_RIGHT_OFFSET, value);
    }

    private static int getAssemblerLength(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null ? 3 : tag.getInteger(NBT_LENGTH);
    }

    private static void setAssemblerLength(ItemStack stack, int value) {
        getOrCreateTag(stack).setInteger(NBT_LENGTH, value);
    }

    private static int getAssemblerHeight(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        int mode = getAssemblerMode(stack);
        return tag == null ? (mode == MODE_UNMANNED ? 3 : 4) : tag.getInteger(NBT_HEIGHT);
    }

    private static void setAssemblerHeight(ItemStack stack, int value) {
        getOrCreateTag(stack).setInteger(NBT_HEIGHT, value);
    }

    private static void clampAssemblerSettings(ItemStack stack) {
        int mode = getAssemblerMode(stack);
        int maxWidth = mode == MODE_UNMANNED ? 17 : 16;
        int minHeight = mode == MODE_UNMANNED ? 3 : 4;
        int maxHeight = mode == MODE_UNMANNED ? 17 : 128;
        int maxLength = mode == MODE_UNMANNED ? 17 : 16;

        int left = Math.max(1, getLeftOffset(stack));
        int right = Math.max(1, getRightOffset(stack));

        while (left + 1 + right > maxWidth) {
            if (right >= left && right > 1) {
                right--;
            } else if (left > 1) {
                left--;
            } else {
                break;
            }
        }

        setLeftOffset(stack, left);
        setRightOffset(stack, right);
        setAssemblerLength(stack, clamp(getAssemblerLength(stack), 3, maxLength));
        setAssemblerHeight(stack, clamp(getAssemblerHeight(stack), minHeight, maxHeight));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int getTowerSide(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();

        if (tag == null || !tag.hasKey(NBT_TOWER_SIDE)) {
            return TOWER_SIDE_OPPOSITE;
        }

        return tag.getInteger(NBT_TOWER_SIDE);
    }

    private static void setTowerSide(ItemStack stack, int value) {
        getOrCreateTag(stack).setInteger(NBT_TOWER_SIDE, value);
    }

    private static void cycleTowerSide(ItemStack stack) {
        int next = getTowerSide(stack) + 1;

        if (next > TOWER_SIDE_RIGHT) {
            next = TOWER_SIDE_OPPOSITE;
        }

        setTowerSide(stack, next);
    }

    @SideOnly(Side.CLIENT)
    private static String getTowerSideLabel(ItemStack stack) {
        switch (getTowerSide(stack)) {
            case TOWER_SIDE_LEFT:
                return I18n.format(LANG_GUI_TOWER_SIDE_LEFT);

            case TOWER_SIDE_RIGHT:
                return I18n.format(LANG_GUI_TOWER_SIDE_RIGHT);

            case TOWER_SIDE_OPPOSITE:
            default:
                return I18n.format(LANG_GUI_TOWER_SIDE_OPPOSITE);
        }
    }

    private static class ModuleStaticStarryPanel extends ModuleContainerPan {

        public ModuleStaticStarryPanel(int offsetX,
                                       int offsetY,
                                       List<ModuleBase> moduleList,
                                       List<ModuleBase> staticModules,
                                       net.minecraft.util.ResourceLocation backdrop,
                                       int screenSizeX,
                                       int screenSizeY,
                                       int paddingX,
                                       int paddingY) {
            super(offsetX, offsetY, moduleList, staticModules, backdrop, screenSizeX, screenSizeY, paddingX, paddingY);
        }

        @Override
        public void onScroll(int dwheel) {
            // Disabled: keep starry background and contents fixed.
        }
        @Override
        protected void moveContainerInterior(int deltaX, int deltaY) {
            // Disabled: prevent dragging/panning from moving contents.
        }
        @Override
        public void onMouseClickedAndDragged(int x, int y, int button, long timeSinceLastClick) {
            // Disabled: prevent mouse-drag panning.
        }
    }

    @SideOnly(Side.CLIENT)
    public static List<AxisAlignedBB> getClientPreviewBoxes(World world, EntityPlayer player, ItemStack stack) {
        if (world == null || player == null || stack.isEmpty() || !(stack.getItem() instanceof ItemHoloAssembler)) {
            return Collections.emptyList();
        }

        NBTTagCompound tag = stack.getTagCompound();

        if (tag == null || !tag.getBoolean(NBT_PREVIEW_ACTIVE)) {
            return Collections.emptyList();
        }

        if (world.getTotalWorldTime() >= tag.getLong(NBT_PREVIEW_EXPIRE)) {
            tag.setBoolean(NBT_PREVIEW_ACTIVE, false);
            tag.removeTag(NBT_PREVIEW_EXPIRE);
            return Collections.emptyList();
        }

        if (tag.getInteger(NBT_ASSEMBLER_DIM) != world.provider.getDimension()) {
            tag.setBoolean(NBT_PREVIEW_ACTIVE, false);
            tag.removeTag(NBT_PREVIEW_EXPIRE);
            return Collections.emptyList();
        }

        BlockPos assemblerPos = new BlockPos(
                tag.getInteger(NBT_ASSEMBLER_X),
                tag.getInteger(NBT_ASSEMBLER_Y),
                tag.getInteger(NBT_ASSEMBLER_Z)
        );

        if (player.getDistanceSq(assemblerPos) > PREVIEW_MAX_DISTANCE_SQ) {
            tag.setBoolean(NBT_PREVIEW_ACTIVE, false);
            tag.removeTag(NBT_PREVIEW_EXPIRE);
            return Collections.emptyList();
        }

        if (!world.isBlockLoaded(assemblerPos)) {
            return Collections.emptyList();
        }

        int mode = getAssemblerMode(stack);

        EnumFacing forward = RotatableBlock.getFront(world.getBlockState(assemblerPos)).getOpposite();
        if (forward.getAxis() == EnumFacing.Axis.Y) {
            return Collections.emptyList();
        }

        int leftOffset = getLeftOffset(stack);
        int rightOffset = getRightOffset(stack);
        int length = getAssemblerLength(stack);
        int height = getAssemblerHeight(stack);

        if (mode == MODE_ROCKET || mode == MODE_STATION) {
            return createRocketStationPreviewBoxes(
                    assemblerPos,
                    forward,
                    leftOffset,
                    rightOffset,
                    length,
                    height,
                    getTowerSide(stack)
            );
        }

        if (mode == MODE_UNMANNED) {
            return createUnmannedPreviewBoxes(
                    assemblerPos,
                    forward,
                    leftOffset,
                    rightOffset,
                    length,
                    height
            );
        }

        return Collections.emptyList();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List tooltip, ITooltipFlag flag) {
        tooltip.add(
                TextFormatting.GRAY + I18n.format("tooltip.holoassemblerar.sources")
                        + " "
                        + getColoredSourcePriorityText(stack)
        );

        if (isMeAvailable()) {
            tooltip.add(
                    TextFormatting.AQUA + I18n.format("tooltip.holoassemblerar.me_link")
                            + " "
                            + TextFormatting.WHITE
                            + I18n.format(
                            AE2Compat.getLinkStatusLangKey(stack),
                            AE2Compat.getLinkStatusLangArgs(stack)
                    )
            );
        }
        tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.holoassemblerar.description"));
        tooltip.add(TextFormatting.YELLOW + I18n.format("tooltip.holoassemblerar.use.assemble"));
        tooltip.add(TextFormatting.GOLD + I18n.format("tooltip.holoassemblerar.use.settings"));
    }

    @SideOnly(Side.CLIENT)
    private static String getColoredSourcePriorityText(ItemStack stack) {
        StringJoiner joiner = new StringJoiner(
                TextFormatting.DARK_GRAY + " " + I18n.format("tooltip.holoassemblerar.source_separator") + " " + TextFormatting.RESET
        );
        if (useInventory(stack)) {
            joiner.add(TextFormatting.GREEN + I18n.format(LANG_GUI_SOURCE_INVENTORY));
        }
        if (useEmc(stack) && isEmcAvailable()) {
            joiner.add(TextFormatting.GREEN + I18n.format(LANG_GUI_SOURCE_EMC));
        }
        if (useMe(stack) && isMeAvailable()) {
            joiner.add(TextFormatting.GREEN + I18n.format(LANG_GUI_SOURCE_ME));
        }
        String text = joiner.toString();

        if (text.isEmpty()) {
            return TextFormatting.DARK_GRAY + I18n.format("tooltip.holoassemblerar.sources.none");
        }
        return text;
    }
}