package org.baseparadigm;


public enum TypeValues implements ToByteArray {
    RAW, URI, PATTERN, TEXT;

    @Override
    public byte[] toByteArray() {
        return this.name().getBytes();
    }
}

