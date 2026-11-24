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
    public int getInt(String name, int defaultvalue) {
        String value = getString(name, null);
        if (value == null) {
            return defaultvalue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " must be a number, not " + value);
        }
    }
}
>>>>>>> e903a50bfee72cc51ef9565a40439440004c387f
