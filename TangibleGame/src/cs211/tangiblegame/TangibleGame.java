package cs211.tangiblegame;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.MouseEvent;

@SuppressWarnings("serial")
public class TangibleGame extends PApplet {

	Mover mover;
	Cylinder cylinder;
	PGraphics bottomBar;
	ImageProcessing imageProcessing;
	
	float vidScale = 0.4f;

	float rotX = 0;
	float rotZ = 0;
	float tmpRotX = 0;
	float tmpRotZ = 0;
	float initX, initY;
	float sensitivity = 100;
	
	float score = 0;
	float lastScore = 0;
	float scoreSquareSize = 4.0f;
	int keepLastScores;
	
	BoundedQueue<Float> scores;
	
	int chartWidth;
	HScrollbar scoreScrollbar;
	
	PFont f;

	static final int BOX_WIDTH = 200;
	static final int BOX_DEPTH = 200;
	static final int BOX_HEIGHT = 10;
	
	static final int BOTTOM_HEIGHT = 192;
	static final int VIDEO_WIDTH = 256;
	static final int TOPVIEW_WIDTH = 192;
	static final int SCORE_WIDTH = 192;

	boolean addCylinderOK = false;
	boolean paused = false;

	ArrayList<PVector> cylinders = new ArrayList<PVector>();

	public void setup() {
		size(1200, 800, P3D);
		bottomBar = createGraphics(width-VIDEO_WIDTH, BOTTOM_HEIGHT, P2D);
		chartWidth = ((int)bottomBar.width - TOPVIEW_WIDTH - SCORE_WIDTH);
		keepLastScores = (int) (chartWidth / scoreSquareSize);
		scores = new BoundedQueue<Float>(keepLastScores);
		scoreScrollbar = new HScrollbar(this, TOPVIEW_WIDTH + SCORE_WIDTH, height - 10, chartWidth, 10);

		noStroke();
		f = createFont("Arial", 45, true);
		cylinder = new Cylinder(this);
		mover = new Mover(this, new GameCollisionListener());

		imageProcessing = new ImageProcessing(this);
		imageProcessing.init();
	}

	void drawBase() {
		pushMatrix();
		textFont(f, 36);
		fill(133, 116, 86);
		background(237, 229, 194);
		image(bottomBar, 0, height - BOTTOM_HEIGHT);
		lights();
		translate(width / 2, height / 2, 0);
		popMatrix();
		drawBottomBar();
	}

	void drawBottomBar() {
		bottomBar.beginDraw();
		bottomBar.background(152, 217, 210);
		drawTop(bottomBar);
		bottomBar.translate(TOPVIEW_WIDTH, 0);
		drawScore(bottomBar);
		bottomBar.translate(SCORE_WIDTH, 0);
		drawChart(bottomBar);
		bottomBar.endDraw();
		
		pushStyle();
		scoreScrollbar.display();
		popStyle();
	}

	void drawTop(PGraphics ctx) {
		float k = (float)TOPVIEW_WIDTH / (float)BOX_WIDTH;
		
		ctx.fill(50, 62, 70);
		ctx.rect(0, 0, TOPVIEW_WIDTH, TOPVIEW_WIDTH);
		
		float x = (mover.location.x + BOX_WIDTH / 2) * k;
		float y = (mover.location.z + BOX_DEPTH / 2) * k;
		float r = mover.sphereDiam * k * 2;
		
		ctx.fill(237, 229, 194);
		ctx.noStroke();
		ctx.ellipse(x, y, r, r);
		
		ctx.fill(200, 200, 200);
		
		r = cylinder.cylinderBaseSize * k * 2;
		
		for (PVector c : cylinders) {
			x = c.x + BOX_WIDTH/2;
			y = c.z + BOX_DEPTH/2;
			ctx.ellipse(x, y, r, r);
		}
	}
	
	void drawChart(PGraphics ctx) {
		float maxScore = 0.0f;
		for (float s : scores) {
			maxScore = Math.max(s,  maxScore);
		}
		
		scoreScrollbar.update();
		
		scoreSquareSize = ceil(scoreScrollbar.getPos() * (16 - 4));
		keepLastScores = (int) (chartWidth / scoreSquareSize);
		scores.setLimit(keepLastScores);
		
		for (int i = 0; i < scores.size(); i += 1) {
			float s = scores.get(i);
			float normalized = s / maxScore;
			float squaresNum = normalized * TOPVIEW_WIDTH / scoreSquareSize;
			
			ctx.fill(50, 62, 70);
			ctx.noStroke();
			
			for (int j = 0; j < squaresNum; j += 1) {
				ctx.rect(i*scoreSquareSize, BOTTOM_HEIGHT-12-j*scoreSquareSize, scoreSquareSize, scoreSquareSize);
			}
		}
	}
	
