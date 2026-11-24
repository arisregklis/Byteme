package gr.aueb.byteme.common;

public class Args {
    private String[] args;

    public Args(String[] args) {
        this.args = args;
    }

    public String getString(String name, String defaultvalue) {
    for (int i = 0; i < args.length; i++) {
        if (args[i].equals(name)) {
            return args[i + 1];
        }
    }
    return defaultvalue;
    }
}