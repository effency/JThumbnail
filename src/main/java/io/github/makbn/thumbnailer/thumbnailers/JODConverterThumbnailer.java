/*
 * regain/Thumbnailer - A file search engine providing plenty of formats (Plugin)
 * Copyright (C) 2011  Come_IN Computerclubs (University of Siegen)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: Come_IN-Team <come_in-team@listserv.uni-siegen.de>
 */

package io.github.makbn.thumbnailer.thumbnailers;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.ExistingProcessAction;
import org.jodconverter.local.office.LocalOfficeManager;

import io.github.makbn.thumbnailer.AppSettings;
import io.github.makbn.thumbnailer.ThumbnailerException;
import io.github.makbn.thumbnailer.model.ResizeParameters;
import io.github.makbn.thumbnailer.util.IOUtil;
import io.github.makbn.thumbnailer.util.Platform;
import io.github.makbn.thumbnailer.util.TemporaryFilesManager;
import io.github.makbn.thumbnailer.util.mime.MimeTypeDetector;

public abstract class JODConverterThumbnailer extends AbstractThumbnailer {

  /**
   * How long may a service work take? (in ms)
   */
  private static final long TIMEOUT = 300000;
  private static Logger mLog = LogManager.getLogger("JODConverterThumbnailer");
  /**
   * JOD Office Manager
   */
  protected static LocalOfficeManager officeManager = null;
  /**
   * JOD Converter
   */
  protected static LocalConverter officeConverter = null;
  /**
   * Thumbnail Extractor for OpenOffice Files
   */
  protected OpenOfficeThumbnailer ooo_thumbnailer = null;
  /**
   * MimeIdentification
   */
  protected MimeTypeDetector mimeTypeDetector = null;
  private TemporaryFilesManager temporaryFilesManager = null;
  private final long counter = 0;

  public JODConverterThumbnailer() {
    ooo_thumbnailer = new OpenOfficeThumbnailer();
    mimeTypeDetector = new MimeTypeDetector();
    temporaryFilesManager = new TemporaryFilesManager();
  }

  /**
   * Check if a connection to OpenOffice is established.
   *
   * @return True if connected.
   */
  public static boolean isConnected() {
    return officeManager != null && officeManager.isRunning();
  }

  /**
   * Stop the OpenOffice Process and disconnect.
   */
  public static void disconnect() {
    // close the connection
    if (officeManager != null) {
      try {
        officeManager.stop();
      } catch (OfficeException e) {
        mLog.warn("JODConverterThumbnailer", e);
      }
    }
    officeManager = null;
  }

  /**
   * Start OpenOffice-Service and connect to it. (Does not reconnect if already connected.)
   */
  public void connect() {
    connect(false);
  }

  /**
   * Start OpenOffice-Service and connect to it.
   *
   * @param forceReconnect
   *          Connect even if he is already connected.
   */
  public void connect(boolean forceReconnect) {
    if (!forceReconnect && isConnected())
      return;

    officeManager = LocalOfficeManager.builder().portNumbers(AppSettings.DRIVE_OPENOFFICE_PORT).processTimeout(TIMEOUT)
        .maxTasksPerProcess(1000).existingProcessAction(ExistingProcessAction.CONNECT_OR_KILL).disableOpengl(true)
        .officeHome(AppSettings.DRIVE_OPENOFFICE_SERVER_PATH).build();

    try {
      officeManager.start();
      mLog.warn("OpenOffice/LibreOffice server started!");
    } catch (OfficeException e) {
      mLog.warn(e);
    }
    officeConverter = LocalConverter.builder().officeManager(officeManager).build();
  }

  @Override
  public void close() throws IOException {
    try {
      try {
        temporaryFilesManager.deleteAllTempfiles();
        ooo_thumbnailer.close();
      } finally {
        disconnect();
      }
    } finally {
      super.close();
    }
  }

  /**
   * Generates a thumbnail of Office files.
   *
   * @param input
   *          Input file that should be processed
   * @param output
   *          File in which should be written
   * @throws IOException
   *           If file cannot be read/written
   * @throws ThumbnailerException
   *           If the creating thumbnail process failed.
   */
  @Override
  public void generateThumbnail(File input, File output, ResizeParameters params)
      throws IOException, ThumbnailerException {
    if (!isConnected())
      connect();

    File outputTmp = null;
    try {
      outputTmp = File.createTempFile("jodtemp", "." + getStandardOpenOfficeExtension());

      if (Platform.isWindows())
        input = new File(input.getAbsolutePath().replace("\\\\", "\\"));

      try {
        officeConverter.convert(input).to(outputTmp).execute();
      } catch (OfficeException e) {
        mLog.warn(e);
        throw new ThumbnailerException();

      }
      if (outputTmp.length() == 0) {
        throw new ThumbnailerException("Could not convert into OpenOffice-File (file was empty)...");
      }

      ooo_thumbnailer.generateThumbnail(outputTmp, output, params);

    } finally {
      IOUtil.deleteQuietlyForce(outputTmp);
    }
  }

  /**
   * Generate a Thumbnail of the input file. (Fix file ending according to MIME-Type).
   *
   * @param input
   *          Input file that should be processed
   * @param output
   *          File in which should be written
   * @param mimeType
   *          MIME-Type of input file (null if unknown)
   * @throws IOException
   *           If file cannot be read/written
   * @throws ThumbnailerException
   *           If the thumbnailing process failed.
   */
  @Override
  public void generateThumbnail(File input, File output, String mimeType, ResizeParameters params)
      throws IOException, ThumbnailerException {
    String ext = FilenameUtils.getExtension(input.getName());
    if (!mimeTypeDetector.doesExtensionMatchMimeType(ext, mimeType)) {
      String newExt;
      if ("application/zip".equals(mimeType))
        newExt = getStandardZipExtension();
      else if ("application/vnd.ms-office".equals(mimeType))
        newExt = getStandardOfficeExtension();
      else
        newExt = mimeTypeDetector.getStandardExtensionForMimeType(mimeType);

      input = temporaryFilesManager.createTempfileCopy(input, newExt);
    }

    generateThumbnail(input, output, params);
  }

  protected abstract String getStandardZipExtension();

  protected abstract String getStandardOfficeExtension();

  protected abstract String getStandardOpenOfficeExtension();

  @Override
  public void setImageSize(int imageResizeOptions) {
    super.setImageSize(imageResizeOptions);
    ooo_thumbnailer.setImageSize(imageResizeOptions);
  }
}
