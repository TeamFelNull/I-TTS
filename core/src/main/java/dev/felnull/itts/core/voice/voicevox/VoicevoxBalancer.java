package dev.felnull.itts.core.voice.voicevox;

import com.google.common.collect.ImmutableList;
import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.ImmortalityTimer;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class VoicevoxBalancer implements ITTSRuntimeUse {
    private final VoicevoxManager manager;
    private final Supplier<List<String>> enginUrls;
    private final Object checkLock = new Object();
    private final Map<VVURL, AtomicInteger> useCounter = new ConcurrentHashMap<>();
    private List<VVURL> availableUrls;
    private List<VoicevoxSpeaker> availableSpeakers;

    public VoicevoxBalancer(VoicevoxManager manager, Supplier<List<String>> enginUrls) {
        this.manager = manager;
        this.enginUrls = enginUrls;
    }

    private AtomicInteger getUseCounter(VVURL vvurl) {
        return useCounter.computeIfAbsent(vvurl, k -> new AtomicInteger());
    }

    protected List<VoicevoxSpeaker> getAvailableSpeakers() {
        synchronized (checkLock) {
            if (availableSpeakers == null)
                return ImmutableList.of();

            return availableSpeakers;
        }
    }

    public CompletableFuture<?> init() {
        return CompletableFuture.runAsync(this::check, getAsyncExecutor());
    }

    private void check() {
        synchronized (checkLock) {
            var cr = checkAndGet();
            availableUrls = cr.getLeft();
            availableSpeakers = cr.getRight();
        }

        getImmortalityTimer().schedule(new ImmortalityTimer.ImmortalityTimerTask() {
            @Override
            public void run() {
                CompletableFuture.runAsync(() -> check(), getAsyncExecutor());
            }
        }, manager.getConfig().getCheckTime());
    }

    private Pair<List<VVURL>, List<VoicevoxSpeaker>> checkAndGet() {
        var urls = enginUrls.get().stream()
                .map(VVURL::new)
                .map(n -> Pair.of(n, CompletableFuture.supplyAsync(() -> {
                    try {
                        return manager.requestSpeakers(n);
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }, getAsyncExecutor())))
                .toList();

        List<VVURL> rurls = new ArrayList<>();
        List<VoicevoxSpeaker> rspeakers = null;

        for (Pair<VVURL, CompletableFuture<List<VoicevoxSpeaker>>> ret : urls) {
            var vu = ret.getLeft();
            var cf = ret.getRight();

            try {
                var r = cf.get();

                if (rspeakers == null)
                    rspeakers = r;

                rurls.add(vu);

                if (availableUrls == null || !availableUrls.contains(vu))
                    getITTSLogger().info("Available {} URL: {}", manager.getName(), vu.url());
            } catch (InterruptedException | ExecutionException e) {
                if (availableUrls == null || availableUrls.contains(vu))
                    getITTSLogger().warn("Unavailable {} URL: {}", manager.getName(), vu.url());
            }

        }

        return Pair.of(rurls, rspeakers);
    }

    public boolean isAvailable() {
        return enginUrls != null && !enginUrls.get().isEmpty();
    }

    protected VoicevoxUseURL getUseURL() {
        if (availableUrls == null || availableUrls.isEmpty())
            throw new RuntimeException("No URL available.");

        VVURL vvurl = availableUrls.stream()
                .min(Comparator.comparingInt(r -> getUseCounter(r).get()))
                .get();

        getUseCounter(vvurl).incrementAndGet();
        return new VoicevoxUseURL() {
            @Override
            public VVURL getVVURL() {
                return vvurl;
            }

            @Override
            public void close() {
                getUseCounter(vvurl).decrementAndGet();
            }
        };
    }
}
