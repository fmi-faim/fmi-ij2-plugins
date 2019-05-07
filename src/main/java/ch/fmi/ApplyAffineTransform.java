package ch.fmi;

import net.imglib2.Cursor;
import net.imglib2.RealRandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.AffineRandomAccessible;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.view.Views;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, headless = true, menuPath = "FMI>Apply 3D Affine Transform")
public class ApplyAffineTransform<T extends NumericType<T>> implements Command {
	@Parameter(label = "Image to be transformed")
	private Img<T> img;

	@Parameter(label = "Affine transformation matrix")
	private double[] m;

	@Parameter(label = "Apply inverse transform", required = false)
	private boolean applyInverse = false;

	@Parameter(type = ItemIO.OUTPUT)
	private Img<T> output;

	@Override
	public void run() {
		AffineTransform3D transform = new AffineTransform3D();
		transform.set(m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], m[8],
				m[9], m[10], m[11]);
		RealRandomAccessible<T> interpolated = Views.interpolate(
				Views.extendZero(img), new NLinearInterpolatorFactory<T>());
		AffineRandomAccessible<T, AffineGet> view = RealViews.affine(
				interpolated, applyInverse ? transform.inverse() : transform);

		output = img.factory().create(img);
		Cursor<T> cursorIn = Views.interval(view, img).cursor();
		Cursor<T> cursorOut = output.cursor();

		while (cursorIn.hasNext()) {
			cursorIn.fwd();
			cursorOut.fwd();
			cursorOut.get().set(cursorIn.get());
		}
	}
}
