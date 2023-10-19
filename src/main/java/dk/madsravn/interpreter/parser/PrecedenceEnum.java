package dk.madsravn.interpreter.parser;

public enum PrecedenceEnum {
    LOWEST(1),
    EQUALS(2),
    LESSGREATER(3),
    SUM(4),
    PRODUCT(5),
    PREFIX(6),
    CALL(7),
    ;

    private final int order;

    PrecedenceEnum(int order) { this.order = order;}

    @Override
    public String toString() {
        return this.name() + " (" + this.order + ")";
    }
}
