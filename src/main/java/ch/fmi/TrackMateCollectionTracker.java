/*-
 * #%L
 * A collection of plugins developed at the FMI Basel.
 * %%
 * Copyright (C) 2016 - 2021 FMI Basel
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

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

import java.util.ArrayList;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.TrackModel;
import fiji.plugin.trackmate.tracking.LAPUtils;
import fiji.plugin.trackmate.tracking.TrackerKeys;
import fiji.plugin.trackmate.tracking.sparselap.SparseLAPTrackerFactory;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, headless = true, menuPath = "FMI>Track Spot Collection")
public class TrackMateCollectionTracker implements Command {

	@Parameter
	private LogService log;

	// -- Workflow inputs --

	@Parameter(label = "Frame interval")
	private double frameInterval;

	@Parameter(label = "Linking max distance")
	private double linkingMaxDistance = TrackerKeys.DEFAULT_LINKING_MAX_DISTANCE;

	@Parameter(label = "Gap closing max distance")
	private double closingMaxDistance = TrackerKeys.DEFAULT_GAP_CLOSING_MAX_DISTANCE;

	@Parameter(label = "Gap closing max frame gap")
	private int frameGap = TrackerKeys.DEFAULT_GAP_CLOSING_MAX_FRAME_GAP;

	@Parameter(label = "Spot IDs")
	private double[] spotID;

	@Parameter(label = "Spot frames")
	private double[] frame;

	@Parameter(label = "Spot radii")
	private double[] radius;

	@Parameter(label = "X Coordinates")
	private double[] x;

	@Parameter(label = "Y Coordinates")
	private double[] y;

	@Parameter(label = "Z Coordinates")
	private double[] z;

	@Parameter(label = "Spot qualities")
	private double[] quality;

	// -- Workflow outputs --

	@Parameter(type = ItemIO.OUTPUT)
	private int nSpotsFound;

	@Parameter(type = ItemIO.OUTPUT)
	private int nTracksFound;

	@Parameter(type = ItemIO.OUTPUT)
	private int[] keptSpotID;

	@Parameter(type = ItemIO.OUTPUT)
	private int[] trackID;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] keptFrame;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] keptX; // TODO not required

	@Parameter(type = ItemIO.OUTPUT)
	private double[] keptY; // TODO not required

	@Parameter(type = ItemIO.OUTPUT)
	private double[] keptZ; // TODO not required

	@Override
	public void run() {
		// Generate input model
		Model model = new Model();
		// TODO add spot analyzers etc. to the model
		model.beginUpdate();
		for (int i = 0; i < spotID.length; i++) {
			Spot spot = new Spot((int) spotID[i]);
			spot.putFeature(Spot.POSITION_X, x[i]);
			spot.putFeature(Spot.POSITION_Y, y[i]);
			spot.putFeature(Spot.POSITION_Z, z[i]);
			spot.putFeature(Spot.RADIUS, radius[i]);
			spot.putFeature(Spot.QUALITY, quality[i]);
			model.addSpotTo(spot, (int) frame[i]);
		}
		model.endUpdate();

		// Create settings
		Settings settings = new Settings();
		settings.dt = frameInterval;

		settings.trackerFactory = new SparseLAPTrackerFactory();
		settings.trackerSettings = LAPUtils.getDefaultLAPSettingsMap();
		settings.trackerSettings.put(TrackerKeys.KEY_LINKING_MAX_DISTANCE,
				linkingMaxDistance);
		settings.trackerSettings.put(TrackerKeys.KEY_GAP_CLOSING_MAX_DISTANCE,
				closingMaxDistance);
		settings.trackerSettings.put(TrackerKeys.KEY_GAP_CLOSING_MAX_FRAME_GAP,
				frameGap);

		// Run TrackMate (tracking only)
		TrackMate trackmate = new TrackMate(model, settings);

		// Process (spot detection and tracking)
		/*
		 * // do net check input; instead, make sure it is valid for tracking
		 * only if (!trackmate.checkInput()) { log.error("Configuration error: "
		 * + trackmate.getErrorMessage()); return; }
		 */

		ArrayList<Integer> spotIDlist = new ArrayList<>();
		ArrayList<Integer> trackIDlist = new ArrayList<>();
		ArrayList<Double> frameList = new ArrayList<>();
		ArrayList<Double> xList = new ArrayList<>();
		ArrayList<Double> yList = new ArrayList<>();
		ArrayList<Double> zList = new ArrayList<>();

		if (trackmate.execTracking()) {
			TrackModel trackmodel = model.getTrackModel();

			// fill outputs
			for (Integer currentTrackID : trackmodel.trackIDs(false)) {
				for (Spot spot : trackmodel.trackSpots(currentTrackID)) {
					spotIDlist.add(spot.ID());
					trackIDlist.add(currentTrackID);
					frameList.add(spot.getFeature(Spot.FRAME));
					xList.add(spot.getDoublePosition(0));
					yList.add(spot.getDoublePosition(1));
					zList.add(spot.getDoublePosition(2));
				}
			}
		}

		keptSpotID = Ints.toArray(spotIDlist);
		trackID = Ints.toArray(trackIDlist);
		keptFrame = Doubles.toArray(frameList);
		keptX = Doubles.toArray(xList);
		keptY = Doubles.toArray(yList);
		keptZ = Doubles.toArray(zList);

		nSpotsFound = model.getSpots().getNSpots(false);
		nTracksFound = model.getTrackModel().nTracks(false);
	}
}
