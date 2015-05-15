
import processing.core.PApplet;
import processing.core.PImage;
HScrollbar thresholdBarMax;
HScrollbar thresholdBarMin;
PImage img, result;
int max, min;
public void setup() {
  size(1600, 600);
  img = loadImage("board1.jpg");
  thresholdBarMin = new HScrollbar(800, 580, 800, 20);
  thresholdBarMax = new HScrollbar(800, 550, 800, 20);

  max = (int)(thresholdBarMax.getPos()*255);
  min = (int)(thresholdBarMin.getPos()*255);

  //noLoop(); // no interactive behaviour: draw() will be called only once.
}
public void draw() {
  max = (int)(thresholdBarMax.getPos()*255);
  min = (int)(thresholdBarMin.getPos()*255);

  background(color(0, 0, 0));
  result = sobel(img);

  image(img, 0, 0);
  image(result, 800, 0);
  thresholdBarMax.display();
  thresholdBarMin.display();

  thresholdBarMax.update();
  thresholdBarMin.update();
}

public PImage sobel(PImage img) {
  float[][] hKernel = { 
    { 
      0, 1, 0
    }
    , 
    { 
      0, 0, 0
    }
    , 
    { 
      0, -1, 0
    }
  };
  float[][] vKernel = { 
    { 
      0, 0, 0
    }
    , 
    { 
      1, 0, -1
    }
    , 
    { 
      0, 0, 0
    }
  };
  PImage result = createImage(img.width, img.height, ALPHA);
  // clear the image
  for (int i = 0; i < img.width * img.height; i++) {
    result.pixels[i] = color(0);
  }
  float max=0;
  float[] buffer = new float[img.width * img.height];
  // *************************************
  // Implement here the double convolution
  for (int y = 2; y < img.height - 2; y++) { // Skip top and bottom edges
    for (int x = 2; x < img.width - 2; x++) { // Skip left and right
      float sum_h, sum_v, sum;
      sum_h =0;
      sum_v =0;
      sum=0;

      for (int k=- (hKernel.length/2); k< (hKernel.length/2); k++) {
        for (int l=- (hKernel[0].length/2); l<(hKernel[0].length/2); l++) {
          sum_h+=(brightness(img.pixels[((y+k)*img.width)+x+l])*hKernel[k+1][l+1]);
          sum_v+=(brightness(img.pixels[((y+k)*img.width)+x+l])*vKernel[k+1][l+1]);
        }
      }
      sum = sqrt(pow(sum_h, 2) + pow(sum_v, 2));
      if (sum>max) {
        max=sum;
      }
      buffer[y*img.width+x] =sum;
    }
  }
  // *************************************
  
  for (int y = 2; y < img.height - 2; y++) { // Skip top and bottom edges
    for (int x = 2; x < img.width - 2; x++) { // Skip left and right
      if (buffer[y * img.width + x] > (int)(max * 0.4f)) { // 30% of the max
        result.pixels[y * img.width + x] = color(255);
      } else {
        result.pixels[y * img.width + x] = color(0);
      }
    }
  }
  return result;
}

public PImage convolute(PImage target) {
  float[][] kernel = { 
    {
      9, 12, 9
    }
    , 
    {
      12, 15, 12
    }
    , 
    {
      9, 12, 9
    }
  };
  float weight = 225.f;
  // create a greyscale image (type: ALPHA) for output
  PImage conv = createImage(target.width, target.height, ALPHA);
  // kernel size N = 3
  for (int i=1; i< target.height-1; i++) {
    for (int j = 1; j< target.width-1; j++) {
      // - multiply intensities for pixels in the range
      // (x - N/2, y - N/2) to (x + N/2, y + N/2) by the
      // corresponding weights in the kernel matrix
      float sum = 0;
      for (int k=-1; k<2; k++) {
        for (int l=-1; l<2; l++) {
          sum+= (brightness(target.pixels[(target.width*(i+k))+j+l])*kernel[k+1][l+1]);
        }
      }
      // - sum all these intensities and divide it by the weight
      sum= sum/weight;

      // - set result.pixels[y * img.width + x] to this value
      conv.pixels[i*conv.width + j]= color((int)sum);
    }
  }
  return conv;
}

public PImage threshold(PImage img) {
  PImage result = createImage(img.width, img.height, RGB); // create a new, initially transparent, 'result' image
  for (int i = 0; i < img.width * img.height; i++) {
    // do something with the pixel img.pixels[i]
    if (hue(img.pixels[i])>min && hue(img.pixels[i])<max) {
      result.pixels[i]= img.pixels[i];
    } else {
      result.pixels[i]=0;
    }
  } 
  return result;
}

