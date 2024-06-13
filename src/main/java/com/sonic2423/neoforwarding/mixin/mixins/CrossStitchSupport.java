package com.sonic2423.neoforwarding.mixin.mixins;

import com.mojang.brigadier.arguments.ArgumentType;
import io.netty.buffer.Unpooled;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.sonic2423.neoforwarding.NeoForwarding.LOGGER;

/*
 * The following is ported from "CrossStitch" with slight modifications to work with NeoForge.
 * See: https://github.com/VelocityPowered/CrossStitch/blob/f8d6be1128cb049e5c5a93068b9069e0838a2200/src/main/java/com/velocitypowered/crossstitch/mixin/command/CommandTreeSerializationMixin.java
 */
@Mixin(targets = "net.minecraft.network.protocol.game.ClientboundCommandsPacket$ArgumentNodeStub")
public abstract class CrossStitchSupport {

    @Unique
    private static final int MOD_ARGUMENT_INDICATOR = -256;

    @Inject(method = "serializeCap(Lnet/minecraft/network/FriendlyByteBuf;Lnet/minecraft/commands/synchronization/ArgumentTypeInfo;Lnet/minecraft/commands/synchronization/ArgumentTypeInfo$Template;)V", at = @At("HEAD"), cancellable = true)
    private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void neoforwarding$wrapInVelocityModArgument(FriendlyByteBuf pBuffer, ArgumentTypeInfo<A, T> pArgumentInfo, ArgumentTypeInfo.Template<A> pArgumentInfoTemplate, CallbackInfo ci) {
        ResourceLocation key = BuiltInRegistries.COMMAND_ARGUMENT_TYPE.getKey(pArgumentInfo);
        int id = BuiltInRegistries.COMMAND_ARGUMENT_TYPE.getId(pArgumentInfo);

        if (key == null || key.getNamespace().equals("brigadier") || (key.getNamespace().equals("minecraft") && !key.getPath().equals("test_argument") && !key.getPath().equals("test_class"))) {
            return;
        }

        LOGGER.debug("Mod argument type: {}: {}:{}", id, key.getNamespace(), key.getPath());

        ci.cancel();

        // Not a standard Minecraft argument type - so we need to wrap it
        neoforwarding$serializeWrappedArgumentType(pBuffer, pArgumentInfo, pArgumentInfoTemplate);
    }

    @Unique
    private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void neoforwarding$serializeWrappedArgumentType(FriendlyByteBuf pBuffer, ArgumentTypeInfo<A, T> pArgumentInfo, ArgumentTypeInfo.Template<A> pArgumentInfoTemplate) {
        pBuffer.writeVarInt(MOD_ARGUMENT_INDICATOR);
        pBuffer.writeVarInt(BuiltInRegistries.COMMAND_ARGUMENT_TYPE.getId(pArgumentInfo));

        FriendlyByteBuf extraData = new FriendlyByteBuf(Unpooled.buffer());
        pArgumentInfo.serializeToNetwork((T) pArgumentInfoTemplate, extraData);

        pBuffer.writeVarInt(extraData.readableBytes());
        pBuffer.writeBytes(extraData);
    }
}

