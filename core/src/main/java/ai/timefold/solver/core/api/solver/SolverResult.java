package ai.timefold.solver.core.api.solver;

import java.util.Objects;

import ai.timefold.solver.core.api.domain.solution.PlanningSolution;

/**
 * A {@link PlanningSolution solution} together with metadata collected during solving.
 *
 * @param solution never null
 * @param solverRunInfo never null
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public record SolverResult<Solution_>(Solution_ solution, SolverRunInfo solverRunInfo) {

    public SolverResult {
        Objects.requireNonNull(solution, "The solution must not be null.");
        Objects.requireNonNull(solverRunInfo, "The solver run info must not be null.");
    }
}
