package exptrunner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import atlasdsl.loader.*;
import atlassharedclasses.FaultInstance;
import faultgen.*;
import atlasdsl.*;
import atlasdsl.faults.Fault;

public class RunExperiment {

	private final static String ABS_SCRIPT_PATH = "/home/jharbin/academic/atlas/atlas-middleware/bash-scripts/";
	private final static String ABS_WORKING_PATH = "/home/jharbin/academic/atlas/atlas-middleware/expt-working/";
	private final static String ABS_MIDDLEWARE_PATH = "/home/jharbin/academic/atlas/atlas-middleware/expt-working/";
	private final static String ABS_MOOS_PATH = "/home/jharbin//academic/atlas/atlas-middleware/middleware-java/moos-sim/";
	
	private final static String ABS_ATLAS_JAR = "/home/jharbin/academic/atlas/atlas-middleware/expt-jar/atlas.jar";

	private static void exptLog(String s) {
		System.out.println(s);
	}

	private static void waitUntilMiddlewareTime(double time) throws FileNotFoundException {
		String pathToFile = ABS_MIDDLEWARE_PATH + "/logs/atlasTime.log";
		String target = Double.toString(time);
		boolean finished = false;
		BufferedReader reader = new BufferedReader(new FileReader(pathToFile));
		try {
			while (!finished) {
				TimeUnit.MILLISECONDS.sleep(100);
				while (reader.ready()) {
					String line = reader.readLine();
					Double lineVal = Double.valueOf(line);
					exptLog(line + "-" + target);
					if (lineVal >= time) {
						finished = true;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}





	private static void scanExperiment(Mission mission, String exptTag, ExptParams eparams) {
		Process middleware;

		int faultInstanceFileNum = 0;
		Runtime r = Runtime.getRuntime();

		while (!eparams.completed()) {
			// TODO: generate fresh MOOS code if required - need to specify the MOOSTimeWarp
			// here?

			String faultInstanceFileName = "expt_" + exptTag + Integer.toString(faultInstanceFileNum);
			exptLog("Running experiment with fault instance file " + faultInstanceFileName);
			// Generate a fault instance file for the experiment according to the experiment
			// parameters
			FaultFileCreator ffc = new FaultFileCreator(mission, ABS_WORKING_PATH);
			List<FaultInstance> outputFaultInstances = eparams.specificFaults();

			try {
				ffc.writeFaultDefinitionFile(ABS_WORKING_PATH + faultInstanceFileName, outputFaultInstances);

				// Launch the MOOS code, middleware and CI as separate subprocesses
				
				// TODO: if launching an experiment with more robots, need to ensure individual launch scripts are
				// generated in the MOOS code
				ExptHelper.startScript(ABS_MOOS_PATH, "launch_shoreside.sh");
				ExptHelper.startScript(ABS_MOOS_PATH, "launch_ella.sh");
				ExptHelper.startScript(ABS_MOOS_PATH, "launch_frank.sh");
				ExptHelper.startScript(ABS_MOOS_PATH, "launch_gilda.sh");
				ExptHelper.startScript(ABS_MOOS_PATH, "launch_henry.sh");

				exptLog("Started MOOS launch scripts");
				// Sleep until MOOS is ready
				TimeUnit.MILLISECONDS.sleep(1000);

				String[] middlewareOpts = { faultInstanceFileName, " false" };
				middleware = ExptHelper.startNewJavaProcess("-jar " + ABS_ATLAS_JAR, "middleware.core.ATLASMain", middlewareOpts,	ABS_WORKING_PATH);

				// Sleep until the middleware is ready, then start the CI
				TimeUnit.MILLISECONDS.sleep(1000);

				// CI not starting properly as a process, call it via a script
				exptLog("Starting CI");
				// TODO: fix absolute paths when working
				ExptHelper.startScript("/home/jharbin/academic/atlas/atlas-middleware/expt-working", "run-ci.sh");

				// wait until the end condition for the middleware - time elapsed?
				waitUntilMiddlewareTime(eparams.getTimeLimit());
				exptLog("Middleware end time reached");
				exptLog("Destroying middleware processes");
				middleware.destroy();
				ExptHelper.startCmd(ABS_SCRIPT_PATH, "terminate.sh");
				exptLog("Kill MOOS / Java processes command sent");
				exptLog("Destroy commands completed");

				// Read and process the result files from the experiment
				eparams.logResults(ABS_WORKING_PATH + "logs");

				// Modify the experiment spec data (either by using the results for mutation,
				// or according to predefined template)
				eparams.advance();
				faultInstanceFileNum++;
				exptLog("Waiting to restart");
				TimeUnit.MILLISECONDS.sleep(1000);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}



	public static void main(String[] args) {
		DSLLoader loader = new GeneratedDSLLoader();
		Mission mission;
		try {
			mission = loader.loadMission();
			Optional<Fault> f_o = mission.lookupFaultByName("SPEEDFAULT");
			if (f_o.isPresent()) {
				Fault f = f_o.get();
				// TODO: Read args to launch appropriate experiment
				ExptParams ep = new SingleFaultCoverageExpt(0.0, 999.0, 999.0, 100.0, f);
				scanExperiment(mission, "coverage", ep);
				exptLog("Done");
			}
		} catch (DSLLoadFailed e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
