package com.brandon3055.brandonscore.network;

import codechicken.lib.packet.PacketCustom;
import codechicken.lib.packet.PacketCustomChannelBuilder;
import codechicken.lib.util.ServerUtils;
import codechicken.lib.vec.Vector3;
import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.handlers.FileHandler;
import com.brandon3055.brandonscore.handlers.contributor.ContributorConfig;
import com.brandon3055.brandonscore.handlers.contributor.ContributorProperties;
import com.brandon3055.brandonscore.multiblock.MultiBlockDefinition;
import com.brandon3055.brandonscore.multiblock.MultiBlockManager;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.google.gson.JsonParser;
import com.mojang.math.Vector3f;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.event.EventNetworkChannel;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Created by brandon3055 on 17/12/19.
 */
public class BCoreNetwork {
    private static final CrashLock LOCK = new CrashLock("Already Initialized.");

    public static final ResourceLocation CHANNEL = new ResourceLocation(BrandonsCore.MODID + ":network");
    public static EventNetworkChannel netChannel;

    //Server to client
    public static final int C_TILE_DATA_MANAGER = 1;
    public static final int C_TILE_MESSAGE = 2;
    public static final int C_NO_CLIP = 4;
    public static final int C_PLAYER_ACCESS = 5;
    public static final int C_PLAYER_ACCESS_UPDATE = 6;
    public static final int C_INDEXED_MESSAGE = 7;
    public static final int C_TILE_CAP_DATA = 8;
    public static final int C_PLAY_SOUND = 9;
    public static final int C_SPAWN_ENTITY = 10;
    public static final int C_SPAWN_PARTICLE = 11;
    public static final int C_ENTITY_VELOCITY = 12;
    public static final int C_OPEN_HUD_CONFIG = 13;
    public static final int C_MULTI_BLOCK_DEFINITIONS = 14;
    public static final int C_CONTRIBUTOR_CONFIG = 15;
    //Client to server
    public static final int S_CONTAINER_MESSAGE = 1;
    public static final int S_PLAYER_ACCESS_BUTTON = 2;
    public static final int S_TILE_DATA_MANAGER = 3;
    public static final int S_CONTRIBUTOR_CONFIG = 4;
    public static final int S_CONTRIBUTOR_LINK = 5;

    public static final int S_DUMMY_PACKET = 99;

    public static void sendNoClip(ServerPlayer player, boolean enabled) {
        PacketCustom packet = new PacketCustom(CHANNEL, C_NO_CLIP);
        packet.writeBoolean(enabled);
        packet.sendToPlayer(player);
        LogHelperBC.dev("Sending NoClip update to player: " + player + " Enabled: " + enabled);
    }

    public static void sendOpenPlayerAccessUI(ServerPlayer player, int windowID) {
        PacketCustom packet = new PacketCustom(CHANNEL, C_PLAYER_ACCESS);
        packet.writeInt(windowID);
        packet.sendToPlayer(player);
    }

    public static void sendPlayerAccessUIUpdate(ServerPlayer player, Player target) {
        PacketCustom packet = new PacketCustom(CHANNEL, C_PLAYER_ACCESS_UPDATE);
        packet.writeString(target.getGameProfile().getName());
        packet.writePos(target.blockPosition());
//        packet.writeInt(target.dimension.getId());
//        packet.sendToPlayer(player);
    }

    public static void sendPlayerAccessButton(int button) {
        PacketCustom packet = new PacketCustom(CHANNEL, S_PLAYER_ACCESS_BUTTON);
        packet.writeByte(button);
        packet.sendToServer();
    }

    public static void sendIndexedMessage(ServerPlayer player, Component message, int index) {
        PacketCustom packet = new PacketCustom(CHANNEL, C_INDEXED_MESSAGE);
        packet.writeTextComponent(message);
        packet.writeInt(index);
        packet.sendToPlayer(player);
    }

    public static void sendSound(Level world, int x, int y, int z, SoundEvent sound, SoundSource category, float volume, float pitch, boolean distanceDelay) {
        sendSound(world, new BlockPos(x, y, z), sound, category, volume, pitch, distanceDelay);
    }

    public static void sendSound(Level world, Entity entity, SoundEvent sound, SoundSource category, float volume, float pitch, boolean distanceDelay) {
        sendSound(world, entity.blockPosition(), sound, category, volume, pitch, distanceDelay);
    }

    public static void sendSound(Level world, BlockPos pos, SoundEvent sound, SoundSource category, float volume, float pitch, boolean distanceDelay) {
        if (!world.isClientSide) {
            PacketCustom packet = new PacketCustom(CHANNEL, C_PLAY_SOUND);
            packet.writePos(pos);
            packet.writeRegistryId(sound);
            packet.writeVarInt(category.ordinal());
            packet.writeFloat(volume);
            packet.writeFloat(pitch);
            packet.writeBoolean(distanceDelay);
            packet.sendToChunk(world, pos);
        }
    }

