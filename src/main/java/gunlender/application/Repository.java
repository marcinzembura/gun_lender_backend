package gunlender.application;

import gunlender.domain.exceptions.RepositoryException;

public interface Repository {
    void migrate() throws RepositoryException;
}
