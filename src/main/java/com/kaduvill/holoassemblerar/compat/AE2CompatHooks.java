package com.kaduvill.holoassemblerar.compat;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.features.ILocatable;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IConfigManager;
import appeng.me.helpers.PlayerSource;
import appeng.util.ConfigManager;
import com.kaduvill.holoassemblerar.HoloAssemblerAR;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.IGuiHandler;
import zmaster587.libVulpes.block.BlockMeta;

import java.util.List;

public final class AE2CompatHooks {

    private static boolean registered;

    private AE2CompatHooks() {
    }

    public static void registerWirelessHandler() {
        if (registered) {
            return;
        }

        AEApi.instance()
                .registries()
                .wireless()
                .registerWirelessHandler(new HoloAssemblerWirelessHandler());

        registered = true;
    }

    public static boolean tryPlaceFromME(ItemStack toolStack,
                                         EntityPlayer player,
                                         List<BlockMeta> allowed,
                                         AE2Compat.BlockPlacer placer) {
        String encryptionKey = getEncryptionKey(toolStack);

        if (encryptionKey.isEmpty()) {
            return false;
        }

        long parsedKey;

        try {
            parsedKey = Long.parseLong(encryptionKey);
        } catch (NumberFormatException ignored) {
            return false;
        }

        ILocatable locatable = AEApi.instance()
                .registries()
                .locatable()
                .getLocatableBy(parsedKey);

        if (!(locatable instanceof IGridHost)) {
            return false;
        }

        IGridNode node = findActiveNode((IGridHost) locatable);

        if (node == null) {
            return false;
        }

        IGrid grid = node.getGrid();

        if (grid == null) {
            return false;
        }

        IEnergyGrid energyGrid = grid.getCache(IEnergyGrid.class);

        if (energyGrid == null || !energyGrid.isNetworkPowered()) {
            return false;
        }

        IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);

        if (storageGrid == null) {
            return false;
        }

        IItemStorageChannel itemChannel = AEApi.instance()
                .storage()
                .getStorageChannel(IItemStorageChannel.class);

        IMEMonitor<IAEItemStack> inventory = storageGrid.getInventory(itemChannel);

        if (inventory == null) {
            return false;
        }

        PlayerSource source = new PlayerSource(player, null);

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
                continue;
            }

            int wantedMeta = blockMeta.getMeta();
            int metaToPlace = isWildcardMeta(wantedMeta) ? 0 : wantedMeta;

            ItemStack requestStack = new ItemStack(item, 1, metaToPlace);
            IAEItemStack request = itemChannel.createStack(requestStack);

            if (request == null) {
                continue;
            }

            request.setStackSize(1);

            IAEItemStack simulated = AEApi.instance().storage().poweredExtraction(
                    energyGrid,
                    inventory,
                    request,
                    source,
                    Actionable.SIMULATE
            );

            if (simulated == null || simulated.getStackSize() < 1) {
                continue;
            }

            IAEItemStack extracted = AEApi.instance().storage().poweredExtraction(
                    energyGrid,
                    inventory,
                    request,
                    source,
                    Actionable.MODULATE
            );

            if (extracted == null || extracted.getStackSize() < 1) {
                continue;
            }

            if (placer.place(block, metaToPlace)) {
                return true;
            }

            IAEItemStack refund = extracted.copy();
            refund.setStackSize(1);

            AEApi.instance().storage().poweredInsert(
                    energyGrid,
                    inventory,
                    refund,
                    source,
                    Actionable.MODULATE
            );
        }

        return false;
    }

    private static IGridNode findActiveNode(IGridHost host) {
        for (AEPartLocation side : AEPartLocation.values()) {
            IGridNode node = host.getGridNode(side);

            if (node != null && node.isActive()) {
                return node;
            }
        }

        return null;
    }

    private static String getEncryptionKey(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();

        if (tag == null) {
            return "";
        }

        return tag.getString("encryptionKey");
    }

    private static NBTTagCompound getOrCreateTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();

        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }

        return tag;
    }

    private static boolean isWildcardMeta(int meta) {
        return meta == Short.MAX_VALUE || meta == 32767 || meta == -1;
    }

    private static final class HoloAssemblerWirelessHandler implements IWirelessTermHandler {

        @Override
        public boolean canHandle(ItemStack stack) {
            return stack != null
                    && !stack.isEmpty()
                    && stack.getItem() == HoloAssemblerAR.HOLO_ASSEMBLER;
        }

        @Override
        public boolean usePower(EntityPlayer player, double amount, ItemStack stack) {
            return true;
        }

        @Override
        public boolean hasPower(EntityPlayer player, double amount, ItemStack stack) {
            return true;
        }

        @Override
        public IConfigManager getConfigManager(ItemStack stack) {
            return new ConfigManager((manager, settingName, newValue) -> {
            });
        }

        @Override
        public IGuiHandler getGuiHandler(ItemStack stack) {
            return null;
        }

        @Override
        public String getEncryptionKey(ItemStack stack) {
            NBTTagCompound tag = stack.getTagCompound();

            if (tag == null) {
                return "";
            }

            return tag.getString("encryptionKey");
        }

        @Override
        public void setEncryptionKey(ItemStack stack, String encKey, String name) {
            NBTTagCompound tag = getOrCreateTag(stack);

            tag.setString("encryptionKey", encKey);

            if (name != null) {
                tag.setString("name", name);
            }
        }
    }
}