package com.kaduvill.holoassemblerar.compat;

import com.kaduvill.holoassemblerar.config.HoloAssemblerConfig;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Loader;
import zmaster587.libVulpes.block.BlockMeta;

import java.lang.reflect.Method;
import java.util.List;

public final class AE2Compat {

    private static final String AE2_MODID = "appliedenergistics2";

    private static final String NBT_ME_LINKED = "MeLinked";
    private static final String NBT_ME_DIM = "MeDim";
    private static final String NBT_ME_X = "MeX";
    private static final String NBT_ME_Y = "MeY";
    private static final String NBT_ME_Z = "MeZ";

    private static Method canLinkMethod;
    private static Method tryPlaceFromMeMethod;

    private AE2Compat() {
    }

    public interface BlockPlacer {
        boolean place(Block block, int meta);
    }

    public static boolean isEnabled() {
        return HoloAssemblerConfig.enableMeCompat && Loader.isModLoaded(AE2_MODID);
    }

    public static boolean tryLink(ItemStack stack, World world, BlockPos pos) {
        if (!isEnabled() || world == null || world.isRemote) {
            return false;
        }

        try {
            if (canLinkMethod == null) {
                Class<?> hooksClass = Class.forName("com.kaduvill.holoassemblerar.compat.AE2CompatHooks");
                canLinkMethod = hooksClass.getMethod("canLink", World.class, BlockPos.class);
            }

            boolean canLink = (Boolean) canLinkMethod.invoke(null, world, pos);

            if (!canLink) {
                return false;
            }

            NBTTagCompound tag = getOrCreateTag(stack);
            tag.setBoolean(NBT_ME_LINKED, true);
            tag.setInteger(NBT_ME_DIM, world.provider.getDimension());
            tag.setInteger(NBT_ME_X, pos.getX());
            tag.setInteger(NBT_ME_Y, pos.getY());
            tag.setInteger(NBT_ME_Z, pos.getZ());

            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean tryPlaceFromME(ItemStack stack,
                                         EntityPlayer player,
                                         List<BlockMeta> allowed,
                                         BlockPlacer placer) {
        if (!isEnabled() || !hasLink(stack)) {
            return false;
        }

        NBTTagCompound tag = stack.getTagCompound();

        if (tag == null) {
            return false;
        }

        int dim = tag.getInteger(NBT_ME_DIM);
        WorldServer linkedWorld = DimensionManager.getWorld(dim);

        if (linkedWorld == null) {
            return false;
        }

        BlockPos linkPos = new BlockPos(
                tag.getInteger(NBT_ME_X),
                tag.getInteger(NBT_ME_Y),
                tag.getInteger(NBT_ME_Z)
        );

        try {
            if (tryPlaceFromMeMethod == null) {
                Class<?> hooksClass = Class.forName("com.kaduvill.holoassemblerar.compat.AE2CompatHooks");
                tryPlaceFromMeMethod = hooksClass.getMethod(
                        "tryPlaceFromME",
                        World.class,
                        BlockPos.class,
                        EntityPlayer.class,
                        List.class,
                        BlockPlacer.class
                );
            }

            return (Boolean) tryPlaceFromMeMethod.invoke(null, linkedWorld, linkPos, player, allowed, placer);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean hasLink(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.getBoolean(NBT_ME_LINKED);
    }

    public static String getLinkText(ItemStack stack) {
        if (!hasLink(stack)) {
            return "Not linked";
        }

        NBTTagCompound tag = stack.getTagCompound();

        if (tag == null) {
            return "Not linked";
        }

        return "dim " + tag.getInteger(NBT_ME_DIM)
                + " @ "
                + tag.getInteger(NBT_ME_X)
                + ", "
                + tag.getInteger(NBT_ME_Y)
                + ", "
                + tag.getInteger(NBT_ME_Z);
    }

    private static NBTTagCompound getOrCreateTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();

        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }

        return tag;
    }
}