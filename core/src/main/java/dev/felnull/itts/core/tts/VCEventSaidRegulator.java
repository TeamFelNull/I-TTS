package dev.felnull.itts.core.tts;

import dev.felnull.itts.core.tts.saidtext.VCEventSaidText;
import dev.felnull.itts.core.util.ApoptosisObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class VCEventSaidRegulator {
    private static final long REGULATOR_TIME = 1000 * 10;
    private final Map<Long, RegulatorEntry> regulators = new ConcurrentHashMap<>();
    private final TTSInstance ttsInstance;

    public VCEventSaidRegulator(TTSInstance ttsInstance) {
        this.ttsInstance = ttsInstance;
    }

    protected boolean restrict(long userId, VCEventSaidText vcEventSaidText) {
        var rgr = regulators.computeIfAbsent(userId, userId1 -> new RegulatorEntry(userId1, vcEventSaidText.getEventType().isJoin()));
        var pre = rgr.setLastVcEventSaidText(vcEventSaidText);
        return pre != null;
    }

    protected void dispose() {
        regulators.forEach((id, en) -> en.broke());
    }

    private class RegulatorEntry extends ApoptosisObject {
        private final long userId;
        private final AtomicReference<VCEventSaidText> vcEventSaidText = new AtomicReference<>();
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
                var vs = vcEventSaidText.get();
                if (vs != null && firstIsJoin != vs.getEventType().isJoin())
                    ttsInstance.sayText(vs);
            }
        }

        public VCEventSaidText setLastVcEventSaidText(VCEventSaidText lastVcEventSaidText) {
            return vcEventSaidText.getAndSet(lastVcEventSaidText);
        }
    }
}
