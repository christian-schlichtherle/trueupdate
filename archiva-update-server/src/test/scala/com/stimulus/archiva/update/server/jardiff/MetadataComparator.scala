/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jardiff

/**
 * @author Christian Schlichtherle
 */
private object MetadataComparator extends Comparator {
  def equals(entryInFile1: EntryInFile, entryInFile2: EntryInFile) = {
    val entry1 = entryInFile1.entry
    val entry2 = entryInFile2.entry
    assume(entry1.getName == entry2.getName)
    entry1.getTime == entry2.getTime &&
      entry1.getSize == entry2.getSize &&
      entry1.getCrc == entry2.getCrc
  }
}
