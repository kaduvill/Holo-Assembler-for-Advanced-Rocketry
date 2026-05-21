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
                    "If true, and ProjectE is loaded, the Holo-Assembler can spend player EMC to place missing multiblock blocks. The player must have learned the item and have enough EMC."
            );

            enableMeCompat = config.getBoolean(
                    "enableMeCompat",
                    "compat",
                    true,
                    "If true, and Applied Energistics 2 is loaded, the Holo-Assembler can pull stored blocks from a linked ME network."
            );

            enableDebugMode = config.getBoolean(
                    "enableDebugMode",
                    "debug",
                    false,
                    "If true, shows the Debug toggle in the Holo-Assembler GUI. When enabled on the item, chat feedback includes detailed placement counts and active source information."
            );
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
}