package dev.felnull.ttsvoice.voice;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.BooleanSupplier;

public class SimpleAliveChecker {
    private final BooleanSupplier enable;
    private final BooleanSupplier checker;
    private boolean alive;

    public SimpleAliveChecker(BooleanSupplier enable, BooleanSupplier checker) {
        this.enable = enable;
        this.checker = checker;
    }

    public void init(Timer timer) {
        if (!enable.getAsBoolean()) return;

        alive = checker.getAsBoolean();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                alive = checker.getAsBoolean();
            }
        }, 0, 10 * 1000);
    }

    public boolean isAlive() {
        return alive;
    }
}
