package ai.timefold.solver.core.api.solver;

import java.time.Duration;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

/**
 * Metadata collected during a solver run.
 *
 * @param solvingDuration time spent solving since the last start
 * @param scoreCalculationCount number of score calculations since the last start
 * @param moveEvaluationCount number of move evaluations since the last start
 * @param scoreCalculationSpeed average score calculations per second since the last start
 * @param moveEvaluationSpeed average move evaluations per second since the last start
 * @param problemSizeStatistics problem size statistics, null when no problem has been supplied yet
 * @param terminationInfo information about the termination categories that stopped the run
 */
public record SolverRunInfo(Duration solvingDuration,
        long scoreCalculationCount,
        long moveEvaluationCount,
        long scoreCalculationSpeed,
        long moveEvaluationSpeed,
        @Nullable ProblemSizeStatistics problemSizeStatistics,
        SolverTerminationInfo terminationInfo) {

    public SolverRunInfo {
        Objects.requireNonNull(solvingDuration, "The solving duration must not be null.");
        Objects.requireNonNull(terminationInfo, "The termination info must not be null.");
    }

    public static SolverRunInfo empty() {
        return new SolverRunInfo(Duration.ZERO, 0L, 0L, 0L, 0L, null, SolverTerminationInfo.notTerminated());
    }
}
