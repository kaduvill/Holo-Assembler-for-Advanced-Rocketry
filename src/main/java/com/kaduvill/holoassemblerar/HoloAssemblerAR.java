package com.kaduvill.holoassemblerar;

import com.kaduvill.holoassemblerar.item.ItemHoloAssembler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;
import com.kaduvill.holoassemblerar.config.HoloAssemblerConfig;
import com.kaduvill.holoassemblerar.compat.AE2Compat;

@Mod(
        modid = HoloAssemblerAR.MOD_ID,
        name = HoloAssemblerAR.MOD_NAME,
        version = HoloAssemblerAR.VERSION,
        dependencies = HoloAssemblerAR.DEPENDENCIES,
        acceptedMinecraftVersions = "[1.12.2]"
)
@Mod.EventBusSubscriber(modid = HoloAssemblerAR.MOD_ID)
public class HoloAssemblerAR {

    public static final String MOD_ID = "holoassemblerar";
    public static final String MOD_NAME = "Holo-Assembler for Advanced Rocketry";
    public static final String VERSION = "GRADLETOKEN_VERSION";

    public static final String DEPENDENCIES =
            "required-after:libvulpes;" +
                    "required-after:advancedrocketry;" +
                    "after:projecte;" +
                    "after:appliedenergistics2";

    public static final ItemHoloAssembler HOLO_ASSEMBLER = new ItemHoloAssembler();

    public static final CreativeTabs TAB = new CreativeTabs(MOD_ID) {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(HOLO_ASSEMBLER);
        }
    };

    @Mod.Instance(MOD_ID)
    public static HoloAssemblerAR instance;

    public static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        HoloAssemblerConfig.load(event.getSuggestedConfigurationFile());
        logger.info("{} loaded", MOD_NAME);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        HOLO_ASSEMBLER.setRegistryName(new ResourceLocation(MOD_ID, "holo_assembler"));
        HOLO_ASSEMBLER.setCreativeTab(TAB);
        event.getRegistry().register(HOLO_ASSEMBLER);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (Loader.isModLoaded("appliedenergistics2")) {
            AE2Compat.registerWirelessHandler();
        }
    }
}