	void drawScore(PGraphics ctx) {
		ctx.textFont(f, 20);
		ctx.fill(50, 62, 70);
		double speed_ =  Math.round(1000 / sensitivity * 100.0) / 100.0;
		double velocity_ = Math.round(mover.velocity.mag() * 100.0) / 100.0;
		double score_ = Math.round(score * 100.0) / 100.0;
		double lastScore_ = Math.round(lastScore * 100.0) / 100.0;
		
		ctx.text("Sensitivity: " + speed_, 10, 30);
		ctx.text("Velocity: " + velocity_, 10, 60);
		ctx.text("Score: " + score_, 10, 90);
		ctx.text("Last score: " + lastScore_, 10, 120);
	}
	
	class GameCollisionListener implements CollisionListener {
		public void onCylinderCollision(float v) {
			lastScore = v;
			score += lastScore;
			scores.add(score);
		}
		public void onEdgeCollision(float v) {
			lastScore = -v;
			score += lastScore;
			if (score < 0.0f) {
				score = 0.0f;
			}
			scores.add(score);
		}
	}

	public void pause() {
		paused = true;
	}

	public void unPause() {
		paused = false;
	}

	public void draw() {
		drawBase();
		pushMatrix();
		translate(width / 2, height / 2, 0);
		rotateX(rotX);
		rotateZ(rotZ);
		box(BOX_WIDTH, BOX_HEIGHT, BOX_DEPTH);
		for (int i = 0; i < cylinders.size(); i++) {
			PVector vec = cylinders.get(i);
			pushMatrix();
			translate(vec.x, 0, vec.z);
			shape(cylinder.cylinder);
			popMatrix();
		}
		pushMatrix();
		translate(0, -15, 0);
		if (!paused) {
			if (imageProcessing != null && imageProcessing.rotations != null) {
				rotX = imageProcessing.rotations.x + 2 * rotX;
				rotZ = imageProcessing.rotations.y + 2 * rotZ;
				rotX *= 0.4;
				rotZ *= 0.4;
			}
			mover.update(rotX, rotZ, cylinders);
			mover.checkEdges();
		}
		mover.display();
		popMatrix();
		popMatrix();

		if (imageProcessing != null) {
			pushStyle();
			pushMatrix();
			translate(width - 640 * vidScale, height - 480 * vidScale);
			scale(vidScale);
			imageProcessing.draw(vidScale);
			popMatrix();
			popStyle();
		}
	}

	public void mouseDragged() {
		if (!paused && !scoreScrollbar.locked) {
			float nextRotX = rotX + (initY - mouseY) / sensitivity;
			if (nextRotX > -PI / 3 && nextRotX < PI / 3) {
				rotX = nextRotX;
			}
			float nextRotZ = rotZ + (mouseX - initX) / sensitivity;
			if (nextRotZ > -PI / 3 && nextRotZ < PI / 3) {
				rotZ = nextRotZ;
			}
			initY = mouseY;
			initX = mouseX;
		}
	}

	public void mousePressed() {
		// println(mouseX + "  " + mouseY);
		if (addCylinderOK) {
			addCylinder();
		} else {
			initX = mouseX;
			initY = mouseY;
		}
	}

	public void mouseWheel(MouseEvent event) {
		if (event.getCount() > 0 && sensitivity < 1000) {
			sensitivity = (float) (sensitivity * 1.1f);
		} else if (event.getCount() < 0 && sensitivity > 10) {
			sensitivity = (float) (sensitivity * 0.9f);
		}
	}

	public void keyPressed() {
		if (key == CODED) {
			if (keyCode == SHIFT) {
				addCylinderOK = true;
				tmpRotX = rotX;
				tmpRotZ = rotZ;
				rotX = -PI / 2;
				rotZ = 0;
				pause();
			}
		}
	}

	public void keyReleased() {
		if (key == CODED) {
			if (keyCode == SHIFT) {
				addCylinderOK = false;
				rotX = tmpRotX;
				rotZ = tmpRotZ;
				unPause();
			}
		}
	}

	public void addCylinder() {
		if (checkBounds(mouseX - (width - BOX_WIDTH)/2, mouseY - (height - BOX_DEPTH)/2)) {
			cylinders.add(new PVector(-(width / 2 - mouseX), 0,
									  -(height / 2 - mouseY)));
		}
	}

	public boolean checkBounds(float x, float y) {
		if ((x > (BOX_WIDTH - 10)) || x < 10) {
			return false;
		} else {
			return (y <= (BOX_DEPTH - 10)) && (y >= 10);
		}
	}
}
