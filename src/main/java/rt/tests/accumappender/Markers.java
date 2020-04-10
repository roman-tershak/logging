package rt.tests.accumappender;

import org.apache.logging.log4j.Marker;

public enum Markers implements Marker {
    SUCCESS, FAIL;

    @Override
    public Marker addParents(Marker... markers) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public Marker[] getParents() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean hasParents() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean isInstanceOf(Marker m) {
        return false;
    }

    @Override
    public boolean isInstanceOf(String name) {
        return false;
    }

    @Override
    public boolean remove(Marker marker) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Marker setParents(Marker... markers) {
        throw new UnsupportedOperationException("Not supported");
    }
}
