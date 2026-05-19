package com.kaduvill.holoassemblerar.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import zmaster587.libVulpes.block.BlockMeta;
import zmaster587.libVulpes.block.multiblock.BlockMultiblockMachine;
import zmaster587.libVulpes.tile.multiblock.TileMultiBlock;

import javax.annotation.Nullable;
import java.util.List;

public class ItemHoloAssembler extends Item {

    private static final int MAX_BLOCKS_PER_USE = 64;

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
        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        }

        TileEntity tile = world.getTileEntity(controllerPos);

        if (!(tile instanceof TileMultiBlock)) {
            send(player, "Not an Advanced Rocketry multiblock controller.");
            return EnumActionResult.FAIL;
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
        int missingItems = 0;

        boolean assemble = player.isSneaking();

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

                    IBlockState existing = world.getBlockState(targetPos);

                    if (!world.isAirBlock(targetPos) && !existing.getBlock().isReplaceable(world, targetPos)) {
                        blocked++;
                        continue;
                    }

                    missing++;

                    if (assemble && placed < MAX_BLOCKS_PER_USE) {
                        if (placeFromInventory(world, targetPos, allowed, player)) {
                            placed++;
                        } else {
                            missingItems++;
                        }
                    }
                }
            }
        }

        if (assemble) {
            send(player, "Assembly: placed=" + placed
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
            int slot = findInventorySlot(player, block, wantedMeta);

            if (slot < 0) {
                continue;
            }

            ItemStack stack = player.inventory.getStackInSlot(slot);

            if (stack.isEmpty()) {
                continue;
            }

            int metaToPlace = isWildcardMeta(wantedMeta) ? stack.getMetadata() : wantedMeta;

            IBlockState state;

            try {
                state = block.getStateFromMeta(metaToPlace);
            } catch (Exception ignored) {
                state = block.getDefaultState();
            }

            if (!world.mayPlace(block, pos, false, EnumFacing.UP, player)) {
                continue;
            }

            if (!world.setBlockState(pos, state, 3)) {
                continue;
            }

            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);

                if (stack.getCount() <= 0) {
                    player.inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
                }

                player.inventory.markDirty();
            }

            return true;
        }

        return false;
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
        tooltip.add(I18n.format("item.holoassemblerar.holo_assembler.tooltip"));
        tooltip.add(I18n.format("item.holoassemblerar.holo_assembler.tooltip.preview"));
        tooltip.add(I18n.format("item.holoassemblerar.holo_assembler.tooltip.sneak"));
    }
}