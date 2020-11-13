package ch.fmi;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;


public class ApplyAffineTransformPointsTest {

	private Context context;
	private CommandService commandService;

	@Before
	public void setUp() {
		context = new Context();
		commandService = context.service(CommandService.class);
	}

	@After
	public void tearDown() {
		if (context != null) {
			context.dispose();
			context = null;
		}
	}

	@Test
	public void testApplyInverse() throws InterruptedException, ExecutionException {
		double[] x = new double[] { 0, 1 };
		double[] y = new double[] { 0, 1 };
		double[] z = new double[] { 0, 1 };
		double[] affine = new double[] { //
			1, 0, 0, 2, //
			0, 1, 0, 3, //
			0, 0, 1, 4 //
		};
		Map<String, Object> inputMap = new HashMap<>();
		inputMap.put("xIn", x);
		inputMap.put("yIn", y);
		inputMap.put("zIn", z);
		inputMap.put("m", affine);

		CommandModule module = commandService.run(ApplyAffineTransformPoints.class, true, inputMap).get();
		double[] xOut = (double[]) module.getOutput("xOut");
		double[] yOut = (double[]) module.getOutput("yOut");
		double[] zOut = (double[]) module.getOutput("zOut");
		x[0] = -2;
		y[0] = -3;
		z[0] = -4;
		x[1] = -1;
		y[1] = -2;
		z[1] = -3;
		assertArrayEquals(x, xOut, 0);
		assertArrayEquals(y, yOut, 0);
		assertArrayEquals(z, zOut, 0);
	}

	@Test
	public void testApply() throws InterruptedException, ExecutionException {
		double[] x = new double[] { 0, 1 };
		double[] y = new double[] { 0, 1 };
		double[] z = new double[] { 0, 1 };
		double[] affine = new double[] { //
			1, 0, 0, 2, //
			0, 1, 0, 3, //
			0, 0, 1, 4 //
		};
		Map<String, Object> inputMap = new HashMap<>();
		inputMap.put("xIn", x);
		inputMap.put("yIn", y);
		inputMap.put("zIn", z);
		inputMap.put("m", affine);
		inputMap.put("inverse", false);

		CommandModule module = commandService.run(ApplyAffineTransformPoints.class, true, inputMap).get();
		double[] xOut = (double[]) module.getOutput("xOut");
		double[] yOut = (double[]) module.getOutput("yOut");
		double[] zOut = (double[]) module.getOutput("zOut");
		x[0] = 2;
		y[0] = 3;
		z[0] = 4;
		x[1] = 3;
		y[1] = 4;
		z[1] = 5;
		assertArrayEquals(x, xOut, 0);
		assertArrayEquals(y, yOut, 0);
		assertArrayEquals(z, zOut, 0);
	}
}
