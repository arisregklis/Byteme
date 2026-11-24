package gr.aueb.byteme.evaluation;

import gr.aueb.byteme.common.Request;
import java.util.Random;

// Τυχαίοι όροφοι εκκίνησης και προορισμού. Ίδιο seed = ίδια αιτήματα.
public class UniformScenario implements Scenario {

    public String getName() {
        return "uniform";
    }

    public Request[] generate(int floors, int count, long seed) {
         if (floors < 2) {
        throw new IllegalArgumentException("floors must be at least 2");
        }
        
        Random random = new Random(seed);
        Request[] requests = new Request[count];
        int made = 0;

        while (made < count) {
            int from = random.nextInt(floors);
            int to = random.nextInt(floors);
            if (from != to) {
                requests[made] = new Request(from, to);
                made++;
            }
        }
        return requests;
    }
}
