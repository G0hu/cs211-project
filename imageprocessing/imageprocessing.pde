
import processing.core.PApplet;
import processing.core.PImage;
import processing.video.Capture;

HScrollbar thresholdBarMax;
HScrollbar thresholdBarMin;
PImage img, result;
Capture cam;
int max, min;
public void setup() {
  size(800, 600);

  //Setup of the camera
  String[] cameras = Capture.list();
  if (cameras.length == 0) {
    println("There are no cameras available for capture.");
    exit();
  } else {
    println("Available cameras:");
    for (int i = 0; i < cameras.length; i++) {
      println(cameras[i]);
    }
    cam = new Capture(this, cameras[0]);
    cam.start();
  }



  thresholdBarMin = new HScrollbar(0, 580, 800, 20);
  thresholdBarMax = new HScrollbar(0, 550, 800, 20);

  max = (int)(thresholdBarMax.getPos()*255);
  min = (int)(thresholdBarMin.getPos()*255);

  //noLoop(); // no interactive behaviour: draw() will be called only once.
}
public void draw() {
  if (cam.available() == true) {
    cam.read();
  }
  img = cam.get();

  max = (int)(thresholdBarMax.getPos()*255);
  min = (int)(thresholdBarMin.getPos()*255);

  background(color(0, 0, 0));
  //result= threshold(img);
  result = sobel(img);
  image(result, 0, 0);
  result = hough(result);


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
public PImage hough(PImage edgeImg) {

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
        float phi =0;
        for (int i =0; i<phiDim; i++) {
          double r = x*Math.cos(phi)+y*Math.sin(phi);
          int rIndex = Math.round((float)(r/discretizationStepsR)+(rDim-1)/2);
          int phiIndex= Math.round(phi/discretizationStepsPhi);
          accumulator[(phiIndex+1)*(rDim+2)+rIndex+1] +=1;
          phi+= discretizationStepsPhi;
        }
      }
    }
  }
  for (int idx = 0; idx < accumulator.length; idx++) {
    if (accumulator[idx] > 200) {
      // first, compute back the (r, phi) polar coordinates:
      int accPhi = (int) (idx / (rDim + 2)) - 1;
      int accR = idx - (accPhi + 1) * (rDim + 2) - 1;
      float r = (accR - (rDim - 1) * 0.5f) * discretizationStepsR;
      float phi = accPhi * discretizationStepsPhi;
      // Cartesian equation of a line: y = ax + b
      // in polar, y = (-cos(phi)/sin(phi))x + (r/sin(phi))
      // => y = 0 : x = r / cos(phi)
      // => x = 0 : y = r / sin(phi)
      // compute the intersection of this line with the 4 borders of
      // the image
      int x0 = 0;
      int y0 = (int) (r / sin(phi));
      int x1 = (int) (r / cos(phi));
      int y1 = 0;
      int x2 = edgeImg.width;
      int y2 = (int) (-cos(phi) / sin(phi) * x2 + r / sin(phi));
      int y3 = edgeImg.width;
      int x3 = (int) (-(y3 - r / sin(phi)) * (sin(phi) / cos(phi)));
      // Finally, plot the lines
      stroke(204, 102, 0);
      if (y0 > 0) {
        if (x1 > 0)
          line(x0, y0, x1, y1);
        else if (y2 > 0)
          line(x0, y0, x2, y2);
        else
          line(x0, y0, x3, y3);
      } else {
        if (x1 > 0) {
          if (y2 > 0)
            line(x1, y1, x2, y2);
          else
            line(x1, y1, x3, y3);
        } else
          line(x2, y2, x3, y3);
      }
    }
  }

  //code to display the accumulator
  PImage houghImg = createImage(rDim + 2, phiDim + 2, ALPHA);
  for (int i = 0; i < accumulator.length; i++) {
    houghImg.pixels[i] = color(min(255, accumulator[i]));
  }
  houghImg.updatePixels();
  houghImg.resize(300, 300);
  return houghImg;
}

public PImage threshold(PImage img) {
  PImage result = createImage(img.width, img.height, RGB); // create a new, initially transparent, 'result' image
  for (int i = 0; i < img.width * img.height; i++) {
    if (hue(img.pixels[i])>min && hue(img.pixels[i])<max) {
      result.pixels[i]= img.pixels[i];
    } else {
      result.pixels[i]=0;
    }
  } 
  return result;
}
