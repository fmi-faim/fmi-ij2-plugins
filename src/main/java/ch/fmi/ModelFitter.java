/*-
 * #%L
 * A collection of plugins developed at the FMI Basel.
 * %%
 * Copyright (C) 2016 - 2020 FMI Basel
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

package ch.fmi;

import java.util.ArrayList;
import mpicbg.models.Affine2D;
import mpicbg.models.Affine3D;
import mpicbg.models.AffineModel2D;
import mpicbg.models.AffineModel3D;
import mpicbg.models.IllDefinedDataPointsException;
import mpicbg.models.Model;
import mpicbg.models.NotEnoughDataPointsException;
import mpicbg.models.Point;
import mpicbg.models.PointMatch;
import mpicbg.models.RigidModel2D;
import mpicbg.models.RigidModel3D;
import mpicbg.models.SimilarityModel2D;
import mpicbg.models.SimilarityModel3D;
import mpicbg.models.TranslationModel2D;
import mpicbg.models.TranslationModel3D;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, headless = true,
	menuPath = "FMI>Fit Transformation Model to Paired Point Sets")
public class ModelFitter implements Command {

	final static protected String TRANSLATION = "Translation";
	final static protected String RIGID = "Rigid";
	final static protected String SIMILARITY = "Similarity";
	final static protected String AFFINE = "Affine";
	final static protected String DIM2D = "2D";
	final static protected String DIM3D = "3D";

	@Parameter(label = "Type of Transformation", choices = { TRANSLATION, RIGID,
		SIMILARITY, AFFINE })
	private String transformType;

	@Parameter(label = "Dimensionality", choices = { DIM2D, DIM3D })
	private String dim;

	@Parameter(label = "Set 1 - X Coordinates")
	private double[] x1;

	@Parameter(label = "Set 1 - Y Coordinates")
	private double[] y1;

	@Parameter(label = "Set 1 - Z Coordinates", required = false)
	private double[] z1 = null;

	@Parameter(label = "Set 2 - X Coordinates")
	private double[] x2;

	@Parameter(label = "Set 2 - Y Coordinates")
	private double[] y2;

	@Parameter(label = "Set 2 - Z Coordinates", required = false)
	private double[] z2 = null;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] affine;

	private Model<?> model;

	@Override
	public void run() {
		// Choose model
		switch (transformType) {
			case TRANSLATION:
				model = dim.equals(DIM2D) ? //
					new TranslationModel2D() : new TranslationModel3D();
				break;
			case RIGID:
				model = dim.equals(DIM2D) ? //
					new RigidModel2D() : new RigidModel3D();
				break;
			case SIMILARITY:
				model = dim.equals(DIM2D) ? //
					new SimilarityModel2D() : new SimilarityModel3D();
				break;
			case AFFINE:
				model = dim.equals(DIM2D) ? //
					new AffineModel2D() : new AffineModel3D();
				break;
		}

		// Prepare point correspondences (assuming positional correspondence)
		assert x1.length == y1.length : "X and Y vectors for first point set need to be equal length";
		assert x2.length == y2.length : "X and Y vectors for second point set need to be equal length";
		assert x1.length == x2.length : "Both point sets need to have equal length";

		ArrayList<PointMatch> correspondences = new ArrayList<>();
		for (int i = 0; i < x1.length; i++) {
			double[] pos1 = new double[dim.equals(DIM2D) ? 2 : 3];
			pos1[0] = x1[i];
			pos1[1] = y1[i];
			if (dim.equals(DIM3D)) pos1[2] = z1[i];
			Point p1 = new Point(pos1);
			double[] pos2 = new double[dim.equals(DIM2D) ? 2 : 3];
			pos2[0] = x2[i];
			pos2[1] = y2[i];
			if (dim.equals(DIM3D)) pos2[2] = z2[i];
			Point p2 = new Point(pos2);
			correspondences.add(new PointMatch(p1, p2));
		}

		// Fit the model
		try {
			model.fit(correspondences);
		}
		catch (NotEnoughDataPointsException exc) {
			// TODO Auto-generated catch block
			exc.printStackTrace();
		}
		catch (IllDefinedDataPointsException exc) {
			// TODO Auto-generated catch block
			exc.printStackTrace();
		}

		// Retrieve results
		affine = new double[12];
		double[] temp;
		switch (dim) {
			case DIM2D:
				temp = new double[6];
				((Affine2D<?>) model).toArray(temp);
				map2DColsTo3DRows(temp, affine);
				break;
			case DIM3D:
				temp = new double[12];
				((Affine3D<?>) model).toArray(temp);
				mapColsToRows(temp, affine);
				break;
		}
	}

	private void map2DColsTo3DRows(double[] temp, double[] mat) {
		mat[0] = temp[0];
		mat[1] = temp[2];
		mat[2] = 0; //
		mat[3] = temp[4];
		mat[4] = temp[1];
		mat[5] = temp[3];
		mat[6] = 0; //
		mat[7] = temp[5];
		mat[8] = 0; //
		mat[9] = 0; //
		mat[10] = 1; //
		mat[11] = 0; //
	}

	private void mapColsToRows(double[] temp, double[] mat) {
		mat[0] = temp[0];
		mat[1] = temp[3];
		mat[2] = temp[6];
		mat[3] = temp[9];
		mat[4] = temp[1];
		mat[5] = temp[4];
		mat[6] = temp[7];
		mat[7] = temp[10];
		mat[8] = temp[2];
		mat[9] = temp[5];
		mat[10] = temp[8];
		mat[11] = temp[11];
	}
}
