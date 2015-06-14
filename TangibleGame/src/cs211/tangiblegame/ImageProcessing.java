package cs211.tangiblegame;

import processing.core.*;
import processing.video.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

public class ImageProcessing {

	String useInput = "video";

	PImage img, result, stillImg;
	Capture cam;
	Movie vid;
	int min, max;
	
	TwoDThreeD twoDThreeD;

	int maxLines = 4;

	PVector rotations;

	PApplet p;
	
	public ImageProcessing(PApplet parent) {
		p = parent;
	}

	public void init() {
		if (useInput == "still") {
			stillImg = p.loadImage("board1.jpg");
		}
		else if (useInput == "camera") {
			setupCamera();
		}
		else {
			vid = new Movie(p, "testvideo.mp4");
			vid.loop();
		}
		
		twoDThreeD = new TwoDThreeD(640, 480);
	}

	public void setupCamera() {
		// Setup of the camera
		String[] cameras = Capture.list();
		if (cameras.length == 0) {
			// PApplet.println("There are no cameras available for capture.");
			p.exit();
		} else {
			// PApplet.println("Available cameras:");
			for (int i = 0; i < cameras.length; i++) {
				// PApplet.println(cameras[i]);
			}
			cam = new Capture(p, cameras[0]);
			cam.start();
		}
	}

	public PImage getImage() {
		// getting the camera stream into img to then process it
		if (useInput == "still") {
			// PApplet.println("using still");
			return stillImg;
		} else if (useInput == "camera") {
			if (cam.available()) {
				cam.read();
				return cam.get();
			}
			p.exit();
			return null;
		} else {
			if (vid.available()) {
				vid.read();
				return vid.get();
			}
			p.exit();
			return null;
		}
	}

	public void draw(float scale) {
		// initialization of the list of lines created by hough
		ArrayList<PVector> lines = new ArrayList<PVector>();


		img = getImage();
		if (img == null) {
			p.exit();
		}

		// p.background(p.color(0, 0, 0));

		// pipeline (the image(result,0,0) can be moved to test each part
		result = hueThreshold(img, 80, 140);
		result = brightnessThreshold(result, 40);
		result = saturationThreshold(result, 100);
		result = blurring(result);
		result = intensityThreshold(result, 170);
		result = sobel(result);

		p.image(img, 0, 0);

		hough(result, lines);
		// image(houghImg, 800, 0);

		ArrayList<PVector[]> quads = computeQuads(lines);
		
		if (quads.size() > 0) {
			PVector[] quad = quads.get(0);
			rotations = twoDThreeD.get3DRotations(Arrays.asList(quad));
			
			drawQuad(quad);

			// Show intersections as given by the first quad
			/*PApplet.println("Showing corners using quad: " + quad[0] + ", " + quad[1]
					+ ", " + quad[2] + ", " + quad[3]);*/
			
			drawIntersections(Arrays.asList(quad));
			// otherwise use the previous methods that computes all
			// intersections
		} else {
			// PApplet.println("Showing all intersections.");
			drawIntersections(getIntersections(lines));
		}

		// image(result, 800 + 600, 0);

		if (useInput == "still") {
			p.noLoop();
		}
	}
	
	public void drawQuad(PVector[] quad) {
		Random random = new Random();
		p.fill(p.color(PApplet.min(255, random.nextInt(300)),
				PApplet.min(255, random.nextInt(300)),
				PApplet.min(255, random.nextInt(300)), 50));

		p.quad(quad[0].x, quad[0].y, quad[1].x, quad[1].y, quad[2].x,
				quad[2].y, quad[3].x, quad[3].y);
	}
	
	public void drawIntersections(Collection<PVector> intersections) {
		for (PVector intersection : intersections) {
			p.fill(255, 128, 0);
			p.ellipse(intersection.x, intersection.y, 10, 10);
		}
	}

	public ArrayList<PVector[]> computeQuads(ArrayList<PVector> lines) {
		QuadGraph graph = new QuadGraph();
		graph.build(lines, 800, 600);

		ArrayList<int[]> quads = (ArrayList<int[]>) graph.findCycles();
		ArrayList<PVector[]> validQuads = new ArrayList<PVector[]>();

		for (int[] quad : quads) {
			PVector l1 = lines.get(quad[0]);
			PVector l2 = lines.get(quad[1]);
			PVector l3 = lines.get(quad[2]);
			PVector l4 = lines.get(quad[3]);

			PVector c12 = intersection(l1, l2);
			PVector c23 = intersection(l2, l3);
			PVector c34 = intersection(l3, l4);
			PVector c41 = intersection(l4, l1);

			if (QuadGraph.isConvex(c12, c23, c34, c41)
					&& QuadGraph.validArea(c12, c23, c34, c41, 1000000, 50000)
					&& QuadGraph.nonFlatQuad(c12, c23, c34, c41)) {
				PVector[] validQuad = { c12, c23, c34, c41 };
				validQuads.add(validQuad);
			}
		}

		return validQuads;
	}

