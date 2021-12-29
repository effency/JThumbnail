package io.github.makbn.thumbnailer.model;

import java.util.ResourceBundle;

public class ResizeParameters {
  private static final ResourceBundle rb = ResourceBundle.getBundle("application");

  private int width = Integer.parseInt(rb.getString("thumb_width"));
  private int height = Integer.parseInt(rb.getString("thumb_height"));
  private ResizeMethod resizeMethod = ResizeMethod.OneDimension;

  public ResizeParameters() {
  }

  public ResizeParameters(int width, int height, ResizeMethod resizeMethod) {
    super();
    this.width = width;
    this.height = height;
    this.resizeMethod = resizeMethod;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
    if (resizeMethod == ResizeMethod.OneDimension) {
      this.height = width;
    }
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public ResizeMethod getResizeMethod() {
    return resizeMethod;
  }

  public void setResizeMethod(ResizeMethod resizeMethod) {
    this.resizeMethod = resizeMethod;
  }

}
