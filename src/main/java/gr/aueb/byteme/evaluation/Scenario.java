package gr.aueb.byteme.evaluation;

import gr.aueb.byteme.common.Request;

// Η διεπαφή ενός σεναρίου: φτιάχνει count αιτήματα για ένα κτίριο.
public interface Scenario {
    String getName();
    Request[] generate(int floors, int count, long seed);
}
