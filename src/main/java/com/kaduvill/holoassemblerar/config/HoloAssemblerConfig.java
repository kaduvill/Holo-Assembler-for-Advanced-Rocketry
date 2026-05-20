package com.kaduvill.holoassemblerar.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public final class HoloAssemblerConfig {

    public static boolean enableEmcCompat = true;
    public static boolean enableMeCompat = true;
    public static boolean enableDebugMode = false;

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
            enableMeCompat = config.getBoolean(
                    "enableMeCompat",
                    "compat",
                    true,
                    "If true, and Applied Energistics 2 is loaded, the Holo-Assembler can use ME-network sources. Currently only enables the GUI toggle until ME extraction is implemented."
            );

            enableDebugMode = config.getBoolean(
                    "enableDebugMode",
                    "debug",
                    false,
                    "If true, shows the Debug toggle in the Holo-Assembler GUI."
            );
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
}