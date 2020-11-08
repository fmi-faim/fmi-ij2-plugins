
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

public class PointCloudSeriesRegistrationTest {

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
	public void testSeriesRegistration() throws InterruptedException, ExecutionException
	{
		// Create Point Arrays
		double[] x = { //
			0, 0, 1, 1, 1, 2, //
			1, 2.1, 2, 0.9, 2, 3, //
			2, 3, 2, 3, 3, 4 //
		};
		double[] y = { //
			0, 1, 1, 0, 1, 2, //
			1, 2, 0.9, 2, 2.1, 3, //
			1, 2, 2, 1, 2, 3 //
		};
		double[] z = { //
			0, 1, 0, 1, 1, 2, //
			1, 1, 2, 2, 2, 3, //
			0, 0, 0, 1, 1, 2 //
		};
		double[] frame = { //
			0, 0, 0, 0, 0, 0,//
			1, 1, 1, 1, 1, 1, //
			3, 3, 3, 3, 3, 3 //
		};

		Map<String, Object> inputMap = new HashMap<>();
		inputMap.put("transformType", PointCloudSeriesRegistration.TRANSLATION);
		inputMap.put("dim", PointCloudSeriesRegistration.DIM3D);
		inputMap.put("xCoords", x);
		inputMap.put("yCoords", y);
		inputMap.put("zCoords", z);
		inputMap.put("frame", frame);
		inputMap.put("range", 3);

		// Fit Model
		CommandService commandService = context.getService(CommandService.class);
		CommandModule module = commandService.run(PointCloudSeriesRegistration.class, true, inputMap).get();

		// Compare
		double[] expected = { //
			1, 0, 0,  0,   0, 1, 0,  0,   0, 0, 1,  0, //
			1, 0, 0, -1,   0, 1, 0, -1,   0, 0, 1, -1, //
			1, 0, 0, -2,   0, 1, 0, -1,   0, 0, 1,  0  //
		};
		double[] flatModels = (double[]) module.getOutput("flatModels");
		System.out.println(Arrays.toString(flatModels));
		assertArrayEquals("Models", expected, flatModels, 0.01);

		
		int[] frames = { 0, 1, 3 };
		int[] frameList = (int[]) module.getOutput("frameList");
		System.out.println(Arrays.toString(flatModels));
		assertArrayEquals("Frame list", frames, frameList);
		
	}

}
