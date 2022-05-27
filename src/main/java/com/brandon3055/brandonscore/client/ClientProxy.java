package com.brandon3055.brandonscore.client;

import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.CommonProxy;
import com.brandon3055.brandonscore.api.TimeKeeper;
import com.brandon3055.brandonscore.client.hud.HudManager;
import com.brandon3055.brandonscore.handlers.IProcess;
import com.brandon3055.brandonscore.lib.DLRSCache;
import com.brandon3055.brandonscore.utils.BCProfiler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Created by Brandon on 14/5/2015.
 */
public class ClientProxy extends CommonProxy {

    @Override
    public void construct() {
        super.construct();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(BCGuiSprites::initialize);
//        MinecraftForge.EVENT_BUS.addListener(this::registerShaderReloads);
        HudManager.init();

        BCShaders.init();
    }

//    private void registerShaderReloads(ParticleFactoryRegisterEvent event) {
//        if (Minecraft.getInstance() != null && BCConfig.useShaders) {
//            ResourceUtils.registerReloadListener(GuiEnergyBar.barShaderH);
//            ResourceUtils.registerReloadListener(GuiEnergyBar.barShaderV);
//        }
//    }

    @Override
    public void commonSetup(FMLCommonSetupEvent event) {
        super.commonSetup(event);
//        ((ReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(new ModelUtils());
        MinecraftForge.EVENT_BUS.register(new BCClientEventHandler());
        DLRSCache.initialize();
        ProcessHandlerClient.init();
        BCProfiler.init();
    }

    @Override
    public void clientSetup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(CursorHelper::closeGui);
    }


    @Override
    public MinecraftServer getMCServer() {
        return super.getMCServer();
    }

    @Override
    public Level getClientWorld() {
        return Minecraft.getInstance().level;
    }

    @Override
    public boolean isJumpKeyDown() {
        return Minecraft.getInstance().options.keyJump.isDown();
    }

    @Override
    public boolean isSneakKeyDown() {
        return Minecraft.getInstance().options.keyShift.isDown();
    }

    @Override
    public boolean isSprintKeyDown() {
        return Minecraft.getInstance().options.keySprint.isDown();
    }

    @Override
    public boolean isCTRLKeyDown() {
        return Screen.hasControlDown();
    }

    @Override
    public Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    @Override
    public void addProcess(IProcess iProcess) {
        ProcessHandlerClient.addProcess(iProcess);
    }

    @Override
    public void runSidedProcess(IProcess process) {
        ProcessHandlerClient.addProcess(process);
    }

    @Override
    public int tickTimer() {
        return TimeKeeper.getClientTick();
    }

    @Override
    public void sendIndexedMessage(Player player, Component message, int index) {
        if (message == null) {
            Minecraft.getInstance().gui.getChat().removeById(index);
        } else {
            Minecraft.getInstance().gui.getChat().addMessage(message, index);
        }
    }

    @Override
    public void setClipboardString(String text) {
        Minecraft.getInstance().keyboardHandler.setClipboard(text);
    }

    @Override
    public void sendToServer(PacketCustom packet) {
        packet.sendToServer();
    }
}
