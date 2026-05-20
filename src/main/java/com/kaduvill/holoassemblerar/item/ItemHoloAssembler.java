package com.kaduvill.holoassemblerar.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.resources.I18n;
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
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import zmaster587.libVulpes.block.BlockMeta;
import zmaster587.libVulpes.block.multiblock.BlockMultiblockMachine;
import zmaster587.libVulpes.tile.multiblock.TileMultiBlock;
import com.kaduvill.holoassemblerar.compat.ProjectECompat;

import javax.annotation.Nullable;
import java.util.List;

public class ItemHoloAssembler extends Item {

    public ItemHoloAssembler() {
        setMaxStackSize(1);
        setUnlocalizedName("holoassemblerar.holo_assembler");
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
                cycleMode(heldStack, player);
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
        int emcPlaced = 0;
        int missingItems = 0;

        boolean assemble = isAssembleMode(heldStack);

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

                    missing++;

                    if (assemble) {
                        if (placeFromInventory(world, targetPos, allowed, player)) {
                            placed++;
                        } else if (ProjectECompat.tryPlaceFromEMC(
                                player,
                                allowed,
                                (block, meta) -> placeBlock(world, targetPos, block, meta, player)
                        )) {
                            placed++;
                            emcPlaced++;
                        } else {
                            missingItems++;
                        }
                    }
                }
            }
        }

        if (assemble) {
            send(player, "Assembly: placed=" + placed
                    + ", EMC placed=" + emcPlaced
                    + ", missing items=" + missingItems
                    + ", blocked=" + blocked
                    + ", already valid=" + alreadyValid + ".");
        } else {
            send(player, "Structure check: missing=" + missing
                    + ", blocked=" + blocked
                    + ", already valid=" + alreadyValid
                    + ", ignored=" + ignored + ".");
        }

        return EnumActionResult.SUCCESS;
    }

    private static boolean isAssembleMode(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();

        if (tag == null || !tag.hasKey("Mode")) {
            return true; // Default mode: Assemble
        }

        return "assemble".equals(tag.getString("Mode"));
    }

    private static void cycleMode(ItemStack stack, EntityPlayer player) {
        NBTTagCompound tag = stack.getTagCompound();

        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }

        boolean currentlyAssemble = isAssembleMode(stack);

        if (currentlyAssemble) {
            tag.setString("Mode", "preview");
            send(player, "Mode set to: Preview.");
        } else {
            tag.setString("Mode", "assemble");
            send(player, "Mode set to: Assemble.");
        }
    }

    private static String getModeName(ItemStack stack) {
        return isAssembleMode(stack) ? "Assemble" : "Preview";
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
    public ActionResult<ItemStack> onItemRightClick(World world,
                                                    EntityPlayer player,
                                                    EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (!player.isSneaking()) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }

        if (!world.isRemote) {
            cycleMode(stack, player);
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    private static boolean isWildcardMeta(int meta) {
        return meta == Short.MAX_VALUE || meta == 32767 || meta == -1;
    }

    private static void send(EntityPlayer player, String message) {
        player.sendMessage(new TextComponentString("[Holo-Assembler] " + message));
    }

    @Override
    public void addInformation(ItemStack stack,
                               @Nullable World world,
                               List<String> tooltip,
                               ITooltipFlag flag) {
        tooltip.add("Mode: " + getModeName(stack));
        tooltip.add(I18n.format("item.holoassemblerar.holo_assembler.tooltip"));
        tooltip.add("Right-click an Advanced Rocketry multiblock controller to use current mode.");
        tooltip.add("Sneak-right-click to cycle Preview / Assemble.");
    }
}