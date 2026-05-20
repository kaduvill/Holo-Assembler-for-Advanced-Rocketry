package com.kaduvill.holoassemblerar.compat;

import com.kaduvill.holoassemblerar.config.HoloAssemblerConfig;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Loader;
import zmaster587.libVulpes.block.BlockMeta;

import java.lang.reflect.Method;
import java.util.List;

public final class ProjectECompat {

    private static final String PROJECTE_MODID = "projecte";
    private static Method tryPlaceFromEmcMethod;

    private ProjectECompat() {
    }

    public interface BlockPlacer {
        boolean place(Block block, int meta);
    }

    public static boolean tryPlaceFromEMC(EntityPlayer player,
                                          List<BlockMeta> allowed,
                                          BlockPlacer placer) {
        if (!HoloAssemblerConfig.enableEmcCompat) {
            return false;
        }

        if (!Loader.isModLoaded(PROJECTE_MODID)) {
            return false;
        }

        try {
            if (tryPlaceFromEmcMethod == null) {
                Class<?> hooksClass = Class.forName("com.kaduvill.holoassemblerar.compat.ProjectECompatHooks");
                tryPlaceFromEmcMethod = hooksClass.getMethod(
                        "tryPlaceFromEMC",
                        EntityPlayer.class,
                        List.class,
                        BlockPlacer.class
                );
            }

            return (Boolean) tryPlaceFromEmcMethod.invoke(null, player, allowed, placer);
        } catch (Throwable ignored) {
            return false;
        }
    }
}