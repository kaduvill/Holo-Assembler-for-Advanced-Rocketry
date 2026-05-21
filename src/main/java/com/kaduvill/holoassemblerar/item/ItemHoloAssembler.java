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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zmaster587.libVulpes.LibVulpes;
import zmaster587.libVulpes.block.BlockMeta;
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
import zmaster587.libVulpes.tile.multiblock.TileMultiBlock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class ItemHoloAssembler extends Item implements IModularInventory, IButtonInventory, IGuiCallback, INetworkItem {

    private static final String NBT_USE_INVENTORY = "UseInventory";
    private static final String NBT_USE_EMC = "UseEmc";
    private static final String NBT_USE_ME = "UseMe";
    private static final String NBT_DEBUG = "Debug";

    private static final byte PACKET_SETTINGS = 0;

    private static final int BTN_INVENTORY = 0;
    private static final int BTN_EMC = 1;
    private static final int BTN_ME = 2;
    private static final int BTN_DEBUG = 3;

    private static final int COLOR_ON = 0xFF55FF55;
    private static final int COLOR_OFF = 0xFFFF5555;
    private static final int TEXT_COLOR = 0x404040;

    private static final int PANEL_X = 5;
    private static final int PANEL_Y = 20;
    private static final int PANEL_WIDTH = 160;
    private static final int PANEL_HEIGHT = 100;

    private static final int LABEL_X = 8;
    private static final int BUTTON_X = 72;
    private static final int FIRST_ROW_Y = 4;
    private static final int ROW_SPACING = 22;

    private static final int BUTTON_BG_NORMAL = 0xFFFFFFFF;
    private static final int BUTTON_BG_SELECTED = 0xFF444444;

    public ItemHoloAssembler() {
        setMaxStackSize(1);
        setUnlocalizedName("holoassemblerar.holo_assembler");
    }

    @Override
    public List<ModuleBase> getModules(int ID, EntityPlayer player) {
        List<ModuleBase> modules = new LinkedList<>();
        List<ModuleBase> panelModules = new LinkedList<>();

        ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
        boolean isClient = player != null && player.world.isRemote;

        int y = FIRST_ROW_Y;

        panelModules.add(new ModuleText(LABEL_X, y + 4, "Inventory", TEXT_COLOR));
        panelModules.add(makeToggleButton(BTN_INVENTORY, BUTTON_X, y, useInventory(stack), "Inventory", isClient));
        y += ROW_SPACING;

        if (isEmcAvailable()) {
            panelModules.add(new ModuleText(LABEL_X, y + 4, "EMC", TEXT_COLOR));
            panelModules.add(makeToggleButton(BTN_EMC, BUTTON_X, y, useEmc(stack), "EMC", isClient));
            y += ROW_SPACING;
        }

        if (isMeAvailable()) {
            panelModules.add(new ModuleText(LABEL_X, y + 4, "ME", TEXT_COLOR));
            panelModules.add(makeToggleButton(BTN_ME, BUTTON_X, y, useMe(stack), "ME", isClient));
            y += ROW_SPACING;
        }

        if (HoloAssemblerConfig.enableDebugMode) {
            panelModules.add(new ModuleText(LABEL_X, y + 4, "Debug", TEXT_COLOR));
            panelModules.add(makeToggleButton(BTN_DEBUG, BUTTON_X, y, isDebugEnabled(stack), "Debug", isClient));
        }

        modules.add(new ModuleContainerPan(
                PANEL_X,
                PANEL_Y,
                panelModules,
                new LinkedList<>(),
                TextureResources.starryBG,
                PANEL_WIDTH,
                PANEL_HEIGHT,
                0,
                500
        ));

        return modules;
    }

    private ModuleButton makeToggleButton(int buttonId,
                                          int x,
                                          int y,
                                          boolean enabled,
                                          String label,
                                          boolean isClient) {
        if (isClient) {
            return new ModuleHoloAssemblerToggleButton(
                    x,
                    y,
                    buttonId,
                    label,
                    this
            );
        }

        ModuleButton button = new ModuleButton(
                x,
                y,
                buttonId,
                getButtonTextForId(buttonId, label, enabled),
                this,
                TextureResources.buttonBuild
        );

        button.setColor(enabled ? COLOR_ON : COLOR_OFF);
        button.setBGColor(enabled ? BUTTON_BG_SELECTED : BUTTON_BG_NORMAL);

        return button;
    }

    private static String getButtonTextForId(int buttonId, String label, boolean enabled) {
        return enabled ? "ON" : "OFF";
    }

    @SideOnly(Side.CLIENT)
    private class ModuleHoloAssemblerToggleButton extends ModuleButton {

        private final String label;

        public ModuleHoloAssemblerToggleButton(int offsetX,
                                               int offsetY,
                                               int buttonId,
                                               String label,
                                               IButtonInventory tile) {
            super(offsetX, offsetY, buttonId, label, tile, TextureResources.buttonBuild);
            this.label = label;
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

            if (button != null) {
                button.displayString = getButtonText(stack, enabled);
            }

            setColor(enabled ? COLOR_ON : COLOR_OFF);
            setBGColor(enabled ? BUTTON_BG_SELECTED : BUTTON_BG_NORMAL);

            super.renderForeground(guiOffsetX, guiOffsetY, mouseX, mouseY, zLevel, gui, font);
        }

        private boolean getToggleState(ItemStack stack) {
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

        private String getButtonText(ItemStack stack, boolean enabled) {
            return getButtonTextForId(buttonId, label, enabled);
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

        if (!(tile instanceof TileMultiBlock)) {
            return EnumActionResult.PASS;
        }

        TileMultiBlock multiblock = (TileMultiBlock) tile;
        Object[][][] structure = multiblock.getStructure();

        if (structure == null) {
            send(player, "No structure found.");
            return EnumActionResult.FAIL;
        }

        BlockPos offset = getControllerOffset(structure);

        if (offset == null) {
            send(player, "No controller marker found in structure.");
            return EnumActionResult.FAIL;
        }

        EnumFacing dir = BlockMultiblockMachine.getFront(world.getBlockState(controllerPos)).getOpposite();

        int missing = 0;
        int blocked = 0;
        int alreadyValid = 0;
        int ignored = 0;
        int placed = 0;
        int mePlaced = 0;
        int emcPlaced = 0;
        int missingItems = 0;

        Map<String, Integer> couldNotPlace = new LinkedHashMap<>();
        boolean useInventorySource = useInventory(heldStack);
        boolean useEmcSource = useEmc(heldStack) && isEmcAvailable();
        boolean useMeSource = useMe(heldStack) && isMeAvailable();

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
                        addCouldNotPlace(couldNotPlace, allowed);
                        continue;
                    }

                    missing++;

                    if (player.capabilities.isCreativeMode) {
                        if (placeFromInventory(world, targetPos, allowed, player)) {
                            placed++;
                        } else {
                            missingItems++;
                            addCouldNotPlace(couldNotPlace, allowed);
                        }

                        continue;
                    }

                    boolean didPlace = false;
                    if (useInventorySource) {
                        didPlace = placeFromInventory(world, targetPos, allowed, player);
                    }

                    if (didPlace) {
                        placed++;
                    } else if (useEmcSource && ProjectECompat.tryPlaceFromEMC(
                            player,
                            allowed,
                            (block, meta) -> placeBlock(world, targetPos, block, meta, player)
                    )) {
                        placed++;
                        emcPlaced++;
                    } else if (useMeSource && AE2Compat.tryPlaceFromME(
                            heldStack,
                            player,
                            allowed,
                            (block, meta) -> placeBlock(world, targetPos, block, meta, player)
                    )) {
                        placed++;
                        mePlaced++;
                    } else {
                        missingItems++;
                        addCouldNotPlace(couldNotPlace, allowed);
                    }
                }
            }
        }

        if (missing == 0) {
            send(player, "Structure complete. All required blocks are already correct.");
        } else if (couldNotPlace.isEmpty()) {
            send(player, "Assembly complete. Placed=" + placed
                    + formatSourcePlacedPart("EMC", emcPlaced, useEmcSource)
                    + formatSourcePlacedPart("ME", mePlaced, useMeSource)
                    + ". All required blocks are now correct.");
        } else {
            send(player, "Could not place: " + formatCouldNotPlace(couldNotPlace)
                    + ". Placed=" + placed
                    + formatSourcePlacedPart("EMC", emcPlaced, useEmcSource)
                    + formatSourcePlacedPart("ME", mePlaced, useMeSource)
                    + ", blocked=" + blocked
                    + ", missing sources=" + missingItems
                    + ", active sources=" + formatActiveSources(useInventorySource, useEmcSource, useMeSource)
                    + ".");
        }

        return EnumActionResult.SUCCESS;
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

            int wantedMeta = blockMeta.getMeta();

            if (player.capabilities.isCreativeMode) {
                int creativeMeta = isWildcardMeta(wantedMeta) ? 0 : wantedMeta;

                if (placeBlock(world, pos, block, creativeMeta, player)) {
                    return true;
                }
                continue;
            }

            int slot = findInventorySlot(player, block, wantedMeta);
            if (slot < 0) {
                continue;
            }

            ItemStack stack = player.inventory.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }

            int metaToPlace = isWildcardMeta(wantedMeta) ? stack.getMetadata() : wantedMeta;

            if (!placeBlock(world, pos, block, metaToPlace, player)) {
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
                                      int meta,
                                      EntityPlayer player) {
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

    private static int findInventorySlot(EntityPlayer player, Block block, int wantedMeta) {
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

            if (isWildcardMeta(wantedMeta) || stack.getMetadata() == wantedMeta) {
                return slot;
            }
        }

        return -1;
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

        for (BlockMeta allowedBlock : allowed) {
            if (allowedBlock == null) {
                continue;
            }

            if (allowedBlock.getBlock() != block) {
                continue;
            }

            int allowedMeta = allowedBlock.getMeta();

            if (allowedMeta == meta || isWildcardMeta(allowedMeta)) {
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
                return block.getLocalizedName();
            }

            int meta = blockMeta.getMeta();
            int displayMeta = isWildcardMeta(meta) ? 0 : meta;

            return new ItemStack(item, 1, displayMeta).getDisplayName();
        }

        return "Unknown block";
    }

    private static String formatCouldNotPlace(Map<String, Integer> map) {
        StringJoiner joiner = new StringJoiner(", ");

        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            joiner.add(entry.getKey() + " x" + entry.getValue());
        }

        return joiner.toString();
    }

    private static String formatSourcePlacedPart(String sourceName, int placed, boolean sourceAvailable) {
        if (!sourceAvailable || placed <= 0) {
            return "";
        }

        return ", " + sourceName + " placed=" + placed;
    }

    private static String formatActiveSources(boolean inventory, boolean emc, boolean me) {
        StringJoiner joiner = new StringJoiner(" > ");

        if (inventory) {
            joiner.add("Inventory");
        }

        if (emc) {
            joiner.add("EMC");
        }

        if (me) {
            joiner.add("ME");
        }

        String result = joiner.toString();
        return result.isEmpty() ? "None" : result;
    }

    private static void send(EntityPlayer player, String message) {
        player.sendMessage(new TextComponentString("[Holo-Assembler] " + message));
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
    }

    @Override
    public void readDataFromNetwork(ByteBuf in, byte packetId, NBTTagCompound nbt, @Nonnull ItemStack stack) {
        if (packetId == PACKET_SETTINGS) {
            nbt.setBoolean(NBT_USE_INVENTORY, in.readBoolean());
            nbt.setBoolean(NBT_USE_EMC, in.readBoolean());
            nbt.setBoolean(NBT_USE_ME, in.readBoolean());
            nbt.setBoolean(NBT_DEBUG, in.readBoolean());
        }
    }

    @Override
    public void useNetworkData(EntityPlayer player, Side side, byte id, NBTTagCompound nbt, @Nonnull ItemStack stack) {
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
    }

    private static String getSourcePriorityText(ItemStack stack) {
        StringJoiner joiner = new StringJoiner(" > ");
        if (useInventory(stack)) {
            joiner.add("Inventory");
        }
        if (useEmc(stack) && isEmcAvailable()) {
            joiner.add("EMC");
        }
        if (useMe(stack) && isMeAvailable()) {
            joiner.add("ME");
        }
        String text = joiner.toString();
        return text.isEmpty() ? "None" : text;
    }

    @Override
    public void addInformation(ItemStack stack,
                               @Nullable World world,
                               List<String> tooltip,
                               ITooltipFlag flag) {
        tooltip.add("Sources: " + getSourcePriorityText(stack));
        if (isMeAvailable()) {
            tooltip.add("ME Link: " + AE2Compat.getLinkText(stack));
        }
        tooltip.add(I18n.format("item.holoassemblerar.holo_assembler.tooltip"));
        tooltip.add("Right-click controller to assemble.");
        tooltip.add("Sneak-right-click to open settings.");
    }
}