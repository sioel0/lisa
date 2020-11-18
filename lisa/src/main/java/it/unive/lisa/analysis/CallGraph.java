package it.unive.lisa.analysis;

import java.util.Collection;

import it.unive.lisa.cfg.CFG;
import it.unive.lisa.cfg.statement.CFGCall;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.Identifier;

/**
 * A callgraph of the program to analyze, that knows how to resolve dynamic
 * targets of {@link CFGCall}s.
 * 
 * @author <a href="mailto:luca.negrini@unive.it">Luca Negrini</a>
 */
public interface CallGraph {

	/**
	 * Yields a collection containing all possible runtime targets of a
	 * {@link CFGCall}.
	 * 
	 * @param call the call to resolve
	 * @return a collection of all the possible runtime targets
	 */
	Collection<CFG> resolve(CFGCall call);

	/**
	 * Computes a fixpoint over the whole control flow graph, producing a
	 * {@link CFGWithAnalysisResults} for each {@link CFG} contained in this
	 * callgraph. Each result is computed with
	 * {@link CFG#fixpoint(Collection, AnalysisState, CallGraph, it.unive.lisa.util.workset.WorkingSet, int)}.
	 * Results of individual cfgs are then available through
	 * {@link #getAnalysisResultsOf(CFG)}.
	 * 
	 * @param <H>        the type of {@link HeapDomain} to compute
	 * @param <V>        the type of {@link ValueDomain} to compute
	 * @param entryState the entry state for the {@link CFG}s that are the
	 *                   entrypoints of the computation
	 */
	<H extends HeapDomain<H>, V extends ValueDomain<V>> void fixpoint(AnalysisState<H, V> entryState);

	/**
	 * Yields the results of the given analysis, identified by its class, on the
	 * given {@link CFG}. Results are provided as {@link CFGWithAnalysisResults}.
	 * 
	 * @param <H> the type of {@link HeapDomain} contained into the computed
	 *            abstract state
	 * @param <V> the type of {@link ValueDomain} contained into the computed
	 *            abstract state
	 * @param cfg the cfg whose fixpoint results needs to be retrieved
	 * @return the result of the fixpoint computation of {@code valueDomain} over
	 *         {@code cfg}
	 */
	<H extends HeapDomain<H>, V extends ValueDomain<V>> CFGWithAnalysisResults<H, V> getAnalysisResultsOf(CFG cfg);

	/**
	 * Resolves the given call to all of its possible runtime targets, and then
	 * computes an analysis state that abstracts the execution of the possible
	 * targets considering that they were given {@code parameters} as actual
	 * parameters. The abstract value of each parameter is computed on
	 * {@code entryState}.
	 * 
	 * @param <H>        the type of {@link HeapDomain} contained into the computed
	 *                   abstract state
	 * @param <V>        the type of {@link ValueDomain} contained into the computed
	 *                   abstract state
	 * @param call       the call to resolve and evaluate
	 * @param entryState the abstract analysis state when the call is reached
	 * @param parameters the expressions representing the actual parameters of the
	 *                   call
	 * @return an abstract analysis state representing the abstract result of the
	 *         cfg call. The {@link AnalysisState#getLastComputedExpression()} will
	 *         contain an {@link Identifier} pointing to the meta variable
	 *         containing the abstraction of the returned value
	 * @throws SemanticException if something goes wrong during the computation
	 */
	<H extends HeapDomain<H>, V extends ValueDomain<V>> AnalysisState<H, V> getAbstractResultOf(CFGCall call,
			AnalysisState<H, V> entryState, SymbolicExpression[] parameters) throws SemanticException;
}
