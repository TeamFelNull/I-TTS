package dev.felnull.itts.core;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 処理中にエラーが発生しても停止しないタイマー
 *
 * @author MORIMORI0317
 */
public class ImmortalityTimer implements ITTSRuntimeUse {
    private final Timer timer;

    public ImmortalityTimer(Timer timer) {
        this.timer = timer;
    }

    public void schedule(ImmortalityTimerTask task, long delay) {
        timer.schedule(task.task, delay);
    }

    public void schedule(ImmortalityTimerTask task, long delay, long period) {
        timer.schedule(task.task, delay, period);
    }

    public static abstract class ImmortalityTimerTask implements Runnable {
        private final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    ImmortalityTimerTask.this.run();
                } catch (Exception ex) {
                    ITTSRuntime.getInstance().getLogger().error("An error occurred while processing the timer", ex);
                }
            }
        };

        public void cancel() {
            task.cancel();
        }
    }
}
