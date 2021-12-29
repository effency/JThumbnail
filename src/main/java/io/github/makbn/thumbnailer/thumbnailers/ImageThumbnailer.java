package io.github.makbn.thumbnailer.thumbnailers;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.makbn.thumbnailer.ThumbnailerException;
import io.github.makbn.thumbnailer.model.ResizeParameters;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.configurations.Antialiasing;

/**
 * created by Mehdi Akbarian-Rastaghi 2018-10-23
 */
public class ImageThumbnailer extends AbstractThumbnailer {

  private static Logger mLog = LogManager.getLogger("ImageThumbnailer");

  @Override
  public void generateThumbnail(File input, File output, ResizeParameters params) throws ThumbnailerException {
    try {
      Thumbnails.of(input).allowOverwrite(true).antialiasing(Antialiasing.ON)
          .size(params.getWidth(), params.getHeight()).toFile(output);
    } catch (IOException e) {
      mLog.error(e);
      throw new ThumbnailerException();
    }
  }

  @Override
  public String[] getAcceptedMIMETypes() {
    return new String[] { "image/png", "image/jpeg", "image/tiff", "image/bmp", "image/jpg", "image/gif" };
  }
}
