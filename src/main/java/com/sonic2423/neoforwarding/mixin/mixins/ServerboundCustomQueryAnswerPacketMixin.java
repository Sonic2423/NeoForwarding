package com.sonic2423.neoforwarding.mixin.mixins;

import com.sonic2423.neoforwarding.PlayerDataForwarding;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.network.protocol.login.custom.CustomQueryAnswerPayload;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.sonic2423.neoforwarding.Config;

/*
 * The following is ported from "Paper" with slight modifications to work with NeoForge as Mixin.
 * See: https://github.com/PaperMC/Paper/blob/bd5867a96f792f0eb32c1d249bb4bbc1d8338d14/patches/server/0009-MC-Utils.patch
 * Lines 6040-6050.
 */
@Mixin(ServerboundCustomQueryAnswerPacket.class)
public abstract class ServerboundCustomQueryAnswerPacketMixin {

    @Shadow @Final private static int MAX_PAYLOAD_SIZE;

    @Inject(method = "readUnknownPayload", at = @At("HEAD"), cancellable = true)
    private static void onReadPayload(FriendlyByteBuf pBuffer, CallbackInfoReturnable<CustomQueryAnswerPayload> cir) {
        if(!Config.enableForwarding) return;

        FriendlyByteBuf buffer = pBuffer.readNullable((buf2) -> {
            int i = buf2.readableBytes();
            if (i >= 0 && i <= MAX_PAYLOAD_SIZE) {
                return new FriendlyByteBuf(buf2.readBytes(i));
            } else {
                throw new IllegalArgumentException("Payload may not be larger than " + MAX_PAYLOAD_SIZE + " bytes");
            }
        });

        cir.setReturnValue(buffer == null ? null : new PlayerDataForwarding.QueryAnswerPayload(buffer));
        cir.cancel();
    }
}
