package ch.fmi;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.detection.DetectorKeys;
import fiji.plugin.trackmate.detection.LogDetectorFactory;
import fiji.plugin.trackmate.features.FeatureFilter;
import fiji.plugin.trackmate.features.spot.SpotContrastAnalyzerFactory;
import fiji.plugin.trackmate.features.spot.SpotIntensityAnalyzerFactory;
import fiji.plugin.trackmate.features.spot.SpotRadiusEstimatorFactory;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.filter.ThresholdToSelection;
import ij.process.ImageProcessor;

import java.util.ArrayList;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, headless = true, menuPath = "FMI>Spot Detection (Subpixel localization)")
public class TrackMateSpotDetector implements Command {
	@Parameter
	private LogService log;

	@Parameter(label = "Input image")
	private ImagePlus imp;

	@Parameter(label = "ROI mask", required = false)
	private ImagePlus mask;

	@Parameter(label = "Spot radius")
	private double spotSize = DetectorKeys.DEFAULT_RADIUS;

	@Parameter(label = "Spot quality threshold")
	private double spotThreshold = DetectorKeys.DEFAULT_THRESHOLD;

	@Parameter(label = "Filter max quality spot per frame", required = false)
	private boolean filterMaxQuality = false;

	@Parameter(label = "Remove calibration?")
	private boolean removeCalibration = false;

	@Parameter(type = ItemIO.OUTPUT)
	private int nSpotsFound;

	@Parameter(type = ItemIO.OUTPUT)
	private int[] spotID;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] x;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] y;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] z;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] frame;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] radius;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] quality;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] totalIntensity;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] meanIntensity;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] estDiameter;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] contrast;

	@Override
	public void run() {
		// Set mask ROI on input image
		if (mask != null) {
			mask.getProcessor().setThreshold(1.0, Double.POSITIVE_INFINITY,
					ImageProcessor.NO_LUT_UPDATE);
			Roi roi = ThresholdToSelection.run(mask);
			imp.setRoi(roi);
		}

		// Create TrackMate instance with settings
		Model model = new Model();

		// optionally remove calibration from imp
		if (removeCalibration) {
			imp.setCalibration(null);
		}

		Settings settings = new Settings();
		settings.setFrom(imp);

		// TODO make detector choice optional
		settings.detectorFactory = new LogDetectorFactory<>();

		settings.detectorSettings = settings.detectorFactory
				.getDefaultSettings();
		settings.detectorSettings.put( //
				DetectorKeys.KEY_DO_SUBPIXEL_LOCALIZATION, true);
		settings.detectorSettings.put( //
				DetectorKeys.KEY_RADIUS, spotSize);
		settings.detectorSettings.put( //
				DetectorKeys.KEY_THRESHOLD, spotThreshold);
		settings.addSpotAnalyzerFactory(new SpotIntensityAnalyzerFactory<>());
		settings.addSpotAnalyzerFactory(new SpotRadiusEstimatorFactory<>());
		settings.addSpotAnalyzerFactory(new SpotContrastAnalyzerFactory<>());
		settings.addSpotAnalyzerFactory(new MaxQualitySpotAnalyzerFactory<>());

		if (filterMaxQuality) {
			settings.addSpotFilter(new FeatureFilter(
					MaxQualitySpotAnalyzerFactory.HAS_MAX_QUALITY_IN_FRAME,
					0.5, true));
		}

		TrackMate trackmate = new TrackMate(model, settings);

		// Prepare lists to collect results
		ArrayList<Integer> spotIDlist = new ArrayList<>();
		ArrayList<Double> xList = new ArrayList<>();
		ArrayList<Double> yList = new ArrayList<>();
		ArrayList<Double> zList = new ArrayList<>();
		ArrayList<Double> frameList = new ArrayList<>();
		ArrayList<Double> radiusList = new ArrayList<>();
		ArrayList<Double> qualityList = new ArrayList<>();
		ArrayList<Double> totalIntensityList = new ArrayList<>();
		ArrayList<Double> meanIntensityList = new ArrayList<>();
		ArrayList<Double> estDiameterList = new ArrayList<>();
		ArrayList<Double> contrastList = new ArrayList<>();
		// TODO add other outputs

		if (trackmate.execDetection() && trackmate.computeSpotFeatures(true)
				&& trackmate.execSpotFiltering(true)) {
			// Get spot collection (all spots)
			SpotCollection spotCollection = model.getSpots();
			for (Spot spot : spotCollection.iterable(false)) {
				spotIDlist.add(spot.ID());
				xList.add(spot.getDoublePosition(0));
				yList.add(spot.getDoublePosition(1));
				zList.add(spot.getDoublePosition(2));
				frameList.add(spot.getFeature(Spot.FRAME));
				radiusList.add(spot.getFeature(Spot.RADIUS));
				qualityList.add(spot.getFeature(Spot.QUALITY));
				totalIntensityList
						.add(spot
								.getFeature(SpotIntensityAnalyzerFactory.TOTAL_INTENSITY));
				meanIntensityList
						.add(spot
								.getFeature(SpotIntensityAnalyzerFactory.MEAN_INTENSITY));
				estDiameterList
						.add(spot
								.getFeature(SpotRadiusEstimatorFactory.ESTIMATED_DIAMETER));
				contrastList.add(spot
						.getFeature(SpotContrastAnalyzerFactory.KEY));
			}
		} else {
			log.warn(trackmate.getErrorMessage());
		}

		// Get results (spot list)
		spotID = Ints.toArray(spotIDlist);
		x = Doubles.toArray(xList);
		y = Doubles.toArray(yList);
		z = Doubles.toArray(zList);
		frame = Doubles.toArray(frameList);
		radius = Doubles.toArray(radiusList);
		quality = Doubles.toArray(qualityList);
		totalIntensity = Doubles.toArray(totalIntensityList);
		meanIntensity = Doubles.toArray(meanIntensityList);
		estDiameter = Doubles.toArray(estDiameterList);
		contrast = Doubles.toArray(contrastList);

		// Return summary values
		nSpotsFound = model.getSpots().getNSpots(false);
	}
}
