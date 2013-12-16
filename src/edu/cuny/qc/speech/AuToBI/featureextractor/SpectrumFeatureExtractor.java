/*  SpectrumFeatureExtractor.java

    Copyright (c) 2009-2011 Andrew Rosenberg

    This file is part of the AuToBI prosodic analysis package.

    AuToBI is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    AuToBI is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with AuToBI.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cuny.qc.speech.AuToBI.featureextractor;

import edu.cuny.qc.speech.AuToBI.SpectrumExtractor;
import edu.cuny.qc.speech.AuToBI.core.*;

import java.util.HashMap;
import java.util.List;

/**
 * SpectrumFeatureExtractor extracts a spectrum from a given WavData object.
 * <p/>
 * v1.4 SpectrumFeatureExtractor has changed to attach spectra to each region rather than cutting down to size
 * This is a more effective route to extracting context.
 */
@SuppressWarnings("unchecked")
public class SpectrumFeatureExtractor extends FeatureExtractor {
  private String feature_name;  // the name of the feature to hold pitch information
  private double frame_size; // The spectrum frame duration
  private double hamming_window; // The size of the hamming window used in the spectrum analysis


  /**
   * Constructs a new SpectrumFeatureExtractor to process wav_data and store the resulting Spectrum objects on
   * feature_name
   * <p/>
   * This uses a default frame size of 0.01s, and a hamming window of 0.02s.
   *
   * @param feature_name the feature name
   */
  public SpectrumFeatureExtractor(String feature_name) {
    this(feature_name, 0.01, 0.02);
  }

  public SpectrumFeatureExtractor(String feature_name, double frame_size, double hamming_window) {
    this.feature_name = feature_name;
    this.frame_size = frame_size;
    this.hamming_window = hamming_window;

    this.required_features.add("wav");
    this.extracted_features.add(feature_name);
  }

  /**
   * Extracts the spectrum and aligns information to regions.
   *
   * @param regions The regions to extract features from.
   * @throws FeatureExtractorException if there is a problem.
   */
  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    HashMap<WavData, Spectrum> cache = new HashMap<WavData, Spectrum>();
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute("wav")) {
        WavData wav = (WavData) r.getAttribute("wav");
        if (cache.containsKey(wav)) {
          r.setAttribute(feature_name, cache.get(wav));
        } else {
          if (wav != null) {
            SpectrumExtractor extractor = new SpectrumExtractor(wav);
            Spectrum spectrum = extractor.getSpectrum(frame_size, hamming_window);

            if (spectrum == null) {
              // AR: When writing tests, I couldn't get this case to fire. It seems unwise to remove this failsafe
              // though. If it happens during runtime, write a test for it.
              throw new FeatureExtractorException(
                  "Tried to extract the spectrum from segment with too few frames: " + r.getDuration() +
                      " seconds. (" +
                      wav.getNumSamples() + " frames)");
            }
            r.setAttribute(feature_name, spectrum);
            cache.put(wav, spectrum);
          }
        }
      }
    }
  }
}
