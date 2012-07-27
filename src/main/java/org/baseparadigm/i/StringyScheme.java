package org.baseparadigm.i;

/**
 * A cid scheme is stringy if its content ids have string representations.
 */
public interface StringyScheme extends CidScheme {

    CharSequence toCharSequence(ContentId contentId);
    ContentId fromCharSequence(CharSequence stringyCid);

}
