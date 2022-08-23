package org.example.test;

import org.apache.hadoop.util.DataChecksum;

public enum Type {
    NULL  (1, 0),
    CRC32 (2, 4),
    CRC32C(3, 4),
    DEFAULT(4, 0), // This cannot be used to create DataChecksum
    MIXED (5, 0); // This cannot be used to create DataChecksum

    public final int id;
    public final int size;

    private Type(int id, int size) {
        this.id = id;
        this.size = size;
    }

    /** @return the type corresponding to the id. */
    public static Type valueOf(int id) {
        if (id < 0 || id >= values().length) {
            throw new IllegalArgumentException("id=" + id
                    + " out of range [0, " + values().length + ")");
        }
        return values()[id];
    }
}
