package dev.felnull.itts.core;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 処理中にエラーが発生しても停止しないタイマー
 * 元のタイマーをラップする
 *
 * @author MORIMORI0317
 */
public class ImmortalityTimer implements ITTSRuntimeUse {

    /**
     * ラップされるタイマー
     */
    private final Timer timer;

    /**
     * コンストラクタ
     *
     * @param timer ラップされるタイマー
     */
    public ImmortalityTimer(Timer timer) {
        this.timer = timer;
    }

    /**
     * Timerのscheduleと同機能
     *
     * @param task  タスク
     * @param delay 遅延
     * @see Timer
     */
    public void schedule(ImmortalityTimerTask task, long delay) {
        timer.schedule(task.task, delay);
    }

    /**
     * Timerのscheduleと同機能
     *
     * @param task   タスク
     * @param delay  遅延
     * @param period 間隔
     * @see Timer
     */
    public void schedule(ImmortalityTimerTask task, long delay, long period) {
        timer.schedule(task.task, delay, period);
    }

    /**
     * TimerTaskのラップ
     *
     * @author MORIMORI0317
     * @see TimerTask
     */
    public abstract static class ImmortalityTimerTask implements Runnable {
        /**
         * ラップされるタイマータスク
         */
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

        /**
         * タイマータスクをキャンセル
         */
        public void cancel() {
            task.cancel();
        }
    }
}
