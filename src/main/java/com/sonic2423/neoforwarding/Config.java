package com.sonic2423.neoforwarding;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import static com.sonic2423.neoforwarding.NeoForwarding.LOGGER;

@Mod.EventBusSubscriber(modid = NeoForwarding.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    public static boolean enableForwarding;
    public static String forwardingSecret;

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<String> FORWARDING_SECRET = BUILDER
            .comment("Use the 'forwarding.secret' from Velocity (and not the default value of '') and insert it here")
            .define("forwardingSecret", "");

    private static final ModConfigSpec.BooleanValue ENABLE_FORWARDING = BUILDER
            .comment("This must be enabled after you inserted your forwarding secret for the server to accept and send forwarding requests.\nIf disabled the server will act as if the mod is not installed.")
            .define("enableForwarding", false);

    static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        enableForwarding = ENABLE_FORWARDING.get();
        forwardingSecret = FORWARDING_SECRET.get();

        if (enableForwarding) {
            if (Config.forwardingSecret.isEmpty()) {
                LOGGER.warn("Please specify a forwarding secret.");
                LOGGER.warn("NeoForwarding will be disabled!");
                enableForwarding = false;
            } else if (Config.forwardingSecret.length() != 12) {
                LOGGER.error("Malformed modern forwarding secret.");
                LOGGER.error("It is very likely that no one can log in!");
            } else {
                LOGGER.info("Modern forwarding enabled.");
            }
        } else {
            LOGGER.info("Modern forwarding disabled.");
        }
    }
}
