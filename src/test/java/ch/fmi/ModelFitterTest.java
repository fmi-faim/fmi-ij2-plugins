
package ch.fmi;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;

public class ModelFitterTest {

	private Context context;

	@Before
	public void initialize() {
		context = new Context();
	}

	@After
	public void disposeContext() {
		if (context != null) {
			context.dispose();
			context = null;
		}
	}

	@Test
	public void test3DSimilarityModel() throws InterruptedException,
		ExecutionException
	{
		// Create Point Arrays
		double[] x1 = { 0, 1, 1, 1 };
		double[] y1 = { 0, 0, 1, 1 };
		double[] z1 = { 0, 0, 0, 1 };
		double[] x2 = { 2, 2, 4, 4 };
		double[] y2 = { 0, -2, -2, -2 };
		double[] z2 = { 0, 0, 0, 2 };

		Map<String, Object> inputMap = new HashMap<>();
		inputMap.put("transformType", ModelFitter.SIMILARITY);
		inputMap.put("dim", ModelFitter.DIM3D);
		inputMap.put("x1", x1);
		inputMap.put("y1", y1);
		inputMap.put("z1", z1);
		inputMap.put("x2", x2);
		inputMap.put("y2", y2);
		inputMap.put("z2", z2);

		// Fit Model
		CommandService commandService = context.getService(CommandService.class);
		CommandModule module = commandService.run(ModelFitter.class, true, inputMap)
			.get();

		// Compare
		double[] expected = { //
			0, 2, 0, 2, //
			-2, 0, 0, 0, //
			0, 0, 2, 0 //
		};
		double[] result = (double[]) module.getOutput("affine");
		System.out.println(Arrays.toString(result));
		assertArrayEquals("Affine matrix", expected, result, 0.01);
	}

	@Test
	public void test3DAffineModel() throws InterruptedException,
		ExecutionException
	{
		// Create Point Arrays
		double[] x1 = { 0, 1, 1, 1 };
		double[] y1 = { 0, 0, 1, 1 };
		double[] z1 = { 0, 0, 0, 1 };
		double[] x2 = { 2, 2, 4, 4 };
		double[] y2 = { 0, -2, -3, -3 };
		double[] z2 = { 0, 0, 0, 2 };

		Map<String, Object> inputMap = new HashMap<>();
		inputMap.put("transformType", ModelFitter.AFFINE);
		inputMap.put("dim", ModelFitter.DIM3D);
		inputMap.put("x1", x1);
		inputMap.put("y1", y1);
		inputMap.put("z1", z1);
		inputMap.put("x2", x2);
		inputMap.put("y2", y2);
		inputMap.put("z2", z2);

		// Fit Model
		CommandService commandService = context.getService(CommandService.class);
		CommandModule module = commandService.run(ModelFitter.class, true, inputMap)
			.get();

		// Compare
		double[] expected = { //
			0, 2, 0, 2, //
			-2, -1, 0, 0, //
			0, 0, 2, 0 //
		};
		double[] result = (double[]) module.getOutput("affine");
		System.out.println(Arrays.toString(result));
		assertArrayEquals("Affine matrix", expected, result, 0.01);
	}

	@Test
	public void test2DAffineModel() throws InterruptedException,
		ExecutionException
	{
		// Create Point Arrays
		double[] x1 = { 0, 1, 1, 0 };
		double[] y1 = { 0, 0, 1, 1 };
		double[] x2 = { 2, 2, 4, 4 };
		double[] y2 = { 0, -2, -3, -1 };

		Map<String, Object> inputMap = new HashMap<>();
		inputMap.put("transformType", ModelFitter.AFFINE);
		inputMap.put("dim", ModelFitter.DIM2D);
		inputMap.put("x1", x1);
		inputMap.put("y1", y1);
		inputMap.put("x2", x2);
		inputMap.put("y2", y2);

		inputMap.put("z1", x1); // dummy input
		inputMap.put("z2", x1); // dummy input

		// Fit Model
		CommandService commandService = context.getService(CommandService.class);
		CommandModule module = commandService.run(ModelFitter.class, true, inputMap)
			.get();

		// Compare
		double[] expected = { //
			0, 2, 0, 2, //
			-2, -1, 0, 0, //
			0, 0, 1, 0 //
		};

		double[] result = (double[]) module.getOutput("affine");
		System.out.println(Arrays.toString(result));

		assertArrayEquals("Affine matrix", expected, result, 0.01);
	}
}
