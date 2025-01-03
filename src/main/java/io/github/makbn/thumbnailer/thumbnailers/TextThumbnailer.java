package io.github.makbn.thumbnailer.thumbnailers;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.makbn.thumbnailer.ThumbnailerException;
import io.github.makbn.thumbnailer.model.ResizeParameters;

public class TextThumbnailer extends AbstractThumbnailer {
  private static Charset charset = Charset.forName("UTF-8");
  private static Logger mLog = LogManager.getLogger("TextThumbnailer");

  @Override
  public void generateThumbnail(File input, File output, ResizeParameters params)
      throws IOException, ThumbnailerException {

    String text = readFile(input);
    BufferedImage img = new BufferedImage(params.getWidth(), params.getHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();

    Font font = new Font("Arial", Font.PLAIN, 11);
    g2d.setFont(font);
    FontMetrics fm = g2d.getFontMetrics();

    g2d.dispose();

    img = new BufferedImage(params.getWidth(), params.getHeight(), BufferedImage.TYPE_INT_ARGB);
    g2d = img.createGraphics();
    g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
    g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
    g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    g2d.setFont(font);
    fm = g2d.getFontMetrics();
    g2d.setColor(Color.BLACK);

    int textW = g2d.getFontMetrics().stringWidth(text);

    int lineCount = Math.max(1, textW / params.getWidth());

    int cc = text.length() / lineCount;

    int index = 0;
    ArrayList<String> lines = new ArrayList<>();

    while (index < text.length()) {
      String sub = text.substring(index, Math.min(index + cc, text.length()));
      lines.add(sub);
      index += cc;
    }

    int y = fm.getAscent();
    for (String line : lines)
      g2d.drawString(line, 0, y += g2d.getFontMetrics().getHeight());

    try {
      ImageIO.write(img, "png", output);
    } catch (IOException e) {
      throw new ThumbnailerException(e);
    }
  }

  private String readFile(File input) throws IOException {
    StringBuilder text = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF8"))) {
      String line;
      int linecount = 0;
      while ((line = br.readLine()) != null && linecount++ < 5) {
        text.append(line.replace("\n", ""));
      }
    }

    return charset.decode(charset.encode(text.toString())).toString();
  }

  @Override
  public String[] getAcceptedMIMETypes() {
    return new String[] { "text/plain", "text/rtf", };
  }
}
