package com.brandon3055.brandonscore;

import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.api.TimeKeeper;
import com.brandon3055.brandonscore.handlers.IProcess;
import com.brandon3055.brandonscore.handlers.ProcessHandler;
import com.brandon3055.brandonscore.network.BCoreNetwork;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * Created by Brandon on 14/5/2015.
 */
public class CommonProxy {

    public MinecraftServer getMCServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    public Level getClientWorld() {
        return null;
    }

    public boolean isCTRLKeyDown() {
        return false;
    }

    public Player getClientPlayer() {
        return null;
    }

    public void addProcess(IProcess iProcess) {
        ProcessHandler.addProcess(iProcess);
    }

    public void runSidedProcess(IProcess process) {
        ProcessHandler.addProcess(process);
    }

    public void sendIndexedMessage(Player player, Component message, int index) {
        BCoreNetwork.sendIndexedMessage((ServerPlayer) player, message, index);
    }

    public void setClipboardString(String text) {}
}
