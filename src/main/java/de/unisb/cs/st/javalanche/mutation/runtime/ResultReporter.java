package de.unisb.cs.st.javalanche.mutation.runtime;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.unisb.cs.st.javalanche.invariants.runtime.InvariantObserver;
import de.unisb.cs.st.javalanche.mutation.properties.MutationProperties;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;
import de.unisb.cs.st.javalanche.mutation.results.MutationTestResult;
import de.unisb.cs.st.javalanche.mutation.results.persistence.QueryManager;
import de.unisb.cs.st.javalanche.mutation.runtime.testDriver.MutationTestListener;

/**
 *
 * Class that manages the results of the mutation testing and persits them to
 * the database.
 *
 *
 * @author David Schuler
 *
 */
public class ResultReporter implements MutationTestListener {

	private static Logger logger = Logger.getLogger(ResultReporter.class);

	/**
	 * All mutations results have been reported for.
	 */
	private List<Mutation> reportedMutations = new ArrayList<Mutation>();

	/**
	 * Counts the number of mutations that are reported to the db.
	 */
	private int reportCount;

	/**
	 * Reports the results of the given mutation.
	 *
	 * @param mutation
	 *            The mutation the result is reported for.
	 */
	private synchronized void report(Mutation mutation) {
		if (mutation == null) {
			throw new IllegalArgumentException(
					"Argument was null: " + mutation == null ? ", mutation"
							: "");
		}
		if (mutation.getMutationResult() == null) {
			throw new IllegalArgumentException(
					"Given mutation does not contain a mutation test result "
							+ mutation);
		}

		if (!reportedMutations.contains(mutation)) {
			reportedMutations.add(mutation);
		} else {
			String message = "Mutation " + mutation + " already reported ";
			logger.info(message);
			throw new RuntimeException(message);
		}
		if (MutationProperties.RUN_MODE == MutationProperties.RunMode.MUTATION_TEST_INVARIANT) {
			reportInvariantResults(mutation.getMutationResult());
		}
	}

	/**
	 * Add invariant results tie the given {@link MutationTestResult}.
	 *
	 * @param mutationTestResult
	 *            the result to add the invariant results
	 */
	private static void reportInvariantResults(
			MutationTestResult mutationTestResult) {
		InvariantObserver instance = InvariantObserver.getInstance();
		if (instance != null) {
			int totalViolatedInvariants = instance
					.getTotalInvariantViolations();
			int[] violatedInvariants = instance.getViolatedInvariantsArray();
			mutationTestResult.setTotalViolations(totalViolatedInvariants);
			mutationTestResult.setViolatedInvariants(violatedInvariants);
			mutationTestResult
					.setDifferentViolatedInvariants(violatedInvariants.length);
			InvariantObserver.reset();
		}
	}

	/**
	 * Persits the reported mutations to the database.
	 */
	public synchronized void persist() {
		logger.debug("Start storing " + reportedMutations.size()
				+ " mutation test results in db");
		QueryManager.updateMutations(reportedMutations);
		logger.debug("Stored " + reportedMutations.size()
				+ " mutation test results in db");
		reportedMutations.clear();
	}

	public void end() {
		persist();
	}

	/**
	 * Report the result of the mutation and store them to the db in regular
	 * intervals.
	 */
	public void mutationEnd(Mutation mutation) {
		report(mutation);
		reportCount++;
		if (reportCount % MutationProperties.SAVE_INTERVAL == 0) {
			logger.info("Reached save intervall. Saving "
					+ MutationProperties.SAVE_INTERVAL
					+ " mutations. Total mutations tested until now: "
					+ reportCount);
			persist();
		}
	}

	/**
	 * Not used
	 */
	public void mutationStart(Mutation mutation) {
	}

	/**
	 * Not used
	 */
	public void start() {
	}

	/**
	 * Not used
	 */
	public void testEnd(String testName) {
	}

	/**
	 * Not used
	 */
	public void testStart(String testName) {
	}

}
