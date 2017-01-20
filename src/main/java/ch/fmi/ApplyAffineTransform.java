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

	@Parameter(type = ItemIO.OUTPUT)
	private Img<T> output;

	@Override
	public void run() {
		/*
		AffineModel3D aff = new AffineModel3D();
		aff.set(m[0], m[1], m[2], m[3],
				m[4], m[5], m[6], m[7],
				m[8], m[9], m[10], m[11]);
		ImageTransform<T> transform = new ImageTransform<>(img, img, aff, new NLinearInterpolatorFactory<T>(), img.factory());
		
		output = transform.getResult();
		*/
		/*
		try {
			Affine3D<T> aff = new Affine3D<>(img, m, Affine3D.Mode.LINEAR);
			output = aff; // interval? zeroMin?
		} catch (Exception exc) {
			// Auto-generated catch block
			exc.printStackTrace();
		}
		*/
		AffineTransform3D transform = new AffineTransform3D();
		transform.set(m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], m[8], m[9], m[10], m[11]);
		RealRandomAccessible<T> interpolated = Views.interpolate(Views.extendZero(img), new NLinearInterpolatorFactory<T>());
		AffineRandomAccessible<T, AffineGet> view = RealViews.affine(interpolated, transform);
		
		output = img.factory().create(img, img.firstElement());
		Cursor<T> cursorIn = Views.interval(view, img).cursor();
		Cursor<T> cursorOut = output.cursor();
		//RandomAccess<T> randomAccessOut = view.randomAccess();
		
		while (cursorIn.hasNext()) {
			cursorIn.fwd();
			cursorOut.fwd();
			//randomAccessOut.setPosition(cursorIn);
			//randomAccessOut.get().set(cursorIn.get());
			cursorOut.get().set(cursorIn.get());
		}
		
		//RandomAccess<T> ra = img.randomAccess();
		//RealRandomAccessible<T> rra = Views.interpolate(ra, new NLinearInterpolatorFactory<T>());
		//AffineRandomAccessible<Object, AffineGet> transformed = RealViews.affine(rra, transform);
	}

}
