package io.github.makbn.thumbnailer.thumbnailers;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import io.github.makbn.thumbnailer.ThumbnailerException;
import io.github.makbn.thumbnailer.model.ResizeParameters;

/**
 * created by Mehdi Akbarian-Rastaghi 2018-10-23
 */
public class MP3Thumbnailer extends AbstractThumbnailer {

  private static final Logger logger = LoggerFactory.getLogger(MP3Thumbnailer.class);

  @Override
  public void generateThumbnail(File input, File output, ResizeParameters params)
      throws IOException, ThumbnailerException {
    Mp3File song = null;
    try {
      song = new Mp3File(input.getPath());
      if (song.hasId3v2Tag()) {
        ID3v2 id3v2tag = song.getId3v2Tag();
        byte[] imageData = id3v2tag.getAlbumImage();
        // converting the bytes to an image
        BufferedImage img = getScaledBI(ImageIO.read(new ByteArrayInputStream(imageData)), params);
        ImageIO.write(img, "png", output);
      }

    } catch (UnsupportedTagException | InvalidDataException e) {
      logger.warn("MP3Thumbnailer", e);
      throw new ThumbnailerException();
    }

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

  @Override
  public String[] getAcceptedMIMETypes() {
    return new String[] { "audio/mpeg", "audio/mp3", "audio/mp4", "audio/vnd.wav" };
  }
}
