package com.kaduvill.holoassemblerar.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public final class HoloAssemblerConfig {

    public static boolean enableEmcCompat = true;

    private HoloAssemblerConfig() {
    }

    public static void load(File file) {
        Configuration config = new Configuration(file);

        try {
            enableEmcCompat = config.getBoolean(
                    "enableEmcCompat",
                    "compat",
                    true,
                    "If true, and ProjectE is loaded, the Holo-Assembler can spend player EMC to place missing multiblock blocks when inventory items are unavailable."
            );
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
}