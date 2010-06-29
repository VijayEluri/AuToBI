/*  TextGridReader.java

    Copyright 2009-2010 Andrew Rosenberg

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
package edu.cuny.qc.speech.AuToBI;

import java.util.List;
import java.util.ArrayList;
import java.io.*;

/**
 * Read a TextGrid and generate a list of Words.
 * <p/>
 * The names of orthogonal, tones and breaks tiers in the TextGrid can be specified or standard "words", "tones",
 * "breaks" can be used.
 */
public class TextGridReader {

  protected String filename;          // the name of the textgrid file
  protected String charsetName;  // the name of the character set of the file to read.

  protected String words_tier_name;   // the name of the words tier
  protected String tones_tier_name;   // the name of the tones tier
  protected String breaks_tier_name;  // the name of the breaks tier

  protected Tier words_tier;   // a words Tier object
  protected Tier tones_tier;   // a tones Tier object
  protected Tier breaks_tier;  // a breaks Tier object

  /**
   * Constructs a new TextGridReader for a TextGrid file with default tier names.
   *
   * @param filename the filename to read
   */
  public TextGridReader(String filename) {
    this.filename = filename;
  }

  /**
   * Constructs a new TextGridReader for a TextGrid file with default tier names.
   *
   * @param filename    the filename to read
   * @param charsetName the name of the character set for the input
   */
  public TextGridReader(String filename, String charsetName) {
    this.filename = filename;
    this.charsetName = charsetName;
  }

  /**
   * Constructs a new TextGridReader with supplied tier names
   *
   * @param words_tier_name  the words tier
   * @param tones_tier_name  the tones tier
   * @param breaks_tier_name the breaks tier
   */
  public TextGridReader(String words_tier_name, String tones_tier_name, String breaks_tier_name) {
    this.words_tier_name = words_tier_name;
    this.tones_tier_name = tones_tier_name;
    this.breaks_tier_name = breaks_tier_name;
  }

  /**
   * Constructs a new TextGridReader with specified file and tier names.
   *
   * @param filename         the file name
   * @param words_tier_name  the name of the orthogonal tier
   * @param tones_tier_name  the name of the tones tier
   * @param breaks_tier_name the name of the breaks tier
   */
  public TextGridReader(String filename, String words_tier_name, String tones_tier_name, String breaks_tier_name) {
    this.filename = filename;
    this.words_tier_name = words_tier_name;
    this.tones_tier_name = tones_tier_name;
    this.breaks_tier_name = breaks_tier_name;
  }

  /**
   * Constructs a new TextGridReader with specified file and tier names.
   *
   * @param filename         the file name
   * @param words_tier_name  the name of the orthogonal tier
   * @param tones_tier_name  the name of the tones tier
   * @param breaks_tier_name the name of the breaks tier
   * @param charsetName      the name of the character set for the input
   */
  public TextGridReader(String filename, String words_tier_name, String tones_tier_name, String breaks_tier_name,
                        String charsetName) {
    this.filename = filename;
    this.words_tier_name = words_tier_name;
    this.tones_tier_name = tones_tier_name;
    this.breaks_tier_name = breaks_tier_name;
    this.charsetName = charsetName;
  }

  /**
   * Generates a list of words from the associated TextGrid file.
   * <p/>
   * A list of words is generated, available ToBI information is aligned to them, and checked for consistency with the
   * standard.
   * <p/>
   * This is the main entry point for this class.
   * <p/>
   * Typical Usage:
   * <p/>
   * TextGridReader reader = new TextGridReader(filename) List<Words> data_points = reader.readWords();
   *
   * @return A list of words with from the TextGrid
   * @throws IOException     if there is a reader problem
   * @throws AuToBIException if there is an alignment problem
   */
  public List<Word> readWords() throws IOException, AuToBIException {
    AuToBIFileReader file_reader;
    if (charsetName != null) {
      file_reader = new AuToBIFileReader(filename, charsetName);
    } else {
      file_reader = new AuToBIFileReader(filename);
    }

    Tier tier;
    tier = readTextGridTier(file_reader);  // Remove TextGrid header
    do {
      tier = readTextGridTier(file_reader);

      if (words_tier_name != null) {
        if (tier.name.equals(words_tier_name)) {
          words_tier = tier;
        }
      } else if (tier.name != null && (tier.name.equals("words") || tier.name.equals("orthographic"))) {
        words_tier = tier;
      }

      if (tones_tier_name != null) {
        if (tier.name.equals(tones_tier_name)) {
          tones_tier = tier;
        }
      } else if (tier.name != null && tier.name.equals("tones")) {
        tones_tier = tier;
      }

      if (breaks_tier_name != null) {
        if (tier.name.equals(breaks_tier_name)) {
          tones_tier = tier;
        }
      } else if (tier.name != null && tier.name.equals("breaks")) {
        breaks_tier = tier;
      }

    } while (tier.name != null);

    List<Word> words = generateWordList(words_tier.getRegions());

    if (words_tier == null) {
      String tier_name = words_tier_name == null ? "'words' or 'orthographic'" : words_tier_name;
      throw new AuToBIException("No words tier found with name, " + tier_name);
    }

    if (tones_tier != null) {
      AlignmentUtils.copyToBITones(words, tones_tier.getRegions());
      if (breaks_tier == null) {
        AuToBIUtils.warn(
            "No specified breaks tier found.  Default breaks will be generated from phrase ending tones in the tones tier.");
        ToBIUtils.generateBreaksFromTones(words);
      } else {
        AlignmentUtils.copyToBIBreaks(words, breaks_tier.getRegions());
        ToBIUtils.checkToBIAnnotations(words);
      }
    } else if (breaks_tier != null) {
      AlignmentUtils.copyToBIBreaks(words, breaks_tier.getRegions());
      AuToBIUtils
          .warn("No specified tones tier found.  Default phrase ending tones will be generated from breaks tier.");
      ToBIUtils.generateBreaksFromTones(words);
    }

    return words;
  }


  /**
   * Converts the list of regions held in a Tier to a list of words.
   * <p/>
   * Omits silent regions when creating the list of words.
   * <p/>
   * Note: words can hold ToBI annotations, while regions are more general objects.
   *
   * @param regions the regions to convert
   * @return a list of words
   */
  protected List<Word> generateWordList(List<Region> regions) {
    List<Word> words = new ArrayList<Word>();
    for (Region r : regions) {
      if (!isSilentRegion(r.getLabel())) {
        words.add(new Word(r.getStart(), r.getEnd(), r.getLabel(), null, r.getFile()));
      }
    }
    return words;
  }

  /**
   * Returns true if the label indicates that the region represents silence.
   * <p/>
   * Currently this matches the strings "#", ">brth", "}sil", "endsil", "sil", as well as, null and empty strings
   * <p/>
   * TODO Allow the list of silent labels to be set by the user or through command line parameters.
   *
   * @param label the region to check
   * @return true if r is a silent region
   */
  private boolean isSilentRegion(String label) {

    // put 'sil' back
    if (label.length() > 0 && !label.matches("(#|>brth|}sil|endsil|_|_\\*_|\\*_|_\\*)")) {
      return false;
    }
    return true;
  }

  /**
   * Generates a TextGridTier from the supplied AuToBIFileReader.
   *
   * @param reader The AuToBIFileReader
   * @return the Tier
   * @throws IOException if there is no tier to be read or if there is a problem with the reader
   */
  public Tier readTextGridTier(AuToBIFileReader reader) throws IOException {
    TextGridTier tier = new TextGridTier();
    tier.readTier(reader);
    return tier;
  }
}