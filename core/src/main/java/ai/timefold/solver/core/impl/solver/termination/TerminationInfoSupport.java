package ai.timefold.solver.core.impl.solver.termination;

import java.util.ArrayList;
import java.util.List;

import ai.timefold.solver.core.api.solver.SolverTerminationInfo;
import ai.timefold.solver.core.api.solver.SolverTerminationType;
import ai.timefold.solver.core.impl.phase.scope.AbstractPhaseScope;
import ai.timefold.solver.core.impl.solver.scope.SolverScope;

import org.jspecify.annotations.Nullable;

/**
 * Translates internal termination implementations into public termination metadata.
 */
public final class TerminationInfoSupport {

    private TerminationInfoSupport() {
    }

    public static <Solution_> SolverTerminationInfo buildSolverTerminationInfo(
            SolverTermination<Solution_> termination, SolverScope<Solution_> solverScope) {
        var terminationTypeList = buildTriggeredSolverTerminationTypeList(termination, solverScope);
        return terminationTypeList.isEmpty() ? SolverTerminationInfo.phasesCompleted()
                : new SolverTerminationInfo(terminationTypeList);
    }

    public static <Solution_> @Nullable SolverTerminationInfo buildTriggeredPhaseTerminationInfo(
            PhaseTermination<Solution_> termination, AbstractPhaseScope<Solution_> phaseScope) {
        var terminationTypeList = buildTriggeredPhaseTerminationTypeListFromTermination(termination, phaseScope);
        return terminationTypeList.isEmpty() ? null : new SolverTerminationInfo(terminationTypeList);
    }

    private static <Solution_> List<SolverTerminationType> buildTriggeredSolverTerminationTypeList(
            Termination<Solution_> termination, SolverScope<Solution_> solverScope) {
        if (termination instanceof AndCompositeTermination<Solution_> compositeTermination) {
            return buildSolverCompositeTerminationTypeList(true, compositeTermination, solverScope);
        } else if (termination instanceof OrCompositeTermination<Solution_> compositeTermination) {
            return buildSolverCompositeTerminationTypeList(false, compositeTermination, solverScope);
        } else if (termination instanceof BasicPlumbingTermination<Solution_> basicPlumbingTermination) {
            return buildBasicPlumbingTerminationTypeList(basicPlumbingTermination, solverScope);
        } else if (termination instanceof ChildThreadPlumbingTermination<Solution_> childThreadPlumbingTermination) {
            return childThreadPlumbingTermination.isSolverTerminated(solverScope)
                    ? List.of(SolverTerminationType.TERMINATE_EARLY)
                    : List.of();
        } else if (termination instanceof SolverTermination<Solution_> solverTermination) {
            return solverTermination.isSolverTerminated(solverScope)
                    ? List.of(getTerminationType(termination))
                    : List.of();
        }
        return List.of();
    }

    private static <Solution_> List<SolverTerminationType> buildTriggeredPhaseTerminationTypeListFromTermination(
            Termination<Solution_> termination, AbstractPhaseScope<Solution_> phaseScope) {
        if (termination instanceof SolverBridgePhaseTermination<Solution_> bridgeTermination) {
            var solverTerminationTypeList =
                    buildTriggeredSolverTerminationTypeList(bridgeTermination.solverTermination, phaseScope.getSolverScope());
            if (!solverTerminationTypeList.isEmpty()) {
                return solverTerminationTypeList;
            } else if (bridgeTermination.solverTermination instanceof PhaseTermination<Solution_> phaseTermination) {
                return buildTriggeredPhaseTerminationTypeListFromTermination(phaseTermination, phaseScope);
            }
            return List.of();
        } else if (termination instanceof AndCompositeTermination<Solution_> compositeTermination) {
            return buildPhaseCompositeTerminationTypeList(true, compositeTermination, phaseScope);
        } else if (termination instanceof OrCompositeTermination<Solution_> compositeTermination) {
            return buildPhaseCompositeTerminationTypeList(false, compositeTermination, phaseScope);
        } else if (termination instanceof BasicPlumbingTermination<Solution_> basicPlumbingTermination) {
            return buildBasicPlumbingTerminationTypeList(basicPlumbingTermination, phaseScope.getSolverScope());
        } else if (termination instanceof ChildThreadPlumbingTermination<Solution_> childThreadPlumbingTermination) {
            return childThreadPlumbingTermination.isPhaseTerminated(phaseScope)
                    ? List.of(SolverTerminationType.TERMINATE_EARLY)
                    : List.of();
        } else if (termination instanceof PhaseTermination<Solution_> phaseTermination) {
            return phaseTermination.isPhaseTerminated(phaseScope)
                    ? List.of(getTerminationType(termination))
                    : List.of();
        }
        return List.of();
    }