	// algorithm that keep only pixels with a specific hue (color)
	public PImage hueThreshold(PImage img, int hueMin, int hueMax) {
		PImage result = p.createImage(img.width, img.height, PApplet.RGB); // create a
																	// new,
																	// initially
																	// transparent,
																	// 'result'
																	// image
		for (int i = 0; i < img.width * img.height; i++) {
			if (p.hue(img.pixels[i]) > hueMin && p.hue(img.pixels[i]) < hueMax) {
				result.pixels[i] = img.pixels[i];
			} else {
				result.pixels[i] = 0;
			}
		}
		return result;
	}

	// algorithm that keep only pixels with a specific brightness (color)
	public PImage brightnessThreshold(PImage img, int brightness) {
		PImage result = p.createImage(img.width, img.height, PApplet.RGB); // create a
																	// new,
																	// initially
																	// transparent,
																	// 'result'
																	// image
		for (int i = 0; i < img.width * img.height; i++) {
			if (p.brightness(img.pixels[i]) > brightness) {
				result.pixels[i] = img.pixels[i];
			} else {
				result.pixels[i] = 0;
			}
		}
		return result;
	}

	// algorithm that keep only pixels with a saturation hue (color)
	public PImage saturationThreshold(PImage img, int saturation) {
		PImage result = p.createImage(img.width, img.height, PApplet.RGB); // create a
																	// new,
																	// initially
																	// transparent,
																	// 'result'
																	// image
		for (int i = 0; i < img.width * img.height; i++) {
			if (p.saturation(img.pixels[i]) > saturation) {
				result.pixels[i] = p.color(255, 255, 255);
			} else {
				result.pixels[i] = 0;
			}
		}
		return result;
	}

	public PImage intensityThreshold(PImage img, float minIntensity) {
		PImage result = p.createImage(img.width, img.height, PApplet.ALPHA);
		for (int i = 0; i < img.width * img.height; i++) {
			float intensity = (float) (0.2989 * p.red(img.pixels[i]) + 0.5870
					* p.green(img.pixels[i]) + 0.1140 * p.blue(img.pixels[i]));
			if (intensity > minIntensity) {
				result.pixels[i] = p.color(255, 255, 255); // img.pixels[i]; //
															// color(255, 255,
															// 255);
			} else {
				result.pixels[i] = 0;
			}
		}
		return result;
	}

	// algorithm that blurs the image
	public PImage blurring(PImage target) {
		float[][] kernel = { { 9, 12, 9 }, { 12, 15, 12 }, { 9, 12, 9 } };

		return applyKernel(target, kernel);
	}

	public PImage applyKernel(PImage target, float[][] kernel) {
		PImage result = p.createImage(target.width, target.height, PApplet.ALPHA);
		int n = kernel.length / 2;
		for (int i = n; i < target.height - n; i++) {
			for (int j = n; j < target.width - n; j++) {
				// - multiply intensities for pixels in the range
				// (x - N/2, y - N/2) to (x + N/2, y + N/2) by the
				// corresponding weights in the kernel matrix
				float sumR = 0;
				float sumG = 0;
				float sumB = 0;
				float weight = 0.0f;
				for (int k = -n; k <= n; k++) {
					for (int l = -n; l <= n; l++) {
						sumR += (p.red(target.get(j + k, i + l)) * kernel[k + n][l
								+ n]);
						sumG += (p.green(target.get(j + k, i + l)) * kernel[k + n][l
								+ n]);
						sumB += (p.blue(target.get(j + k, i + l)) * kernel[k + n][l
								+ n]);
						weight += kernel[k + n][l + n];
					}
				}
				// - sum all these intensities and divide it by the weight
				sumR = sumR / weight;
				sumG = sumG / weight;
				sumB = sumB / weight;

				// - set result.pixels[y * img.width + x] to this value
				result.pixels[i * result.width + j] = p.color(sumR, sumG, sumB);
			}
		}
		return result;
	}

