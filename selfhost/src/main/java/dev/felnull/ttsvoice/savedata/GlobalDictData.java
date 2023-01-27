package dev.felnull.ttsvoice.savedata;

public class GlobalDictData extends DictDataBase {
    protected GlobalDictData() {
        super(SelfHostSaveDataManager.GLOBAL_DICT_DIR);
    }

    @Override
    public String getName() {
        return "Global Dict";
    }
}
