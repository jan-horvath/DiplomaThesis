package cz.muni.fi.thesis.shingling;

import java.util.List;
import java.util.Objects;

public final class Shingle {
    private final List<Integer> shingle;

    public Shingle(List<Integer> shingle) {
        this.shingle = shingle;
    }

    public List<Integer> getShingle() {
        return shingle;
    }

    public int getSize() {
        return shingle.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shingle shingle1 = (Shingle) o;
        return shingle.equals(shingle1.shingle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shingle);
    }

    @Override
    public String toString() {
        return shingle.toString();
    }
}
