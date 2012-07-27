package org.baseparadigm.i;

import java.util.Collection;

public interface RepoWithBackups extends Repo {

    /**
     * It is recommended not to modify the returned collection.
     * @return a collection of what storage will recieve content when this repo recieves content.
     */
    Collection<RepoStorage> getSecondaries();

    boolean addSecondary(RepoStorage rs);

}
