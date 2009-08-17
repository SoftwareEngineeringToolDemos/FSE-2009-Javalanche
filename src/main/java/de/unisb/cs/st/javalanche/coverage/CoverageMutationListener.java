package de.unisb.cs.st.javalanche.coverage;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;

import de.unisb.cs.st.ds.util.io.XmlIo;
import de.unisb.cs.st.javalanche.mutation.properties.MutationProperties;
import de.unisb.cs.st.javalanche.mutation.properties.MutationProperties.RunMode;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;
import de.unisb.cs.st.javalanche.mutation.runtime.testDriver.MutationTestListener;

/**
 * @author Bernhard Gruen
 * 
 */
public class CoverageMutationListener implements MutationTestListener {

	private static Logger logger = Logger
			.getLogger(CoverageMutationListener.class);

	private static boolean isPermutated = false;
	private static HashSet<String> seenTests = new HashSet<String>();

	private static Long mutation_id = new Long(-1);
	private String testName = null;

	private boolean saveFiles = false;

	private static HashMap<String, HashMap<Integer, Integer>> classMap = new HashMap<String, HashMap<Integer, Integer>>(
			(int) (2048 * 1.33));

	private static HashMap<String, HashMap<Integer, Integer>> valueMap = new HashMap<String, HashMap<Integer, Integer>>();

	private static HashMap<String, Long> profilerMap = new HashMap<String, Long>();

	private static final HashSet<String> dontInstrumentSet = loadDontInstrument();

	// private static HashMap<String, Integer> idMap = new HashMap<String,
	// Integer>();
	// private static int idMapMasterSize = 0;

	public static HashMap<String, HashMap<Integer, Integer>> getLineCoverageMap() {
		return classMap;
	}

	public static HashMap<String, HashMap<Integer, Integer>> getValueMap() {
		return valueMap;
	}

	public static HashMap<String, Long> getProfilerMap() {
		return profilerMap;
	}

	public static HashSet<String> getDontInstrumentSet() {
		return dontInstrumentSet;
	}

	public static Long getMutationId() {
		return mutation_id;
	}

	public static String getMutationIdFileName() {
		if (mutation_id < 0) {
			return "PERMUTATED_" + Math.abs(mutation_id);
		} else {
			return mutation_id.toString();
		}
	}

	/*
	 * public static HashMap<String, Integer> getIdMap() { return idMap; }
	 */

	public CoverageMutationListener() {
		File dir = new File(CoverageProperties.TRACE_RESULT_DIR);
		if (!dir.exists()) {
			dir.mkdir();
		}

		dir = new File(CoverageProperties.TRACE_RESULT_DATA_DIR);
		if (!dir.exists()) {
			dir.mkdir();
		}

		dir = new File(CoverageProperties.TRACE_RESULT_LINE_DIR);
		if (!dir.exists()) {
			dir.mkdir();
		}

		if (MutationProperties.RUN_MODE == RunMode.TEST_PERMUTED) {
			isPermutated = true;
		}
	}

	private void createMutationDir() {
		File dir = new File(CoverageProperties.TRACE_RESULT_DATA_DIR
				+ getMutationIdFileName());
		if (!dir.exists()) {
			dir.mkdir();
		}

		dir = new File(CoverageProperties.TRACE_RESULT_LINE_DIR
				+ getMutationIdFileName());
		if (!dir.exists()) {
			dir.mkdir();
		}
	}

	public void start() {
		mutation_id = new Long(0);
		classMap.clear();
		valueMap.clear();
		saveFiles = true;
	}

	public void end() {
		writeProfilingData();
		writeDontInstrument();
		classMap.clear();
		valueMap.clear();
		saveFiles = false;
	}

	public void testStart(String testName) {
		this.testName = testName;
		if (isPermutated) {
			if (seenTests.contains(testName)) {
				seenTests.clear();
				long i = 1;
				File dir = new File(CoverageProperties.TRACE_RESULT_DATA_DIR
						+ "PERMUTATED_" + i);
				while (dir.exists()) {
					i++;
					dir = new File(CoverageProperties.TRACE_RESULT_DATA_DIR
							+ "PERMUTATED_" + i);
				}
				mutation_id = -i;
			}
			seenTests.add(testName);
		}

		classMap.clear();
		valueMap.clear();
		saveFiles = true;
	}

	public void testEnd(String testName) {
		createMutationDir();

		Tracer.getInstance().setDataCoverageDeactivated(true);
		Tracer.getInstance().setLineCoverageDeactivated(true);
		serializeHashMap();
		serializeValueMap();
		classMap.clear();
		valueMap.clear();
		Tracer.getInstance().setDataCoverageDeactivated(false);
		Tracer.getInstance().setLineCoverageDeactivated(false);
		;
		saveFiles = false;
	}

