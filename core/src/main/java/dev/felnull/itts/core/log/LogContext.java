package dev.felnull.itts.core.log;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public interface LogContext {
    @NotNull
    Logger getLogger();
}
