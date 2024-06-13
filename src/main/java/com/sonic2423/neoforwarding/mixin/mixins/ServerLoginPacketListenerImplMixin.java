package com.sonic2423.neoforwarding.mixin.mixins;

import com.mojang.authlib.GameProfile;
import com.sonic2423.neoforwarding.Config;
import com.sonic2423.neoforwarding.PlayerDataForwarding;
import com.sonic2423.neoforwarding.mixin.ISetAddressInConnection;
import net.minecraft.network.Connection;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ServerLoginPacketListener;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

import static com.sonic2423.neoforwarding.NeoForwarding.LOGGER;

/*
 * The following is ported from "Paper" with slight modifications to work with NeoForge as Mixin.
 * See: https://github.com/PaperMC/Paper/blob/bd5867a96f792f0eb32c1d249bb4bbc1d8338d14/patches/server/0748-Add-Velocity-IP-Forwarding-Support.patch
 * handleHello: Lines 152-161.
 * onHandleCustomQueryPacket: Lines 182-226.
 */
@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin implements ServerLoginPacketListener, TickablePacketListener {

    @Unique
    private int neoforwarding$velocityLoginMessageId = -1;
    @Final
    @Shadow
    private Connection connection;
    @Shadow
    @Nullable
    private GameProfile authenticatedProfile;

    @Shadow
    private void disconnect(Component pReason) {
    }

    @Shadow
    private void startClientVerification(GameProfile authenticatedProfile) {
    }

    // using specific injection target because it is very unlikely that something else will inject in this function.
    @Inject(method = "handleHello", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;startClientVerification(Lcom/mojang/authlib/GameProfile;)V", ordinal = 1), cancellable = true)
    public void handleHello(ServerboundHelloPacket pPacket, CallbackInfo ci) {
        if (Config.enableForwarding) {
            this.neoforwarding$velocityLoginMessageId = java.util.concurrent.ThreadLocalRandom.current().nextInt();
            net.minecraft.network.FriendlyByteBuf buf = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            buf.writeByte(PlayerDataForwarding.MAX_SUPPORTED_FORWARDING_VERSION);
            net.minecraft.network.protocol.login.ClientboundCustomQueryPacket packet1 =
                    new net.minecraft.network.protocol.login.ClientboundCustomQueryPacket(
                            this.neoforwarding$velocityLoginMessageId, new PlayerDataForwarding.VelocityPlayerInfoPayload(buf)
                    );
            this.connection.send(packet1);
            ci.cancel();
        }
    }

    @Inject(method = "handleCustomQueryPacket", at = @At("HEAD"), cancellable = true)
    private void onHandleCustomQueryPacket(ServerboundCustomQueryAnswerPacket packet, CallbackInfo ci) {
        if (Config.enableForwarding && packet.transactionId() == this.neoforwarding$velocityLoginMessageId) {

            if (packet.payload() == null) {
                this.disconnect(Component.literal("This server requires you to connect with Velocity."));
                return;
            }

            PlayerDataForwarding.QueryAnswerPayload payload = (PlayerDataForwarding.QueryAnswerPayload) packet.payload();

            net.minecraft.network.FriendlyByteBuf buf = payload.buffer();

            if (!PlayerDataForwarding.checkIntegrity(buf)) {
                this.disconnect(Component.literal("Unable to verify player details"));
                return;
            }

            int version = buf.readVarInt();
            if (version > PlayerDataForwarding.MAX_SUPPORTED_FORWARDING_VERSION) {
                throw new IllegalStateException("Unsupported forwarding version " + version + ", wanted upto " + PlayerDataForwarding.MAX_SUPPORTED_FORWARDING_VERSION);
            }

            java.net.SocketAddress listening = this.connection.getRemoteAddress();
            int port = 0;
            if (listening instanceof java.net.InetSocketAddress) {
                port = ((java.net.InetSocketAddress) listening).getPort();
            }

            ISetAddressInConnection setAddressMixin = (ISetAddressInConnection) this.connection;
            setAddressMixin.neoforwarding$setAddress(new java.net.InetSocketAddress(PlayerDataForwarding.readAddress(buf), port));

            startClientVerification(PlayerDataForwarding.createProfile(buf));

            LOGGER.info("UUID of player {} is {}", this.authenticatedProfile.getName(), this.authenticatedProfile.getId());

            ci.cancel();
        }
    }
}