    public static void sendParticle(Level world, ParticleOptions particleData, Vector3 pos, Vector3 motion, boolean distanceOverride) {
        if (!world.isClientSide) {
            PacketCustom packet = new PacketCustom(CHANNEL, C_SPAWN_PARTICLE);
            packet.writeRegistryId(particleData.getType());
            particleData.writeToNetwork(packet.toPacketBuffer());
            packet.writeVector(pos);
            packet.writeVector(motion);
            packet.writeBoolean(distanceOverride);
            packet.sendToChunk(world, pos.pos());
        }
    }

    /**
     * This is a custom entity spawn packet that removes the min/max velocity constraints.
     *
     * @param entity The entity being spawned.
     * @return A packet to return in {@link Entity#getAddEntityPacket()}
     */
    public static Packet<?> getEntitySpawnPacket(Entity entity) {
        PacketCustom packet = new PacketCustom(CHANNEL, C_SPAWN_ENTITY);
        packet.writeVarInt(Registry.ENTITY_TYPE.getId(entity.getType()));
        packet.writeInt(entity.getId());
        packet.writeUUID(entity.getUUID());
        packet.writeDouble(entity.getX());
        packet.writeDouble(entity.getY());
        packet.writeDouble(entity.getZ());
        packet.writeByte((byte) Mth.floor(entity.getXRot() * 256.0F / 360.0F));
        packet.writeByte((byte) Mth.floor(entity.getYRot() * 256.0F / 360.0F));
        packet.writeByte((byte) (entity.getYHeadRot() * 256.0F / 360.0F));
        Vec3 velocity = entity.getDeltaMovement();
        packet.writeFloat((float) velocity.x);
        packet.writeFloat((float) velocity.y);
        packet.writeFloat((float) velocity.z);
        return packet.toPacket(NetworkDirection.PLAY_TO_CLIENT);
    }

    public static Packet<?> sendEntityVelocity(Entity entity, boolean movement) {
        PacketCustom packet = new PacketCustom(CHANNEL, C_ENTITY_VELOCITY);
        packet.writeInt(entity.getId());
        packet.writeVec3f(new Vector3f(entity.getDeltaMovement()));
        packet.writeBoolean(movement);
        if (movement) {
            packet.writeFloat(entity.getXRot());
            packet.writeFloat(entity.getYRot());
            packet.writeBoolean(entity.isOnGround());
        }
        return packet.toPacket(NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendOpenHudConfig(ServerPlayer player) {
        new PacketCustom(CHANNEL, C_OPEN_HUD_CONFIG).sendToPlayer(player);
    }

    public static void sendMultiBlockDefinitions(ServerPlayer player, Map<ResourceLocation, MultiBlockDefinition> multiBlockMap) {
        PacketCustom packet = new PacketCustom(CHANNEL, C_MULTI_BLOCK_DEFINITIONS);
        packet.writeVarInt(multiBlockMap.size());
        multiBlockMap.forEach((key, value) -> {
            packet.writeResourceLocation(key);
            packet.writeString(MultiBlockManager.GSON.toJson(value.getJson()));
        });
        packet.sendToPlayer(player);
    }

    public static void sendContributorConfigToServer(ContributorProperties props) {
        PacketCustom packet = new PacketCustom(CHANNEL, S_CONTRIBUTOR_CONFIG);
        if (props.isContributor()) {
            props.getConfig().serialize(packet);
            packet.sendToServer();
        }
//        BrandonsCore.LOGGER.info("sendContributorConfigToServer");
    }

    public static PacketCustom contributorConfigToClient(ContributorProperties props) {
        PacketCustom packet = new PacketCustom(CHANNEL, C_CONTRIBUTOR_CONFIG);
        packet.writeUUID(props.getUserID());
        props.getConfig().serialize(packet);
        return packet;
    }

    public static void sendContribLinkToServer() {
        new PacketCustom(CHANNEL, S_CONTRIBUTOR_LINK).sendToServer();
    }

    public static void sentToAllExcept(PacketCustom packet, Player exclude) {
        for (ServerPlayer player : ServerUtils.getPlayers()) {
            if (!player.getUUID().equals(exclude.getUUID())) {
                packet.sendToPlayer(player);
            }
        }
    }

    public static void init() {
        LOCK.lock();

        netChannel = PacketCustomChannelBuilder.named(CHANNEL)
                .networkProtocolVersion(() -> "1")
                .clientAcceptedVersions(e -> true)
                .serverAcceptedVersions(e -> true)
                .assignClientHandler(() -> ClientPacketHandler::new)
                .assignServerHandler(() -> ServerPacketHandler::new)
                .build();
    }
}
