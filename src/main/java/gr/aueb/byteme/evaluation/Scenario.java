package gr.aueb.byteme.evaluation;

import gr.aueb.byteme.common.Request;

public interface Scenario {
    String getName();
    Request[] generate(int floors, int count, long seed);
    
}
