package dev.felnull.ttsvoice.discord;

import com.google.common.collect.ImmutableMap;
import dev.felnull.ttsvoice.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

public class JDAManager {
    private static final JDAManager INSTANCE = new JDAManager();
    private Map<Long, JDA> JDAs;

    public static JDAManager getInstance() {
        return INSTANCE;
    }

    public void init(Consumer<JDA> jdaConsumer) throws LoginException {
        ImmutableMap.Builder<Long, JDA> jdasBuilder = new ImmutableMap.Builder<>();
        for (String botToken : Main.getConfig().botTokens()) {
            var jda = JDABuilder.createDefault(botToken).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();
            jdaConsumer.accept(jda);
            jdasBuilder.put(jda.getSelfUser().getIdLong(), jda);
        }
        JDAs = jdasBuilder.build();
    }

    public JDA getJDA(long botUserId) {
        return JDAs.get(botUserId);
    }

    public Collection<JDA> getAllJDA() {
        return JDAs.values();
    }
}