    private static <Solution_> List<SolverTerminationType> buildBasicPlumbingTerminationTypeList(
            BasicPlumbingTermination<Solution_> basicPlumbingTermination, SolverScope<Solution_> solverScope) {
        if (!basicPlumbingTermination.isSolverTerminated(solverScope)) {
            return List.of();
        }
        return basicPlumbingTermination.isTerminateEarly()
                ? List.of(SolverTerminationType.TERMINATE_EARLY)
                : List.of(SolverTerminationType.PROBLEM_CHANGE);
    }

    private static <Solution_> List<SolverTerminationType> buildSolverCompositeTerminationTypeList(
            boolean allChildrenMustTerminate, AbstractCompositeTermination<Solution_> compositeTermination,
            SolverScope<Solution_> solverScope) {
        return buildCompositeTerminationTypeList(allChildrenMustTerminate, compositeTermination.solverTerminationList,
                child -> buildTriggeredSolverTerminationTypeList(child, solverScope));
    }

    private static <Solution_> List<SolverTerminationType> buildPhaseCompositeTerminationTypeList(
            boolean allChildrenMustTerminate, AbstractCompositeTermination<Solution_> compositeTermination,
            AbstractPhaseScope<Solution_> phaseScope) {
        var applicablePhaseTerminationList = compositeTermination.phaseTerminationList.stream()
                .filter(termination -> termination.isApplicableTo(phaseScope.getClass()))
                .toList();
        return buildCompositeTerminationTypeList(allChildrenMustTerminate, applicablePhaseTerminationList,
                child -> buildTriggeredPhaseTerminationTypeListFromTermination(child, phaseScope));
    }

    private static <Termination_ extends Termination<?>> List<SolverTerminationType> buildCompositeTerminationTypeList(
            boolean allChildrenMustTerminate, List<Termination_> terminationList,
            TerminationInfoFunction<Termination_> terminationInfoFunction) {
        var terminationTypeList = new ArrayList<SolverTerminationType>(terminationList.size());
        for (var termination : terminationList) {
            var childTerminationTypeList = terminationInfoFunction.apply(termination);
            if (!childTerminationTypeList.isEmpty()) {
                childTerminationTypeList.forEach(terminationType -> addIfAbsent(terminationTypeList, terminationType));
            } else if (allChildrenMustTerminate) {
                return List.of();
            }
        }
        return List.copyOf(terminationTypeList);
    }

    private static void addIfAbsent(List<SolverTerminationType> terminationTypeList,
            SolverTerminationType terminationType) {
        if (!terminationTypeList.contains(terminationType)) {
            terminationTypeList.add(terminationType);
        }
    }

    private static SolverTerminationType getTerminationType(Termination<?> termination) {
        if (termination instanceof TimeMillisSpentTermination<?>) {
            return SolverTerminationType.TIME_SPENT;
        } else if (termination instanceof UnimprovedTimeMillisSpentTermination<?>) {
            return SolverTerminationType.UNIMPROVED_TIME_SPENT;
        } else if (termination instanceof UnimprovedTimeMillisSpentScoreDifferenceThresholdTermination<?>) {
            return SolverTerminationType.UNIMPROVED_TIME_SPENT_SCORE_DIFFERENCE_THRESHOLD;
        } else if (termination instanceof BestScoreTermination<?>) {
            return SolverTerminationType.BEST_SCORE;
        } else if (termination instanceof BestScoreFeasibleTermination<?>) {
            return SolverTerminationType.BEST_SCORE_FEASIBLE;
        } else if (termination instanceof ScoreCalculationCountTermination<?>) {
            return SolverTerminationType.SCORE_CALCULATION_COUNT;
        } else if (termination instanceof MoveCountTermination<?>) {
            return SolverTerminationType.MOVE_COUNT;
        } else if (termination instanceof StepCountTermination<?>) {
            return SolverTerminationType.STEP_COUNT;
        } else if (termination instanceof UnimprovedStepCountTermination<?>) {
            return SolverTerminationType.UNIMPROVED_STEP_COUNT;
        } else if (termination instanceof DiminishedReturnsTermination<?, ?>) {
            return SolverTerminationType.DIMINISHED_RETURNS;
        }
        return SolverTerminationType.UNKNOWN;
    }

    private interface TerminationInfoFunction<Termination_ extends Termination<?>> {

        List<SolverTerminationType> apply(Termination_ termination);
    }
}
