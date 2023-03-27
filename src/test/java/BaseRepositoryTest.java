import com.github.javafaker.Faker;
import gunlender.application.Repository;
import org.junit.jupiter.api.AfterAll;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class BaseRepositoryTest {
    protected static final Faker FAKER = new Faker();
    protected static final List<String> databaseFiles = new ArrayList<>();
    protected static final Logger LOGGER = LoggerFactory.getLogger(UserRepositoryTest.class);

    protected <T extends Repository> Repository getRepository(Class<T> tClass) throws Exception {
        var fileName = "gunlender" + UUID.randomUUID() + ".db";
        var connectionString = "jdbc:sqlite:" + fileName;

        var ctor = tClass.getConstructor(String.class);
        var repo = ctor.newInstance(connectionString);
        repo.migrate();

        databaseFiles.add(fileName);
        return repo;
    }

    @AfterAll
    static void Cleanup() {
        var path = Paths.get(System.getProperty("user.dir"));
        for (var db : databaseFiles) {
            var file = new File(Paths.get(path.toString(), db).toString());
            file.deleteOnExit();
        }
    }
}
