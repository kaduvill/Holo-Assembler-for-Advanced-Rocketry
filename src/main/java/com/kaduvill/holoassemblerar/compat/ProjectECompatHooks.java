package com.kaduvill.holoassemblerar.compat;

import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.api.proxy.ITransmutationProxy;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import zmaster587.libVulpes.block.BlockMeta;

import java.util.List;

public final class ProjectECompatHooks {

    private ProjectECompatHooks() {
    }

    public static boolean tryPlaceFromEMC(EntityPlayer player,
                                          List<BlockMeta> allowed,
                                          ProjectECompat.BlockPlacer placer) {
        if (!(player instanceof EntityPlayerMP)) {
            return false;
        }

        IEMCProxy emcProxy = ProjectEAPI.getEMCProxy();
        ITransmutationProxy transmutationProxy = ProjectEAPI.getTransmutationProxy();

        if (emcProxy == null || transmutationProxy == null) {
            return false;
        }

        IKnowledgeProvider knowledge = transmutationProxy.getKnowledgeProviderFor(player.getUniqueID());

        if (knowledge == null) {
            return false;
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
                continue;
            }

            int wantedMeta = blockMeta.getMeta();
            int metaToPlace = isWildcardMeta(wantedMeta) ? 0 : wantedMeta;

            ItemStack emcStack = new ItemStack(item, 1, metaToPlace);

            if (!knowledge.hasKnowledge(emcStack)) {
                continue;
            }

            long cost = emcProxy.getValue(emcStack);

            if (cost <= 0) {
                continue;
            }

            long currentEmc = knowledge.getEmc();

            if (currentEmc < cost) {
                continue;
            }

            boolean placed = false;

            knowledge.setEmc(currentEmc - cost);

            try {
                placed = placer.place(block, metaToPlace);
            } finally {
                if (!placed) {
                    knowledge.setEmc(currentEmc);
                }

                knowledge.sync((EntityPlayerMP) player);
            }

            if (placed) {
                return true;
            }
        }

        return false;
    }

    private static boolean isWildcardMeta(int meta) {
        return meta == Short.MAX_VALUE || meta == 32767 || meta == -1;
    }
}