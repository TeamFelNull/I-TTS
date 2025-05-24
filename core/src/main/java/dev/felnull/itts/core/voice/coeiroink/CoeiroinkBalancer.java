package dev.felnull.itts.core.voice.coeiroink;

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

/**
 * Coeiroinkエンジンの使用バランスを調整
 *
 * @author MORIMORI0317
 */
public class CoeiroinkBalancer implements ITTSRuntimeUse {

    /**
     * Coeiroinkマネージャー
     */
    private final CoeiroinkManager manager;

    /**
     * エンジンのURL
     */
    private final Supplier<List<String>> enginUrls;

    /**
     * 確認用ロック
     */
    private final Object checkLock = new Object();

    /**
     * 使用カウント
     */
    private final Map<CIURL, AtomicInteger> useCounter = new ConcurrentHashMap<>();

    /**
     * 使用可能なURL
     */
    private List<CIURL> availableUrls;

    /**
     * 使用可能な話者
     */
    private List<CoeiroinkSpeaker> availableSpeakers;

    /**
     * コンストラクタ
     *
     * @param manager   VOICEVOXマネージャー
     * @param enginUrls エンジンのURL
     */
    public CoeiroinkBalancer(CoeiroinkManager manager, Supplier<List<String>> enginUrls) {
        this.manager = manager;
        this.enginUrls = enginUrls;
    }

    /**
     * CoeiroInkの使用カウンタを取得
     * このメソッドは、指定されたCIURLに対する使用回数をカウントするためのAtomicIntegerを返す
     * 初めて指定されたURLの場合、新しいAtomicIntegerが作成され、既存のURLの場合、既存のカウンタが返される
     * 
     * @param ciurl CoeiroInkのURLを表すCIURLオブジェクト
     * @return 指定されたURLの使用回数をカウントするAtomicInteger
     */
    private AtomicInteger getUseCounter(CIURL ciurl) {
        return useCounter.computeIfAbsent(ciurl, k -> new AtomicInteger());
    }

    /**
     * 全ての話者を取得
     *
     * @return 話者のリスト
     */
    protected List<CoeiroinkSpeaker> getAvailableSpeakers() {
        synchronized (checkLock) {
            return Objects.requireNonNullElseGet(availableSpeakers, ImmutableList::of);
        }
    }

    /**
     * 初期化
     *
     * @return 初期化を行う
     */
    public CompletableFuture<?> init() {
        return CompletableFuture.runAsync(this::check, getAsyncExecutor());
    }

    private void check() {
        synchronized (checkLock) {
            Pair<List<CIURL>, List<CoeiroinkSpeaker>> cr = checkAndGet();
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

    /**
     * CoeiroinkエンジンのURLの可用性をチェックし、スピーカー情報を取得する
     *
     * @return ペアオブジェクトで、第一要素に可用なCIURLのリスト、第二要素にCoeiroinkSpeakerのリストを含む
     * スピーカー情報は最初の成功したリクエストから取得されたもの
     * @throws RuntimeException IOエラーまたは中断が発生した場合にスローされる
     */
    private Pair<List<CIURL>, List<CoeiroinkSpeaker>> checkAndGet() {
        List<Pair<CIURL, CompletableFuture<List<CoeiroinkSpeaker>>>> urls = enginUrls.get().stream()
                .map(CIURL::new)
                .map(n -> Pair.of(n, CompletableFuture.supplyAsync(() -> {
                    try {
                        return manager.requestSpeakers(n);
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }, getAsyncExecutor())))
                .toList();

        List<CIURL> rurls = new ArrayList<>();
        List<CoeiroinkSpeaker> rspeakers = null;

        for (Pair<CIURL, CompletableFuture<List<CoeiroinkSpeaker>>> ret : urls) {
            CIURL vu = ret.getLeft();
            CompletableFuture<List<CoeiroinkSpeaker>> cf = ret.getRight();

            try {
                List<CoeiroinkSpeaker> r = cf.get();
                if (rspeakers == null) {
                    rspeakers = r;
                }

                rurls.add(vu);

                if (availableUrls == null || !availableUrls.contains(vu)) {
                    getITTSLogger().info("Available {} URL: {}", manager.getName(), vu.url());
                }

            } catch (InterruptedException | ExecutionException e) {
                if (availableUrls == null || availableUrls.contains(vu)) {
                    getITTSLogger().warn("Unavailable {} URL: {}", manager.getName(), vu.url());
                }
            }

        }

        return Pair.of(rurls, rspeakers);
    }

    /**
     * エンジンが利用可能かどうかをチェックする
     * 
     * @return エンジンが利用可能であればtrue、それ以外はfalse
     * 
     * @see #enginUrls
     */
    public boolean isAvailable() {
        return enginUrls != null && !enginUrls.get().isEmpty();
    }

    /**
     * URLの使用インターフェイスを取得
     *
     * @return URLの使用インターフェイス
     */
    protected CoeiroinkUseURL getUseURL() {
        if (availableUrls == null || availableUrls.isEmpty()) {
            throw new RuntimeException("No URL available.");
        }

        CIURL ciurl = availableUrls.stream()
                .min(Comparator.comparingInt(r -> getUseCounter(r).get()))
                .get();

        getUseCounter(ciurl).incrementAndGet();
        return new CoeiroinkUseURL() {
            @Override
            public CIURL getCIURL() {
                return ciurl;
            }

            @Override
            public void close() {
                getUseCounter(ciurl).decrementAndGet();
            }
        };
    }
}
