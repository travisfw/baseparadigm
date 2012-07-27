package org.baseparadigm;

import org.baseparadigm.i.Pattern;
import org.baseparadigm.i.ResolvableId;

/**
 * Because many pattern formats could exist, Baseparadigm does not provide implementations.
 *  Code that supports pattern formats must implement an inflator that can match patterns
 *  with appropriate implementations of the Pattern interface.
 *  
 * @author travis@traviswellman.com
 *
 */
public abstract class APatternInflator {
    
    /**
     * A registered inflator must be provided by a subclass.
     */
    protected static APatternInflator registeredInflator = null;
    
    /**
     * A subclass of PatternInflator must call this method to register itself.
     */
    protected static void registerDefaultInflator(APatternInflator pi) { registeredInflator = pi; }
    
    /**
     * Use the registered PatternInflator.
     */
    public static Pattern inflat(ResolvableId shrunkPattern) { return registeredInflator.inflate(shrunkPattern); }
    
    /**
     * Takes an id for a serialized pattern and return an instance.
     */
    public abstract Pattern inflate(ResolvableId shrunkPattern);
}
