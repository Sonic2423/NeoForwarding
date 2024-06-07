package com.sonic2423.neoforwarding.mixin.mixins;

import com.sonic2423.neoforwarding.mixin.ISetAddressInConnection;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.net.SocketAddress;

@Mixin(Connection.class)
public abstract class SetAddressInConnection implements ISetAddressInConnection {
    @Shadow
    private SocketAddress address;

    @Override
    public void neoproxy$setAddress(SocketAddress address) {
        this.address = address;
    }
}
