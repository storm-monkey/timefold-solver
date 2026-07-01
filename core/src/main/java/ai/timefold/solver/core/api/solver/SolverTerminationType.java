package ai.timefold.solver.core.api.solver;

/**
 * Public categories of conditions that can stop a solver or one of its phases.
 */
public enum SolverTerminationType {
    NOT_TERMINATED,
    PHASES_COMPLETED,
    TERMINATE_EARLY,
    PROBLEM_CHANGE,
    TIME_SPENT,
    UNIMPROVED_TIME_SPENT,
    UNIMPROVED_TIME_SPENT_SCORE_DIFFERENCE_THRESHOLD,
    BEST_SCORE,
    BEST_SCORE_FEASIBLE,
    SCORE_CALCULATION_COUNT,
    MOVE_COUNT,
    STEP_COUNT,
    UNIMPROVED_STEP_COUNT,
    DIMINISHED_RETURNS,
    UNKNOWN
}
