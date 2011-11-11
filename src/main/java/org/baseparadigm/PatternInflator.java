package org.baseparadigm;

/**
 * Because many pattern formats could exist, Baseparadigm does not provide implementations.
 *  Code that supports pattern formats must implement an inflator that can match patterns
 *  with appropriate implementations of the Pattern interface.
 *  
 * @author travis@traviswellman.com
 *
 */
public abstract class PatternInflator {
    
    /**
     * A default inflator must be provided by a subclass.
     */
    protected static PatternInflator defaultInflator = null;
    
    /**
     * A subclass of PatternInflator must call this method to register itself as default.
     */
    protected static void registerDefaultInflator(PatternInflator pi) { defaultInflator = pi; }
    
    /**
     * Get the default PatternInflator.
     */
    public static PatternInflator getDefaultInflator() { return defaultInflator; }
    
    /**
     * Takes an id for a serialized pattern and return an instance.
     */
    public abstract Pattern inflate(ContentId shrunkPattern);
}