	// sobel algorithm to detect edges
	public PImage sobel(PImage img) {
		float[][] hKernel = { { 0, 1, 0 }, { 0, 0, 0 }, { 0, -1, 0 } };
		float[][] vKernel = { { 0, 0, 0 }, { 1, 0, -1 }, { 0, 0, 0 } };

		// initialization of the resulting image
		PImage result = p.createImage(img.width, img.height, PApplet.ALPHA);
		// clear the image
		for (int i = 0; i < img.width * img.height; i++) {
			result.pixels[i] = p.color(0);
		}
		float max = 0;
		float[] buffer = new float[img.width * img.height];

		// application of the two kernels on each pixel
		for (int y = 2; y < img.height - 2; y++) { // Skip top and bottom edges
			for (int x = 2; x < img.width - 2; x++) { // Skip left and right
				float sum_h, sum_v;
				sum_h = 0;
				sum_v = 0;
				int sum = 0;
				for (int k = -1; k < 2; k++) {
					for (int l = -1; l < 2; l++) {
						sum_h += (img.get(x + l, y + k) * hKernel[k + 1][l + 1]);
						sum_v += (img.get(x + l, y + k) * vKernel[k + 1][l + 1]);
					}
				}
				// composition of the two results
				sum = (int) PApplet.sqrt(PApplet.pow(sum_h, 2) + PApplet.pow(sum_v, 2));
				if (sum > max) {
					max = sum;
				}
				buffer[y * img.width + x] = sum;
			}
		}

		// filling of the resulting image using a percentage of the maximal
		// value
		for (int y = 2; y < img.height - 2; y++) { // Skip top and bottom edges
			for (int x = 2; x < img.width - 2; x++) { // Skip left and right
				if (buffer[y * img.width + x] > (int) (max * 0.3f)) { // 30% of
																		// the
																		// max
					result.pixels[y * img.width + x] = p.color(255);
				} else {
					result.pixels[y * img.width + x] = p.color(0);
				}
			}
		}
		return result;
	}

