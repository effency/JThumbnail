package io.github.makbn.thumbnailer;

import java.io.File;
import java.io.IOException;

import io.github.makbn.thumbnailer.listener.ThumbnailListener;
import io.github.makbn.thumbnailer.model.ResizeParameters;
import io.github.makbn.thumbnailer.model.ThumbnailCandidate;

/**
 * Created by Mehdi Akbarian-Rastaghi on 9/30/18
 */

public class Test {

  public static void main(String[] args) {

    try {
      AppSettings.init(args);
      Thumbnailer.start();
      File in = new File("/Users/mehdiakbarian-rastaghi/Documents/Projects/Sakku/gharardad.docx");
      if (in.exists()) {
        ResizeParameters params = new ResizeParameters();

        ThumbnailCandidate candidate = new ThumbnailCandidate(in, "unique_code", params);

        Thumbnailer.createThumbnail(candidate, new ThumbnailListener() {
          @Override
          public void onThumbnailReady(String hash, File thumbnail) {
            System.out.println("FILE created at : " + thumbnail.getAbsolutePath());
          }

          @Override
          public void onThumbnailFailed(String hash, String message, int code) {
            System.out.println(message);
          }
        });
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
