package gr.aueb.byteme.central;

import gr.aueb.byteme.common.Request;

public class SimpleDispatcher implements DispatchAlgorithm {

    private static final int STOP_PENALTY = 6;

    @Override
    public String chooseElevator(ElevatorState[] states, Request request) {
        if (states == null || request == null) {
            return null;
        }

        String bestId = null;
        int bestCost = Integer.MAX_VALUE;

        for (ElevatorState state : states) {
            if (!canServe(state)) {
                continue;
            }

            int distance = Math.abs(state.getFloor() - request.getSourceFloor());
            int cost = distance + STOP_PENALTY * state.getStopsCount();

            // Μικρότερο κόστος κερδίζει· σε ισοβαθμία, το μικρότερο γράμμα (π.χ. A < C).
            if (cost < bestCost
                    || (cost == bestCost && state.getId().compareTo(bestId) < 0)) {
                bestCost = cost;
                bestId = state.getId();
            }
        }

        return bestId; // null -> 503 "no elevator available"
    }

    private boolean canServe(ElevatorState state) {
        String status = state.getsStatus();
return !status.equals("STOPPED")
                && !status.equals("STOPPING")
                && !status.equals("EMERGENCY")
                && !status.equals("OUT_OF_SERVICE");    }
}

