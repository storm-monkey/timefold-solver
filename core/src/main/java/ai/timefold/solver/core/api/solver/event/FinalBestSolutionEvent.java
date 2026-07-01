package ai.timefold.solver.core.api.solver.event;

import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.solver.SolverResult;
import ai.timefold.solver.core.api.solver.SolverRunInfo;

/**
 * Delivered in a consumer thread at the end of the solving process and contains the final {@link PlanningSolution best
 * solution} found.
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public interface FinalBestSolutionEvent<Solution_> {
    /**
     * @return the {@link PlanningSolution best solution} found by the solver
     */
    Solution_ solution();

    /**
     * @return metadata collected during the solver run
     */
    default SolverRunInfo solverRunInfo() {
        return SolverRunInfo.empty();
    }

    /**
     * @return the {@link PlanningSolution best solution} and metadata collected during the solver run
     */
    default SolverResult<Solution_> solverResult() {
        return new SolverResult<>(solution(), solverRunInfo());
    }
}
