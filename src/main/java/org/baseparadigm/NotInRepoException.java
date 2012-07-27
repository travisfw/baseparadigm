package org.baseparadigm;

/**
 * Thrown when a Repo does not contain content for a ContentId but it is
 * necessary for it to do so.
 */
public class NotInRepoException extends Exception {

    public NotInRepoException(String message) {
        super(message);
    }

    private static final long serialVersionUID = 1971887447807626688L;

}
