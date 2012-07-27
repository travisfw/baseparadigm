package org.baseparadigm.i;

/**
 * Content Ids are aliases for content. A {@link ResolvableId} can be directly
 * asked for such content, keeping the two together. The equals method for
 * ResolvableId should not consider the content, caching mechanism, etc. All
 * ContentIds should equate when their toByteArray methods return the same byte
 * array and the {@link CidScheme}s are the same ({@code cids1.equals(cids2)}).
 * 
 * @author travis@traviswellman.com
 * 
 */
public interface ResolvableId extends ContentId, HasRepo {
    byte[] resolve();
}
