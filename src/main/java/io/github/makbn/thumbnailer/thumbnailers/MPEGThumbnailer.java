package io.github.makbn.thumbnailer.thumbnailers;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.makbn.thumbnailer.ThumbnailerException;
import io.github.makbn.thumbnailer.exception.ThumbnailerRuntimeException;
import io.github.makbn.thumbnailer.model.ResizeParameters;
import io.github.makbn.thumbnailer.util.GifSequenceWriter;

/**
 * created by Mehdi Akbarian-Rastaghi 2018-10-22
 */
public class MPEGThumbnailer extends AbstractThumbnailer {

  private static final Logger logger = LoggerFactory.getLogger(MPEGThumbnailer.class);

  @Override
  public void generateThumbnail(File input, File output, ResizeParameters params) throws ThumbnailerException {
    try {
      getThumb(input.getPath(), output.getPath(), params);
    } catch (Exception e) {
      logger.warn("MPEGThumbnailer", e);
      throw new ThumbnailerException();
    }
  }

  /**
   * get thumbnail from multimedia files
   *
   * @throws IOException
   * @throws InterruptedException
   * @Author Iman Akbari
   */
  public void getThumb(String inputPath, String outputPath, ResizeParameters params)
      throws IOException, InterruptedException {
    FFmpegFrameGrabber g = new FFmpegFrameGrabber(inputPath);
    ImageOutputStream output = new FileImageOutputStream(new File(outputPath));

    g.setFormat("mp4");
    g.start();
    int frame_count = g.getLengthInFrames();

    GifSequenceWriter gifSequenceWriter = null;

    for (int ig = 0; ig < frame_count; ig += g.getLengthInFrames() / 10) {
      if (ig > 0)
        g.setFrameNumber(ig);

      BufferedImage bi = createImageFromBytes(g.grabImage().data.array());

      if (gifSequenceWriter == null)
        gifSequenceWriter = new GifSequenceWriter(output, bi.getType(), 500, true);
      gifSequenceWriter.writeToSequence(getScaledBI(bi, params));
    }

    g.stop();
    gifSequenceWriter.close();
  }

  /**
   * Get a List of accepted File Types. Only PDF Files are accepted.
   *
   * @return MIME-Types
   */
  @Override
  public String[] getAcceptedMIMETypes() {
    return new String[] { "video/mp4", "video/MP2T", "video/x-msvideo", "video/x-ms-wmv", "video/x-m4v", "video/webm",
        "video/quicktime", "video/3gpp"

    };
  }

  @SuppressWarnings("Duplicates")
  private BufferedImage getScaledBI(BufferedImage org, ResizeParameters params) {
    Image tmp = org.getScaledInstance(params.getWidth(), params.getHeight(), Image.SCALE_SMOOTH);
    BufferedImage scaleBI = new BufferedImage(params.getWidth(), params.getHeight(), BufferedImage.TYPE_INT_ARGB);

    Graphics2D g2d = scaleBI.createGraphics();
    g2d.drawImage(tmp, 0, 0, null);
    g2d.dispose();

    return scaleBI;
  }

  private BufferedImage createImageFromBytes(byte[] imageData) {
    ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
    try {
      return ImageIO.read(bais);
    } catch (IOException e) {
      logger.debug(e.getMessage());
    }
    throw new ThumbnailerRuntimeException("Error in generating thumbnail for MPEG file.");
  }
}
