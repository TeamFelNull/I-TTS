package dev.felnull.ttsvoice.data;

public abstract class BaseSaveData {
    private SaveTimerThread saveTimerThread;

    private class SaveTimerThread extends Thread {
        private boolean wait = true;

        private SaveTimerThread() {
            setName("save-thread");
        }

        @Override
        public void run() {

            while (wait) {
                wait = false;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }

            doSave();
            saveTimerThread = null;
        }

        private void update() {
            wait = true;
        }
    }

    abstract void doSave();

    protected void saved() {
        if (saveTimerThread == null) {
            saveTimerThread = new SaveTimerThread();
            saveTimerThread.start();
        } else {
            saveTimerThread.update();
        }
    }
}
