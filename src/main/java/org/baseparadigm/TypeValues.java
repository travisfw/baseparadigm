package org.baseparadigm;


public enum TypeValues implements ToByteArray {
    RAW, URI, PATTERN;

    @Override
    public byte[] toByteArray() {
        return this.name().getBytes();
    }
}

