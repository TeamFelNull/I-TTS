package dev.felnull.itts;

/**
 * グローバルキャッシュのテスト
 *
 * @author MORIMORI0317
 * @see <a href="https://qiita.com/shiozaki/items/b746dc4bb5e1e87c0528">参考サイト</a>
 */
public class GlobalCacheTest /*implements GlobalCacheAccess*/ {

  /*
    private final JedisPool pool;
    private final Jedis resource;

    protected GlobalCacheTest() {
        this.pool = new JedisPool("localhost", 6379);
        this.resource = pool.getResource();
    }

    @Override
    public byte[] get(@NotNull HashCode hashCode) {
        var key = hashCode.asBytes();
        if (!resource.exists(key))
            return null;

        Response<byte[]> res;
        try (var tr = resource.multi()) {
            res = tr.get(key);
            tr.expire(key, 60 * 10);
            tr.exec();
        }

        return res.get();
    }

    @Override
    public void set(@NotNull HashCode hashCode, byte[] data) {
        var key = hashCode.asBytes();
        try (var tr = resource.multi()) {
            tr.set(key, data);
            tr.expire(key, 60 * 10);
            tr.exec();
        }
    }

    @Override
    public void lock(@NotNull HashCode hashCode) {

    }

    @Override
    public void unlock(@NotNull HashCode hashCode) {

    }

    @Override
    public void close() throws Exception {
        // All unlock
        this.resource.close();
        this.pool.close();
    }*/
}
