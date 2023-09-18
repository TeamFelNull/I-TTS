package dev.felnull.itts.core.tts;

import dev.felnull.itts.core.tts.saidtext.VCEventSaidText;
import dev.felnull.itts.core.util.ApoptosisObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * VCイベントの制御
 *
 * @author MORIMORI0317
 */
public class VCEventSaidRegulator {
    /**
     * 読み上げを制限する時間
     */
    private static final long REGULATOR_TIME = 3000;

    /**
     * 制限エントリ
     */
    private final Map<Long, RegulatorEntry> regulators = new ConcurrentHashMap<>();

    /**
     * TTSインスタンス
     */
    private final TTSInstance ttsInstance;

    /**
     * コンストラクタ
     *
     * @param ttsInstance TTSインスタンス
     */
    public VCEventSaidRegulator(TTSInstance ttsInstance) {
        this.ttsInstance = ttsInstance;
    }

    /**
     * 読み上げを制限するかどうか
     *
     * @param userId          ユーザID
     * @param vcEventSaidText VCイベント読み上げテキスト
     * @return 制限するかどうか
     */
    protected boolean restrict(long userId, VCEventSaidText vcEventSaidText) {
        RegulatorEntry rgr = regulators.computeIfAbsent(userId, userId1 -> new RegulatorEntry(userId1, vcEventSaidText.getEventType().isJoin()));
        VCEventSaidText pre = rgr.setLastVcEventSaidText(vcEventSaidText);
        return pre != null;
    }

    /**
     * 破棄
     */
    protected void dispose() {
        regulators.forEach((id, en) -> en.broke());
    }

    /**
     * 制限エントリ
     *
     * @author MORIMORI0317
     */
    private class RegulatorEntry extends ApoptosisObject {

        /**
         * ユーザーID
         */
        private final long userId;

        /**
         * VCイベントの読み上げテキスト
         */
        private final AtomicReference<VCEventSaidText> vcEventSaidText = new AtomicReference<>();

        /**
         * 最初に参加したかどうか
         */
        private final boolean firstIsJoin;

        protected RegulatorEntry(long userId, boolean firstIsJoin) {
            super(REGULATOR_TIME);
            this.userId = userId;
            this.firstIsJoin = firstIsJoin;
        }

        @Override
        protected void lifeEnd(boolean force) {
            regulators.remove(userId);

            if (!force) {
                VCEventSaidText vs = vcEventSaidText.get();
                if (vs != null && firstIsJoin != vs.getEventType().isJoin()) {
                    ttsInstance.sayText(vs);
                }
            }
        }

        public VCEventSaidText setLastVcEventSaidText(VCEventSaidText lastVcEventSaidText) {
            return vcEventSaidText.getAndSet(lastVcEventSaidText);
        }
    }
}
