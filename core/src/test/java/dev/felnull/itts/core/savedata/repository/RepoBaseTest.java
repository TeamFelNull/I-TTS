package dev.felnull.itts.core.savedata.repository;

import dev.felnull.itts.core.savedata.AbstractSaveDataTest;
import dev.felnull.itts.core.savedata.dao.DAOFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

public abstract class RepoBaseTest extends AbstractSaveDataTest {

    @TempDir
    private static Path dbDir;

    @BeforeAll
    static void setupAll() {
        File dbFile = new File(dbDir.toFile(), "save_data.db");
        assertFalse(dbFile.exists());
    }

    protected DataRepository createRepository() {
        File dbFile = new File(dbDir.toFile(), "save_data.db");
        DataRepository repo = DataRepository.create(() -> DAOFactory.getInstance().createSQliteDAO(dbFile));
        repo.init();
        return repo;
    }

}
