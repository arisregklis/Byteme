package gr.aueb.byteme.common;

// Διάβασμα απλών, επίπεδων JSON (χωρίς εμφωλευμένα αντικείμενα) με indexOf/substring.
// Σε λάθος ή ελλιπές πεδίο πετάει IllegalArgumentException, που γίνεται 400.
//
//     Json body = new Json("{\"floor\": 7, \"type\": \"PICKUP\"}");
//     body.getInt("floor");      // 7
//     body.getString("type");    // PICKUP
public class Json {
    private final String text;

    public Json(String text) {
        if (text == null) {
            this.text = "";
        } else {
            this.text = text;
        }
    }

    public boolean has(String key) {
        return text.contains(quoted(key));
    }

    // {"floor": 7} -> 7
    public int getInt(String key) {
        int start = findColon(key) + 1;
        int end = start;
        while (end < text.length() && text.charAt(end) != ',' && text.charAt(end) != '}') {
            end++;
        }
        String number = text.substring(start, end).trim();
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("400: " + key + " must be an integer");
        }
    }

    // {"elevator_id": "C"} -> C
    public String getString(String key) {
        int colon = findColon(key);
        int open = text.indexOf('"', colon);
        if (open == -1) {
            throw new IllegalArgumentException("400: " + key + " must be a string");
        }
        int close = text.indexOf('"', open + 1);
        if (close == -1) {
            throw new IllegalArgumentException("400: invalid JSON");
        }
        return text.substring(open + 1, close);
    }

    // {"next_stops": [2, 8]} -> 2
    public int countItems(String key) {
        int colon = findColon(key);
        int open = text.indexOf('[', colon);
        int close = text.indexOf(']', colon);
        if (open == -1 || close == -1) {
            throw new IllegalArgumentException("400: " + key + " must be an array");
        }
        String inside = text.substring(open + 1, close).trim();
        if (inside.isEmpty()) {
            return 0;
        }
        // n στοιχεία χωρίζονται με n - 1 κόμματα
        int items = 1;
        for (int i = 0; i < inside.length(); i++) {
            if (inside.charAt(i) == ',') {
                items++;
            }
        }
        return items;
    }

    // Θέση της ':' μετά το "key".
    private int findColon(String key) {
        int start = text.indexOf(quoted(key));
        if (start == -1) {
            throw new IllegalArgumentException("400: missing " + key);
        }
        int colon = text.indexOf(':', start);
        if (colon == -1) {
            throw new IllegalArgumentException("400: invalid JSON");
        }
        return colon;
    }

    private static String quoted(String key) {
        return "\"" + key + "\"";
    }
}