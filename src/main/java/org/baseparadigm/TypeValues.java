package org.baseparadigm;

import org.baseparadigm.i.ToByteArray;


public enum TypeValues implements ToByteArray {
    RAW, URI, PATTERN, TEXT,
    GPG_SIGNATURE;

    @Override
    public byte[] toByteArray() {
        return this.name().getBytes();
    }
}

