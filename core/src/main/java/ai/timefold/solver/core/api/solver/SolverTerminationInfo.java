package ai.timefold.solver.core.api.solver;

import java.util.List;
import java.util.Objects;

/**
 * Identifies the public termination categories that stopped a solver run.
 *
 * @param terminationTypeList never null
 */
public record SolverTerminationInfo(List<SolverTerminationType> terminationTypeList) {

    public SolverTerminationInfo {
        Objects.requireNonNull(terminationTypeList, "The termination type list must not be null.");
        terminationTypeList = List.copyOf(terminationTypeList);
    }

    public SolverTerminationInfo(SolverTerminationType terminationType) {
        this(List.of(terminationType));
    }

    public static SolverTerminationInfo notTerminated() {
        return new SolverTerminationInfo(SolverTerminationType.NOT_TERMINATED);
    }

    public static SolverTerminationInfo phasesCompleted() {
        return new SolverTerminationInfo(SolverTerminationType.PHASES_COMPLETED);
    }

    public boolean isTerminatedBy(SolverTerminationType terminationType) {
        return terminationTypeList.contains(terminationType);
    }
}
