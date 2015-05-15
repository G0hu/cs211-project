
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
  result = convolute(img);

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
      float sum_h, sum_v;
      sum_h =0;
      sum_v =0;
      int sum=0;

      for (int k=- 1; k< 2; k++) {
        for (int l=- 1; l<2; l++) {
          sum_h+=(img.get(x+l, y+k)*hKernel[k+1][l+1]);
          sum_v+=(img.get(x+l, y+k)*vKernel[k+1][l+1]);
        }
      }
      sum = (int)sqrt(pow(sum_h, 2) + pow(sum_v, 2));
      if (sum>max) {
        max=sum;
      }
      buffer[y*img.width+x] =sum;
    }
  }
  // *************************************

  for (int y = 2; y < img.height - 2; y++) { // Skip top and bottom edges
    for (int x = 2; x < img.width - 2; x++) { // Skip left and right
      if (buffer[y * img.width + x] > (int)(max * 0.3f)) { // 30% of the max
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
      1, 1, 1
    }
    , 
    {
      1, 1, 1
    }
    , 
    {
      1, 1, 1
    }
  };
  float weight = 1.f;
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
          sum+= (target.get(j+k, i+l)*kernel[k+1][l+1]);
        }
      }
      // - sum all these intensities and divide it by the weight
      sum= sum/weight;

      // - set result.pixels[y * img.width + x] to this value
      conv.pixels[i*conv.width + j]= (int)sum;
    }
  }
  return conv;
}
public void hough(PImage edgeImg) {
  
  float discretizationStepsPhi = 0.06f;
  float discretizationStepsR = 2.5f;
  
  // dimensions of the accumulator
  int phiDim = (int) (Math.PI / discretizationStepsPhi);
  int rDim = (int) (((edgeImg.width + edgeImg.height) * 2 + 1) / discretizationStepsR);
  
  // our accumulator (with a 1 pix margin around)
  int[] accumulator = new int[(phiDim + 2) * (rDim + 2)];
  
  
  // Fill the accumulator: on edge points (ie, white pixels of the edge
  // image), store all possible (r, phi) pairs describing lines going
  // through the point.
  for (int y = 0; y < edgeImg.height; y++) {
    for (int x = 0; x < edgeImg.width; x++) {
      
      // Are we on an edge?
      if (brightness(edgeImg.pixels[y * edgeImg.width + x]) != 0) {
        
        // ...determine here all the lines (r, phi) passing through
        // pixel (x,y), convert (r,phi) to coordinates in the
        // accumulator, and increment accordingly the accumulator.
        
        
      }
    }
  }
}

