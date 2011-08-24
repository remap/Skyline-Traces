package edu.ucla.tft.hilos.galerie;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import edu.ucla.tft.hilos.Utils;

public abstract class FloatingImage {
	protected float x, y, z;
	protected float rotx, roty, rotz;
	protected Texture texture;

	public FloatingImage() {
	}

	public FloatingImage(float x, float y, float z, float rotx, float roty,
			float rotz, Texture texture) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.rotx = rotx;
		this.roty = roty;
		this.rotz = rotz;
		this.texture = texture;
	}

	public void setLocation(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void setOrientation(float rotx, float roty, float rotz) {
		this.rotx = rotx;
		this.roty = roty;
		this.rotx = rotz;
	}

	public boolean isTextureLoaded() {
		return texture != null;
	}

	public void loadTexture(GL10 gl, Context context) {
		Bitmap bitmap = getBitmap(context);
		texture = Texture.create(gl, Utils.createThumb(bitmap, 512, 10));
		bitmap.recycle();
	}

	protected abstract Bitmap getBitmap(Context context);

	public boolean positionAndBind(GL10 gl) {
		if (!isTextureLoaded()) {
			return false;
		}
		gl.glTranslatef(x, y, z);
		gl.glRotatef(rotx, 1, 0, 0);
		gl.glRotatef(roty, 0, 1, 0);
		gl.glRotatef(rotz, 0, 0, 1);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, texture.id);
		return true;
	}
}