	public void mutationStart(Mutation mutation) {
		mutation_id = mutation.getId();
		classMap.clear();
		valueMap.clear();
		createMutationDir();
		saveFiles = true;
	}

	public void mutationEnd(Mutation mutation) {
		// serializeIdMap(mutation_id);
		classMap.clear();
		valueMap.clear();
		saveFiles = false;
	}

	private void writeProfilingData() {
		if (mutation_id != 0) {
			return;
		}
		XmlIo.toXML(profilerMap, CoverageProperties.TRACE_PROFILER_FILE);
	}

	private static HashSet<String> loadDontInstrument() {
		if (new File(CoverageProperties.TRACE_DONT_INSTRUMENT_FILE).exists()) {
			return XmlIo.get(CoverageProperties.TRACE_DONT_INSTRUMENT_FILE);
		}
		return new HashSet<String>();
	}

	private void writeDontInstrument() {
		if (mutation_id != 0) {
			return;
		}
		XmlIo.toXML(dontInstrumentSet,
				CoverageProperties.TRACE_DONT_INSTRUMENT_FILE);

	}

	private void serializeHashMap() {
		if (!saveFiles) {
			logger.warn("Double Call to serializeHashMap");
			return;
		}
		ObjectOutputStream oos = null;

		HashMap<String, HashMap<Integer, Integer>> classMapCopy = null;

		synchronized (classMap) {
			classMapCopy = new HashMap<String, HashMap<Integer, Integer>>(
					classMap);
		}
		if (classMapCopy.size() == 0 && CoverageProperties.TRACE_LINES) {
			logger.warn("Empty coverage map for test " + testName);
		}

		try {
			oos = new ObjectOutputStream(new BufferedOutputStream(
					new GZIPOutputStream(new FileOutputStream(
							CoverageProperties.TRACE_RESULT_LINE_DIR
									+ getMutationIdFileName() + "/" + testName
									+ ".gz"))));

			Set<String> ks = classMapCopy.keySet();

			HashMap<Integer, Integer> lineMap = new HashMap<Integer, Integer>();

			Iterator<String> it = ks.iterator();
			String s;
			Set<Integer> ks2;
			Iterator<Integer> it2;
			Integer i;

			oos.writeInt(classMapCopy.size());
			// System.out.print(testName+":");
			while (it.hasNext()) {
				s = it.next();
				oos.writeUTF(s);
				lineMap = classMapCopy.get(s);
				ks2 = lineMap.keySet();
				it2 = ks2.iterator();
				oos.writeInt(lineMap.size());
				// System.out.println(s);
				while (it2.hasNext()) {
					i = it2.next();
					oos.writeInt(i);
					oos.writeInt(lineMap.get(i));
					// System.out.println(i);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void serializeValueMap() {
		if (!saveFiles) {
			logger.warn("Double Call to serializeValueMap");
			return;
		}

		HashMap<String, HashMap<Integer, Integer>> valueMapCopy = null;

		synchronized (valueMap) {
			valueMapCopy = new HashMap<String, HashMap<Integer, Integer>>(
					valueMap);

		}
		if (valueMapCopy.size() == 0 && CoverageProperties.TRACE_RETURNS) {
			logger.warn("Empty value map for test " + testName);
		}
		ObjectOutputStream oos = null;

		try {
			oos = new ObjectOutputStream(new BufferedOutputStream(
					new GZIPOutputStream(new FileOutputStream(
							CoverageProperties.TRACE_RESULT_DATA_DIR
									+ getMutationIdFileName() + "/" + testName
									+ ".gz"))));
			HashMap<Integer, Integer> lineMap = new HashMap<Integer, Integer>();

			Set<String> ks = valueMapCopy.keySet();
			Iterator<String> it = ks.iterator();
			String s;
			Set<Integer> ks2;
			Iterator<Integer> it2;
			Integer i;

			oos.writeInt(valueMapCopy.size());
			// System.out.print(testName+":");
			while (it.hasNext()) {
				s = it.next();
				oos.writeUTF(s);
				lineMap = valueMapCopy.get(s);
				ks2 = lineMap.keySet();
				it2 = ks2.iterator();
				oos.writeInt(lineMap.size());
				// System.out.println(s);
				while (it2.hasNext()) {
					i = it2.next();
					oos.writeInt(i);
					oos.writeInt(lineMap.get(i));
					// System.out.println(i);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}