package edu.ucla.tft.hilos.galerie;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import edu.ucla.tft.hilos.Animatable;
import edu.ucla.tft.hilos.AnimationThread;

public class GalerieView extends GLSurfaceView implements Animatable {
	private static final int NONE = 0, MOVING = 1, WAITING = 2;
	private GalerieRenderer mRenderer;
	private AnimationThread mAnimThread;

	private int state = NONE;

	public GalerieView(Context context) {
		super(context);
		mAnimThread = new AnimationThread();
		mAnimThread.register(this);
	}

	public void startAnimation() {
		mAnimThread.start();
	}

	@Override
	public void init() {
	}

	private float m, w;
	private float[] startTrans = new float[3];
	private float[] endTrans = new float[3];
	private float[] startRot = new float[3];
	private float[] endRot = new float[3];

	@Override
	public void update(long deltaMs) {
		switch (state) {
		case NONE:
			FloatingImage[] images = mRenderer.getImages();
			FloatingImage next = images[(int) (Math.random() * images.length)];
			m = 0;
			mRenderer.currLocation(startTrans);
			mRenderer.currOrientation(startRot);
			endTrans[0] = next.x;
			endTrans[1] = next.y;
			endTrans[2] = next.z + 3.0f;
			endRot[0] = startRot[0];
			endRot[1] = (int) (Math.random() * 20 - 10);
			endRot[2] = startRot[2];
			state = MOVING;
			break;
		case MOVING:
			m += deltaMs / (.8 * 1000);
			float m2 = m * m;
			if (m2 >= 1) {
				m2 = 1;
				w = 0;
				state = WAITING;
			}
			mRenderer.moveTo(
					startTrans[0] + m2 * (endTrans[0] - startTrans[0]),
					startTrans[1] + m2 * (endTrans[1] - startTrans[1]),
					startTrans[2] + m2 * (endTrans[2] - startTrans[2]));
			mRenderer.turnTo(startRot[0] + m2 * (endRot[0] - startRot[0]),
					startRot[1] + m2 * (endRot[1] - startRot[1]), startRot[2]
							+ m2 * (endRot[2] - startRot[2]));
			break;
		case WAITING:
			w += deltaMs / (3.0 * 1000);
			if (w >= 1) {
				state = NONE;
			}
			break;
		}
		postInvalidate();
	}

	@Override
	public void setRenderer(Renderer renderer) {
		super.setRenderer(renderer);
		mRenderer = (GalerieRenderer) renderer;
	}

	private static final int G_NONE = 0, G_HORIZONTAL = 1, G_VERTICAL = 2;
	private float px, py;
	private float mx, my;
	private float mx_, my_;
	private float dx, dy;
	private int mGesture = G_NONE;
	private static final int RECO_RADIUS = 10;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mx = event.getX();
		my = event.getY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			px = mx;
			py = my;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mGesture == G_NONE) {
				if (Math.abs(dx = mx - px) >= RECO_RADIUS) {
					mGesture = G_HORIZONTAL;
				} else if (Math.abs(dy = my - py) >= RECO_RADIUS) {
					mGesture = G_VERTICAL;
				}
				mx_ = mx;
				my_ = my;
			} else {
				dx = mx - mx_;
				dy = my - my_;
				track();
				mx_ = mx;
				my_ = my;
			}
			break;
		case MotionEvent.ACTION_UP:
			mGesture = G_NONE;
			break;
		}
		return true;
	}

	private void track() {
		switch (mGesture) {
		case G_HORIZONTAL:
			mRenderer.turn(dx);
			break;
		case G_VERTICAL:
			mRenderer.move(dy / 50.0f);
			break;
		}
	}
}
