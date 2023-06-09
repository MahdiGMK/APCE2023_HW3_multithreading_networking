package Client.View.Command;

public class Option {
    private final String name;
    private final int inputsCount;
    private final boolean required;

    public Option(String name, int inputsCount, boolean required) {
        this.name = name;
        this.inputsCount = inputsCount;
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public int getInputsCount() {
        return inputsCount;
    }

    public boolean isRequired() {
        return required;
    }
}
