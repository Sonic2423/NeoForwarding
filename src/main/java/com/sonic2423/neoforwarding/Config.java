package com.sonic2423.neoforwarding;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import static com.sonic2423.neoforwarding.NeoForwarding.LOGGER;

@Mod.EventBusSubscriber(modid = NeoForwarding.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    public static boolean enableForwarding;
    public static boolean enableEmbeddedCrossStitch;
    public static String forwardingSecret;

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.ConfigValue<String> FORWARDING_SECRET = BUILDER
            .comment("""
                    Every configuration change will immediately be reflected on the running server! (Automatic reload)
                    
                    Use the 'forwarding.secret' from Velocity (and not the default value of "") and insert it here, between the quotes.
                    Example:
                    forwardingSecret="abcdEFGH1234\"""")
            .define("forwardingSecret", "");

    private static final ModConfigSpec.BooleanValue ENABLE_FORWARDING = BUILDER
            .comment("""
                    
                    This must be enabled after you inserted your forwarding secret for the server to accept and send forwarding requests.
                    If disabled the server will act as if the mod is not installed.
                    Consider deleting NeoForwarding jar if you don´t want to use it!
                    (leaving the mod installed will still apply mixins but cancel each function)""")
            .define("enableForwarding", false);

    private static final ModConfigSpec.BooleanValue ENABLE_EMBEDDED_CROSSSTITCH = BUILDER
            .comment("""
                    
                    This must be enabled for velocity to pass modded commands.
                    If disabled Velocity will throw an exception and the login process will be unsuccessful.
                    Leave this on unless you know what you are doing.""")
            .define("enableEmbeddedCrossStitch", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onModConfigEvent(final ModConfigEvent event) {
        if(event instanceof ModConfigEvent.Loading || event instanceof ModConfigEvent.Reloading){
            enableForwarding = ENABLE_FORWARDING.get();
            forwardingSecret = FORWARDING_SECRET.get();
            enableEmbeddedCrossStitch = ENABLE_EMBEDDED_CROSSSTITCH.get();

            if (enableForwarding) {
                if (forwardingSecret.isEmpty()) {
                    LOGGER.error("Please specify a forwarding secret.\nNeoForwarding will be disabled!");
                    enableForwarding = false;
                } else if (forwardingSecret.length() != 12) {
                    LOGGER.warn("Malformed modern forwarding secret.\nIt is very likely that no one can log in!");
                } else {
                    LOGGER.info("Modern forwarding enabled.");
                }
            } else {
                LOGGER.info("Modern forwarding disabled.");
            }
        }
    }
}
