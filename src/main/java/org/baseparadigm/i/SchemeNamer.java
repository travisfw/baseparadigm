package org.baseparadigm.i;

public interface SchemeNamer {
    public CharSequence name(CidScheme cidsch);
    public CidScheme reverse(CharSequence nam);
}
