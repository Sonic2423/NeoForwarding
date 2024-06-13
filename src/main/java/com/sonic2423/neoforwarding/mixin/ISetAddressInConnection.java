package com.sonic2423.neoforwarding.mixin;

import java.net.SocketAddress;

public interface ISetAddressInConnection {
    void neoforwarding$setAddress(SocketAddress address);
}
