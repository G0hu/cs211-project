
import processing.core.PApplet;
import processing.core.PImage;

PImage img;
public void setup() {
  size(800, 600);
  img = loadImage("board1.jpg");
  noLoop(); // no interactive behaviour: draw() will be called only once.
}
public void draw() {
  image(img, 0, 0);
}