	// hough algorithm that compute all the possible lines to draw and select
	// only the best ones (currently 5)
	// returns a list of lines as PVector
	public PImage hough(PImage edgeImg, ArrayList<PVector> lines) {
		float discretizationStepsPhi = 0.04f;
		float discretizationStepsR = 2.5f;

		// dimensions of the accumulator
		int phiDim = (int) (Math.PI / discretizationStepsPhi);
		int rDim = (int) (((edgeImg.width + edgeImg.height) * 2 + 1) / discretizationStepsR);

		// pre-compute the sin and cos values
		float[] tabSin = new float[phiDim];
		float[] tabCos = new float[phiDim];
		float ang = 0;
		float inverseR = 1.f / discretizationStepsR;
		for (int accPhi = 0; accPhi < phiDim; ang += discretizationStepsPhi, accPhi++) {
			// we can also pre-multiply by (1/discretizationStepsR) since we
			// need it in the Hough loop
			tabSin[accPhi] = (float) (Math.sin(ang) * inverseR);
			tabCos[accPhi] = (float) (Math.cos(ang) * inverseR);
		}

		// our accumulator (with a 1 pix margin around)
		int[] accumulator = new int[(phiDim + 2) * (rDim + 2)];
		ArrayList<Integer> bestCandidates = new ArrayList<Integer>();

		// Fill the accumulator: on edge points (ie, white pixels of the edge
		// image), store all possible (r, phi) pairs describing lines going
		// through the point.
		for (int y = 0; y < edgeImg.height; y++) {
			for (int x = 0; x < edgeImg.width; x++) {

				// Are we on an edge?
				if (p.brightness(edgeImg.pixels[y * edgeImg.width + x]) != 0) {
					float phi = 0;
					for (int i = 0; i < phiDim; i++) {
						int phiIndex = Math.round(phi / discretizationStepsPhi);
						double r = x * tabCos[phiIndex] + y * tabSin[phiIndex];
						int rIndex = Math.round((float) (r) + (rDim - 1) / 2);

						accumulator[(phiIndex + 1) * (rDim + 2) + rIndex + 1] += 1;
						phi += discretizationStepsPhi;
					}
				}
			}
		}

		// size of the region we search for a local maximum
		int neighbourhood = 10;
		// only search around lines with more that this amount of votes
		// (to be adapted to your image)
		int minVotes = 200;
		for (int accR = 0; accR < rDim; accR++) {
			for (int accPhi = 0; accPhi < phiDim; accPhi++) {
				// compute current index in the accumulator
				int idx = (accPhi + 1) * (rDim + 2) + accR + 1;
				if (accumulator[idx] > minVotes) {
					boolean bestCandidate = true;
					// iterate over the neighbourhood
					for (int dPhi = -neighbourhood / 2; dPhi < neighbourhood / 2 + 1; dPhi++) {
						// check we are not outside the image
						if (accPhi + dPhi < 0 || accPhi + dPhi >= phiDim)
							continue;
						for (int dR = -neighbourhood / 2; dR < neighbourhood / 2 + 1; dR++) {
							// check we are not outside the image
							if (accR + dR < 0 || accR + dR >= rDim)
								continue;
							int neighbourIdx = (accPhi + dPhi + 1) * (rDim + 2)
									+ accR + dR + 1;
							if (accumulator[idx] < accumulator[neighbourIdx]) {
								// the current idx is not a local maximum!
								bestCandidate = false;
								break;
							}
						}
						if (!bestCandidate)
							break;
					}
					if (bestCandidate) {
						// the current idx *is* a local maximum
						bestCandidates.add(idx);
					}
				}
			}
		}

		Collections.sort(bestCandidates, new HoughComparator(accumulator));
		bestCandidates = new ArrayList<Integer>(bestCandidates.subList(0,
				Math.min(bestCandidates.size(), maxLines)));

		// Display of the lines
		for (int idx = 0; idx < accumulator.length; idx++) {
			if (bestCandidates.contains(idx)) {

				// first, compute back the (r, phi) polar coordinates:
				int accPhi = (int) (idx / (rDim + 2)) - 1;
				int accR = idx - (accPhi + 1) * (rDim + 2) - 1;
				float r = (accR - (rDim - 1) * 0.5f) * discretizationStepsR;
				float phi = accPhi * discretizationStepsPhi;

				lines.add(new PVector(r, phi));

				// Cartesian equation of a line: y = ax + b
				// in polar, y = (-cos(phi)/sin(phi))x + (r/sin(phi))
				// => y = 0 : x = r / cos(phi)
				// => x = 0 : y = r / sin(phi)
				// compute the intersection of this line with the 4 borders of
				// the image
				int x0 = 0;
				int y0 = (int) (r / PApplet.sin(phi));
				int x1 = (int) (r / PApplet.cos(phi));
				int y1 = 0;
				int x2 = edgeImg.width;
				int y2 = (int) (-PApplet.cos(phi) / PApplet.sin(phi) * x2 + r / PApplet.sin(phi));
				int y3 = edgeImg.width;
				int x3 = (int) (-(y3 - r / PApplet.sin(phi)) * (PApplet.sin(phi) / PApplet.cos(phi)));
				// Finally, plot the lines
				drawHoughLine(x0, x1, x2, x3, y0, y1, y2, y3);
			}
		}

		// code to display the accumulator
		PImage houghImg = p.createImage(rDim + 2, phiDim + 2, PApplet.ALPHA);
		for (int i = 0; i < accumulator.length; i++) {
			houghImg.pixels[i] = p.color(PApplet.min(255, accumulator[i]));
		}
		houghImg.updatePixels();
		houghImg.resize(600, 600);

		return houghImg;
	}
	
	public void drawHoughLine(int x0, int x1, int x2, int x3, int y0, int y1, int y2, int y3) {
		p.stroke(204, 102, 0);
		if (y0 > 0) {
			if (x1 > 0)
				p.line(x0, y0, x1, y1);
			else if (y2 > 0)
				p.line(x0, y0, x2, y2);
			else
				p.line(x0, y0, x3, y3);
		} else {
			if (x1 > 0) {
				if (y2 > 0)
					p.line(x1, y1, x2, y2);
				else
					p.line(x1, y1, x3, y3);
			} else
				p.line(x2, y2, x3, y3);
		}
	}

	// algorithm that compute the intersections of a list of lines and return a
	// list of intersections as PVector
	public ArrayList<PVector> getIntersections(ArrayList<PVector> lines) {
		ArrayList<PVector> intersections = new ArrayList<PVector>();
		for (int i = 0; i < lines.size() - 1; i++) {
			PVector line1 = lines.get(i);
			for (int j = i + 1; j < lines.size(); j++) {
				PVector line2 = lines.get(j);
				intersections.add(intersection(line1, line2));
			}
		}
		return intersections;
	}

	public PVector intersection(PVector line1, PVector line2) {
		double d = Math.cos(line2.y) * Math.sin(line1.y) - Math.cos(line1.y)
				* Math.sin(line2.y);
		double x = (line2.x * Math.sin(line1.y) - line1.x * Math.sin(line2.y))
				/ d;
		double y = (-line2.x * Math.cos(line1.y) + line1.x * Math.cos(line2.y))
				/ d;

		return new PVector((float) x, (float) y);
	}


}
