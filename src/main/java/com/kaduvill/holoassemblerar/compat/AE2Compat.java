package com.kaduvill.holoassemblerar.compat;

import com.kaduvill.holoassemblerar.config.HoloAssemblerConfig;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Loader;
import zmaster587.libVulpes.block.BlockMeta;

import java.lang.reflect.Method;
import java.util.List;

public final class AE2Compat {

    private static final String AE2_MODID = "appliedenergistics2";

    private static final String NBT_ENCRYPTION_KEY = "encryptionKey";
    private static final String NBT_NAME = "name";

    private static Method registerWirelessHandlerMethod;
    private static Method tryPlaceFromMeMethod;

    private AE2Compat() {
    }

    public interface BlockPlacer {
        boolean place(Block block, int meta);
    }

    public static boolean isEnabled() {
        return HoloAssemblerConfig.enableMeCompat && Loader.isModLoaded(AE2_MODID);
    }

    public static void registerWirelessHandler() {
        if (!isEnabled()) {
            return;
        }

        try {
            if (registerWirelessHandlerMethod == null) {
                Class<?> hooksClass = Class.forName("com.kaduvill.holoassemblerar.compat.AE2CompatHooks");
                registerWirelessHandlerMethod = hooksClass.getMethod("registerWirelessHandler");
            }

            registerWirelessHandlerMethod.invoke(null);
        } catch (Throwable ignored) {
        }
    }

    public static boolean tryPlaceFromME(ItemStack stack,
                                         EntityPlayer player,
                                         List<BlockMeta> allowed,
                                         BlockPlacer placer) {
        if (!isEnabled() || !hasLink(stack)) {
            return false;
        }

        try {
            if (tryPlaceFromMeMethod == null) {
                Class<?> hooksClass = Class.forName("com.kaduvill.holoassemblerar.compat.AE2CompatHooks");
                tryPlaceFromMeMethod = hooksClass.getMethod(
                        "tryPlaceFromME",
                        ItemStack.class,
                        EntityPlayer.class,
                        List.class,
                        BlockPlacer.class
                );
            }

            return (Boolean) tryPlaceFromMeMethod.invoke(null, stack, player, allowed, placer);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean hasLink(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && !tag.getString(NBT_ENCRYPTION_KEY).isEmpty();
    }

    public static String getLinkStatusLangKey(ItemStack stack) {
        if (!hasLink(stack)) {
            return "tooltip.holoassemblerar.me_link.not_linked";
        }

        String name = getLinkName(stack);

        if (name.isEmpty()) {
            return "tooltip.holoassemblerar.me_link.linked";
        }

        return "tooltip.holoassemblerar.me_link.named";
    }

    public static Object[] getLinkStatusLangArgs(ItemStack stack) {
        String name = getLinkName(stack);

        if (!hasLink(stack) || name.isEmpty()) {
            return new Object[0];
        }

        return new Object[] { name };
    }

    public static String getLinkName(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();

        if (tag == null) {
            return "";
        }

        String name = tag.getString(NBT_NAME);
        return name == null ? "" : name;
    }
}