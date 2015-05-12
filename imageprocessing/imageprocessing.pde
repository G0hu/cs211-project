
import processing.core.PApplet;
import processing.core.PImage;
HScrollbar thresholdBar;
PImage img;
int intensity;
public void setup() {
  size(1600, 600);
  img = loadImage("board1.jpg");
  thresholdBar = new HScrollbar(800, 580, 800, 20);
  intensity = (int)(thresholdBar.getPos()*255);
  //noLoop(); // no interactive behaviour: draw() will be called only once.
}
public void draw() {
  intensity = (int)(thresholdBar.getPos()*255);

  background(color(0, 0, 0));
  PImage result = createImage(img.width, img.height, RGB); // create a new, initially transparent, 'result' image
  for (int i = 0; i < img.width * img.height; i++) {
    // do something with the pixel img.pixels[i]
    if (brightness(img.pixels[i])>intensity) {
      result.pixels[i]= color(255, 255, 255);
    } else {
      result.pixels[i]=0;
    }
  }
  image(img, 0, 0);
  image(result, 800, 0);
  thresholdBar.display();
  thresholdBar.update();
  println(intensity); // getPos() returns a value between 0 and 1
}

