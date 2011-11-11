package org.baseparadigm;

public enum MetadataFields {
    
    /**
     * instead of names, documents in spaciousness are graphically rendered. This field contains
     *  an explicit renderer. Perhaps it is the source for a script, or perhaps it is a constant
     *  identifying the method for the platform you're using. Loop through the values to find
     *  one that works for you. 
     */
    DATA_RENDERER
            
    /**
     * most metadata documents would have a "content" field containing the ids of actual bytes. 
     */
    , CONTENT

    /**
     * most metadata documents would have a "content" field containing the ids of actual bytes. 
     */
    , TYPE
    
    /**
     * If the contained blob were to be split into a collection of edges, that graph would
     *  conform to the patterns named in this field. This field may be used to match parsers
     *  to the file format so that documents may be navigated in the Spaciousness way.
     */
    , PATTERNS
    ;
    
}
