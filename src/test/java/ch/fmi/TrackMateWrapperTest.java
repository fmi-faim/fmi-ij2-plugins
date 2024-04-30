
package ch.fmi;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;

import ij.ImagePlus;

public class TrackMateWrapperTest {

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
	public void testTrackMate() throws InterruptedException, ExecutionException {
		CommandService commandService = context.getService(CommandService.class);

		// Prepare
		RandomAccessibleInterval<UnsignedByteType> img = ArrayImgs.unsignedBytes(16,
			16, 1, 1, 5);
		img.getAt(5, 7, 0, 0, 0).setReal(192);
		img.getAt(5, 6, 0, 0, 1).setReal(186);
		img.getAt(6, 6, 0, 0, 2).setReal(180);
		img.getAt(6, 7, 0, 0, 3).setReal(174);
		img.getAt(5, 7, 0, 0, 4).setReal(168);
		ImagePlus imp = ImageJFunctions.wrapUnsignedByte(img, "Test Image");
		assertNotNull(imp);
		Map<String, Object> inputMap = new HashMap<>();
		inputMap.put("imp", imp);
		inputMap.put("frameInterval", 0.5);

		// Run TrackMateWrapper
		CommandModule module = commandService.run(TrackMateWrapper.class, false,
			inputMap).get();

		// Compare
		int nSpotsFound = (int) module.getOutput("nSpotsFound");
		int nTracksFound = (int) module.getOutput("nTracksFound");
		double[] x = (double[]) module.getOutput("x");
		double[] y = (double[]) module.getOutput("y");
		double[] z = (double[]) module.getOutput("z");
		double[] t = (double[]) module.getOutput("t");
		double[] frame = (double[]) module.getOutput("frame");

		assertEquals(5, nSpotsFound);
		assertEquals(1, nTracksFound);

		System.out.println(Arrays.toString(x));
		System.out.println(Arrays.toString(y));
		System.out.println(Arrays.toString(z));
		System.out.println(Arrays.toString(t));

		Arrays.sort(x);
		Arrays.sort(y);
		Arrays.sort(t);
		Arrays.sort(frame);

		double[] x1 = { 5, 5, 5, 6, 6 };
		double[] y1 = { 6, 6, 7, 7, 7 };
		double[] z1 = { 0, 0, 0, 0, 0 };
		double[] t1 = { 0, 0.5, 1.0, 1.5, 2.0 };
		double[] frame1 = { 0, 1, 2, 3, 4 };

		assertArrayEquals(x1, x, 0.2);
		assertArrayEquals(y1, y, 0.2);
		assertArrayEquals(z1, z, 0.0001);
		assertArrayEquals(t1, t, 0.01);
		assertArrayEquals(frame1, frame, 0.01);
	}
}
