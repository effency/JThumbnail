package io.github.makbn.thumbnailer.model;

import java.io.File;

/**
 * created by Mehdi Akbarian-Rastaghi 2018-10-23
 */
public class ThumbnailCandidate {

  private File file;
  private String hash;
  private String thumbExt;
  private ResizeParameters params;

  public ThumbnailCandidate(File file, String hash, String thumbExt, ResizeParameters params) {
    this.file = file;
    this.hash = hash;
    this.thumbExt = thumbExt;
    this.params = params;
  }

  public ThumbnailCandidate(File file, String hash, ResizeParameters params) {
    this.file = file;
    this.hash = hash;
    this.params = params;
  }

  public File getFile() {
    return file;
  }

  public void setFile(File file) {
    this.file = file;
  }

  public String getThumbExt() {
    return thumbExt;
  }

  public void setThumbExt(String thumbExt) {
    this.thumbExt = thumbExt;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  public ResizeParameters getParams() {
    return params;
  }

  public void setParams(ResizeParameters params) {
    this.params = params;
  }
}
