package com.kaduvill.holoassemblerar.client;

import com.kaduvill.holoassemblerar.HoloAssemblerAR;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = HoloAssemblerAR.MOD_ID, value = Side.CLIENT)
public final class ClientEventHandler {

    private ClientEventHandler() {
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(
                HoloAssemblerAR.HOLO_ASSEMBLER,
                0,
                new ModelResourceLocation(HoloAssemblerAR.MOD_ID + ":holo_assembler", "inventory")
        );
    }
}