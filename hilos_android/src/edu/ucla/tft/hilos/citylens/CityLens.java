package edu.ucla.tft.hilos.citylens;

import min3d.core.Object3dContainer;
import min3d.core.RendererActivity;
import min3d.objectPrimitives.Sphere;
import min3d.vos.Light;
import min3d.vos.Number3d;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import edu.ucla.tft.hilos.AbstractToolbar;
import edu.ucla.tft.hilos.Network;
import edu.ucla.tft.hilos.R;
import edu.ucla.tft.hilos.Scene.Model;
import edu.ucla.tft.hilos.Utils;

/**
 * An activity to visualize and interact with 3D models.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 *
 */
public class CityLens extends RendererActivity implements
		AbstractToolbar.Listener {
	private static final int WAIT_DIALOG = 0;
	private static final int MASK_ZOOMIN = 0x00000001;
	private static final int MASK_ZOOMOUT = 0x00000002;
	private static final int MASK_ROTATELEFT = 0x00000004;
	private static final int MASK_ROTATERIGHT = 0x00000008;
	private int downMask;

	private ProgressDialog waitDialog;
	private Object3dContainer objModel;
	private Sphere earth;
	private CityLensZoomToolbar mZoomToolbar;
	private CityLensRotateToolbar mRotateToolbar;

	private FrameLayout container;

	@Override
	public void onButtonAction(AbstractToolbar toolbar, int button, int action) {
		if (state != EDIT) {
			return;
		}
		if (toolbar instanceof CityLensZoomToolbar) {
			switch (button) {
			case CityLensZoomToolbar.HOME:
				home();
				break;
			case CityLensZoomToolbar.ZOOMIN:
				if (action == AbstractToolbar.Listener.ACTION_DOWN) {
					downMask |= MASK_ZOOMIN;
				} else if (action == AbstractToolbar.Listener.ACTION_UP) {
					downMask &= ~MASK_ZOOMIN;
				}
				break;
			case CityLensZoomToolbar.ZOOMOUT:
				if (action == AbstractToolbar.Listener.ACTION_DOWN) {
					downMask |= MASK_ZOOMOUT;
				} else if (action == AbstractToolbar.Listener.ACTION_UP) {
					downMask &= ~MASK_ZOOMOUT;
				}
				break;
			}
		} else if (toolbar instanceof CityLensRotateToolbar) {
			switch (button) {
			case CityLensRotateToolbar.ROTATE_LEFT:
				if (action == AbstractToolbar.Listener.ACTION_DOWN) {
					downMask |= MASK_ROTATELEFT;
				} else if (action == AbstractToolbar.Listener.ACTION_UP) {
					downMask &= ~MASK_ROTATELEFT;
				}
				break;
			case CityLensRotateToolbar.ROTATE_RIGHT:
				if (action == AbstractToolbar.Listener.ACTION_DOWN) {
					downMask |= MASK_ROTATERIGHT;
				} else if (action == AbstractToolbar.Listener.ACTION_UP) {
					downMask &= ~MASK_ROTATERIGHT;
				}
				break;
			}
		}
	}

	@Override
	protected void onCreateSetContentView() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		container = (FrameLayout) LayoutInflater.from(this).inflate(
				R.layout.empty, null);
		setContentView(container);
		showDialog(WAIT_DIALOG);
		new Thread() {
			public void run() {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				final View view = LayoutInflater.from(CityLens.this).inflate(
						R.layout.citylens, null);
				LinearLayout ll = (LinearLayout) view
						.findViewById(R.id.glSurfaceHolder);
				ll.addView(_glSurfaceView);
				mZoomToolbar = (CityLensZoomToolbar) view
						.findViewById(R.id.zoom_toolbar);
				mZoomToolbar.setListener(CityLens.this);
				mRotateToolbar = (CityLensRotateToolbar) view
						.findViewById(R.id.rotate_toolbar);
				mRotateToolbar.setListener(CityLens.this);
				container.post(new Runnable() {
					@Override
					public void run() {
						container.addView(view);
						dismissDialog(WAIT_DIALOG);
					}
				});
			}
		}.start();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case WAIT_DIALOG:
			waitDialog = new ProgressDialog(this);
			waitDialog.setCancelable(false);
			waitDialog.setMessage("Initialization in progress...");
			waitDialog.setTitle("Please Wait");
			waitDialog.setIndeterminate(true);
			waitDialog.show();
			return waitDialog;
		default:
			return null;
		}
	}

	private static final int WORLD = 0, MODEL = 1, EDIT = 2;
	private int state;

	@Override
	public void onInitScene() {
		startRadius = 100;
		endRadius = 20;
		startAt = new Number3d(0, 0, startRadius);
		endAt = new Number3d(0, 0, endRadius);
		endAt.rotateX((float) Math.toRadians(-45));
		endAt.rotateY((float) Math.toRadians(-30));
		endAt.normalize();
		endAt.multiply(endRadius);

		Light light;
		light = new Light();
		light.position.setAll(30, 30, 30);
		light.diffuse.setAll(0xff, 0xff, 0xff, 0xff);
		// light.diffuse.setAll(0xff, 0, 0, 0xff);
		scene.lights().add(light);
		light = new Light();
		light.position.setAll(-30, 30, 30);
		light.diffuse.setAll(0xff, 0xff, 0xff, 0xff);
		// light.diffuse.setAll(0, 0xff, 0, 0xff);
		scene.lights().add(light);
		light = new Light();
		light.position.setAll(-30, 30, -30);
		light.diffuse.setAll(0xff, 0xff, 0xff, 0xff);
		// light.diffuse.setAll(0, 0, 0xff, 0xff);
		scene.lights().add(light);

		try {
			loadScene();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// earth = new Sphere(endRadius, 40, 40, true, true, false);
		//
		// Bitmap b = Utils.makeBitmapFromResourceId(this, R.drawable.earth);
		// Shared.textureManager().addTextureId(b, "earth", false);
		// b.recycle();
		//
		// earth.textures().addById("earth");
		// scene.addChild(earth);

		scene.camera().position = new Number3d(0, 0, startRadius);
		scene.camera().target = new Number3d(0, 0, 0);
		state = EDIT;
	}

	private void loadScene() throws Exception {
		Model[] models = Network.getModelList(false);
		for (Model model : models) {
			CityLens.this.scene
					.addChild(Utils.loadModel(getResources(), model));
		}
	}

	private static final Number3d HOME = new Number3d(-10, 10, 0);
	private float startRadius;
	private float endRadius;
	private Number3d startAt;
	private Number3d endAt;
	private float t;

	private void rotateLeft() {
		endAt.rotateY((float) (Math.PI / 300));
	}

	private void rotateRight() {
		endAt.rotateY(-(float) (Math.PI / 300));
	}

	private void zoomIn() {
		Number3d dir = new Number3d();
		dir.setAllFrom(endAt);
		dir.normalize();
		dir.multiply(.1f);
		endAt.subtract(dir);
	}

	private void zoomOut() {
		Number3d dir = new Number3d();
		dir.setAllFrom(endAt);
		dir.normalize();
		dir.multiply(.1f);
		endAt.add(dir);
	}

	private void home() {
		endAt.setAllFrom(HOME);
	}

	@Override
	public void onUpdateScene() {
		switch (state) {
		case WORLD:
			t += 1 / 360.0f;
			if (t < 1) {
				scene.camera().position.setAll(startAt.x + t
						* (endAt.x - startAt.x), startAt.y + t
						* (endAt.y - startAt.y), startAt.z + t
						* (endAt.z - startAt.z));
			} else {
				t = 0;
				startAt.setAllFrom(endAt);
				endAt.setAllFrom(HOME);
				state = MODEL;
				// scene.camera().position.setAll(0, 0, 10);
			}
			break;
		case MODEL:
			t += 3 / 360.0f;
			if (t < 1) {
				scene.camera().position.setAll(startAt.x + t
						* (endAt.x - startAt.x), startAt.y + t
						* (endAt.y - startAt.y), startAt.z + t
						* (endAt.z - startAt.z));
			} else {
				t = 0;
				state = EDIT;
			}
			break;
		case EDIT:
			if ((downMask & MASK_ZOOMIN) != 0) {
				zoomIn();
			} else if ((downMask & MASK_ZOOMOUT) != 0) {
				zoomOut();
			} else if ((downMask & MASK_ROTATELEFT) != 0) {
				rotateLeft();
			} else if ((downMask & MASK_ROTATERIGHT) != 0) {
				rotateRight();
			}
			// endAt.rotateY(2 / 360.0f);
			scene.camera().position.setAllFrom(endAt);
			break;
		}
	}
}
