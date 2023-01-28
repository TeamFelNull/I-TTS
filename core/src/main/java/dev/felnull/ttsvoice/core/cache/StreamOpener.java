package dev.felnull.ttsvoice.core.cache;

import java.io.IOException;
import java.io.InputStream;

public interface StreamOpener {
    InputStream openStream() throws IOException, InterruptedException;
}
