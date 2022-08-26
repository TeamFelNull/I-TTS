package dev.felnull.ttsvoice.data;

import java.util.concurrent.atomic.AtomicBoolean;

public class WaitTimeThread extends Thread {
    private final AtomicBoolean wait = new AtomicBoolean();
    private final Runnable end;

    public WaitTimeThread(Runnable end) {
        this.end = end;
    }

    @Override
    public void run() {
        while (wait.get()) {
            wait.set(false);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            end.run();
        }
    }

    public void update() {
        wait.set(true);
    }
}
