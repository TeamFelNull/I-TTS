package dev.felnull.itts;

import dev.felnull.itts.core.log.LogContext;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class LogContextImpl implements LogContext {
    @Override
    public @NotNull Logger getLogger() {
        return Main.LOGGER;
    }
}
