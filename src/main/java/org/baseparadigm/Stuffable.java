package org.baseparadigm;

public interface Stuffable {

    /**
     * Initializes the Pattern after using the zero arg constructor.
     */
    public void init(ContentId cid);
    
    /**
     * Create a MapDatum in the given Repo; stuff(Repo) is the inverse of init(ContentId).
     * The ContentId returned should reference a MapDatum which has the 
     * appropriate TYPE and all the rest of the necessary data (possibly
     * entirely in the "CONTENT" field) to initialize an object of this type using init(ContentId).
     */
    public ContentId stuff(Repo r);
}
