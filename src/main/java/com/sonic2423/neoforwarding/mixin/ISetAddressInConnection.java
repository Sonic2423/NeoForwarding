package com.sonic2423.neoforwarding.mixin;

import java.net.SocketAddress;

public interface ISetAddressInConnection {
    void neoproxy$setAddress(SocketAddress address);
}
