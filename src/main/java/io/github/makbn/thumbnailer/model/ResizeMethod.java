package io.github.makbn.thumbnailer.model;

public enum ResizeMethod {
  /**
   * Scale input image so that width and height is equal (or smaller) to the output size. The other dimension will be
   * smaller or equal than the output size.
   */
  BothDimensions,
  /**
   * Scale input image so that width or height is equal to the output size. The other dimension will be bigger or equal
   * than the output size.
   */
  OneDimension;
}
