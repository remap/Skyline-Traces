package edu.ucla.tft.hilos.galerie;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import edu.ucla.tft.hilos.Utils;

public class GalerieRenderer implements Renderer {
	private Context context;
	private float transx, transy, transz;
	private float rotx, roty, rotz;

	public GalerieRenderer(Context context) {
		this.context = context;
	}

	public void currLocation(float[] trans) {
		trans[0] = transx;
		trans[1] = transy;
		trans[2] = transz;
	}

	public void moveTo(float transx, float transy, float transz) {
		this.transx = transx;
		this.transy = transy;
		this.transz = transz;
	}

	public void move(float amount) {
		transx += Math.sin(Math.toRadians(roty)) * amount;
		transz += Math.cos(Math.toRadians(roty)) * amount;
	}

	public void currOrientation(float[] rot) {
		rot[0] = rotx;
		rot[1] = roty;
		rot[2] = rotz;
	}

	public void turnTo(float rotx, float roty, float rotz) {
		this.rotx = rotx;
		this.roty = roty;
		this.rotz = rotz;
	}

	public void turn(float angleDeg) {
		roty += angleDeg;
	}

	private static float[] quadVertexCoords = new float[] { -1, 1, 0, -1, -1,
			0, 1, 1, 0, 1, -1, 0 };

	private static float[] quadTextureCoords = new float[] { 0, 1, 0, 0, 1, 1,
			1, 0 };

	private static FloatBuffer quadVertexBuffer;
	private static FloatBuffer quadTextureBuffer;

	private FloatingImage[] images;

	static {
		ByteBuffer vbb = ByteBuffer
				.allocateDirect(quadVertexCoords.length * 3 * 4);
		vbb.order(ByteOrder.nativeOrder());
		quadVertexBuffer = vbb.asFloatBuffer();
		quadVertexBuffer.put(quadVertexCoords);
		quadVertexBuffer.position(0);
		ByteBuffer tbb = ByteBuffer
				.allocateDirect(quadTextureCoords.length * 3 * 4);
		tbb.order(ByteOrder.nativeOrder());
		quadTextureBuffer = tbb.asFloatBuffer();
		quadTextureBuffer.put(quadTextureCoords);
		quadTextureBuffer.position(0);
	}

	public FloatingImage[] getImages() {
		return images;
	}

	public void setImages(FloatingImage[] images) {
		this.images = images;
	}

	// private void initializeImage(GL10 gl, ResourceFloatingImage img) {
	// // create textures
	// // set the texture
	// int[] resIDs = new int[] { R.drawable.photo01, R.drawable.photo02,
	// R.drawable.photo03, R.drawable.photo04, R.drawable.photo05,
	// R.drawable.photo06, R.drawable.photo08 };
	// images = new FloatingImage[resIDs.length];
	// for (int i = 0; i < resIDs.length; i++) {
	// images[i] = new FloatingImage(0, 0, -2 - i / 2.0f, 0, 0, 0, Texture
	// .create(gl,
	// getTextureFromBitmapResource(context, resIDs[i])));
	// }
	// }

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glClearColor(0, 0, 0, 0);
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glEnable(GL10.GL_BLEND);
	}

	public void onDrawFrame(GL10 gl) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		if (images != null) {
			//gl.glRotatef(-rotx, 1.0f, 0.0f, 0.0f);
			gl.glRotatef(-roty, 0.0f, 1.0f, 0.0f);
			//gl.glRotatef(-rotz, 0.0f, 0.0f, 1.0f);
			gl.glTranslatef(-transx, -transy, -transz);
			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, quadVertexBuffer);
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, quadTextureBuffer);
			for (int i = 0; i < images.length; i++) {
				if (!images[i].isTextureLoaded()) {
					images[i].loadTexture(gl, context);
				}
				gl.glPushMatrix();
				if (!images[i].positionAndBind(gl)) {
					continue;
				}
				gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
				gl.glPopMatrix();
			}
			gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		}
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// avoid division by zero
		if (height == 0)
			height = 1;
		// draw on the entire screen
		gl.glViewport(0, 0, width, height);
		// setup projection matrix
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 1.0f,
				100.0f);
	}

	private static Matrix xyFlipMatrix;

	static {
		xyFlipMatrix = new Matrix();
		xyFlipMatrix.postScale(-1, -1); // flip Y axis
	}

	public static Bitmap getTextureFromBitmapResource(Context context,
			int resourceId) {
		Bitmap bitmap = null;
		Bitmap texture = null;
		try {
			bitmap = BitmapFactory.decodeResource(context.getResources(),
					resourceId);
			texture = Utils.createThumb(bitmap, 512, 10);
			return Bitmap.createBitmap(texture, 0, 0, texture.getWidth(),
					texture.getHeight(), xyFlipMatrix, false);
		} finally {
			if (bitmap != null) {
				bitmap.recycle();
			}
			if (texture != null) {
				texture.recycle();
			}
		}
	}
}
