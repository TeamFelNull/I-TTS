package dev.felnull.itts.savedata;

import java.io.File;

public class ServerDictData extends DictDataBase {
    private final long guildId;

    protected ServerDictData(long guildId) {
        super(new File(SelfHostSaveDataManager.SERVER_DICT_FOLDER, guildId + ".json"));
        this.guildId = guildId;
    }

    @Override
    public String getName() {
        return "Server Dict: " + guildId;
    }
}
