package app.finwave.rct.reactive;

public interface ListenerRemover {
    ListenerRemover VOID = () -> {};

    void remove();
}
