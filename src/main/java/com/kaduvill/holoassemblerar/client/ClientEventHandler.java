package com.kaduvill.holoassemblerar.client;

import com.kaduvill.holoassemblerar.HoloAssemblerAR;
import com.kaduvill.holoassemblerar.item.ItemHoloAssembler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

import java.util.List;

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

    @SubscribeEvent
    public static void renderAssemblerPreview(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.player == null || mc.world == null) {
            return;
        }

        EntityPlayer player = mc.player;
        ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);

        List<AxisAlignedBB> boxes = ItemHoloAssembler.getClientPreviewBoxes(mc.world, player, stack);

        if (boxes.isEmpty()) {
            return;
        }

        Entity view = mc.getRenderViewEntity();

        if (view == null) {
            return;
        }

        double partialTicks = event.getPartialTicks();
        double viewX = view.lastTickPosX + (view.posX - view.lastTickPosX) * partialTicks;
        double viewY = view.lastTickPosY + (view.posY - view.lastTickPosY) * partialTicks;
        double viewZ = view.lastTickPosZ + (view.posZ - view.lastTickPosZ) * partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.glLineWidth(2.5F);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        for (AxisAlignedBB box : boxes) {
            drawRedFrame(
                    buffer,
                    box.grow(0.002D).offset(-viewX, -viewY, -viewZ)
            );
        }

        tessellator.draw();

        GlStateManager.glLineWidth(1.0F);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    private static void drawRedFrame(BufferBuilder buffer, AxisAlignedBB box) {
        double minX = box.minX;
        double minY = box.minY;
        double minZ = box.minZ;
        double maxX = box.maxX;
        double maxY = box.maxY;
        double maxZ = box.maxZ;

        addLine(buffer, minX, minY, minZ, maxX, minY, minZ);
        addLine(buffer, maxX, minY, minZ, maxX, minY, maxZ);
        addLine(buffer, maxX, minY, maxZ, minX, minY, maxZ);
        addLine(buffer, minX, minY, maxZ, minX, minY, minZ);

        addLine(buffer, minX, maxY, minZ, maxX, maxY, minZ);
        addLine(buffer, maxX, maxY, minZ, maxX, maxY, maxZ);
        addLine(buffer, maxX, maxY, maxZ, minX, maxY, maxZ);
        addLine(buffer, minX, maxY, maxZ, minX, maxY, minZ);

        addLine(buffer, minX, minY, minZ, minX, maxY, minZ);
        addLine(buffer, maxX, minY, minZ, maxX, maxY, minZ);
        addLine(buffer, maxX, minY, maxZ, maxX, maxY, maxZ);
        addLine(buffer, minX, minY, maxZ, minX, maxY, maxZ);
    }

    private static void addLine(BufferBuilder buffer,
                                double x1,
                                double y1,
                                double z1,
                                double x2,
                                double y2,
                                double z2) {
        buffer.pos(x1, y1, z1).color(1.0F, 0.0F, 0.0F, 0.85F).endVertex();
        buffer.pos(x2, y2, z2).color(1.0F, 0.0F, 0.0F, 0.85F).endVertex();
    }
}