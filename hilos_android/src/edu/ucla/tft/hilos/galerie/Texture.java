package edu.ucla.tft.hilos.galerie;

import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.opengl.GLUtils;

public class Texture {
	public final int id;
	public final int width, height;
	public final int texWidth, texHeight;

	public Texture(int id, int width, int height, int texWidth, int texHeight) {
		this.id = id;
		this.width = width;
		this.height = height;
		this.texWidth = texWidth;
		this.texHeight = texHeight;
	}

	public static Texture create(GL10 gl, Bitmap bitmap) {
		IntBuffer textures = IntBuffer.allocate(1);
		gl.glGenTextures(1, textures);
		int id = textures.get(0);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, id);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
				GL10.GL_LINEAR);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
				GL10.GL_LINEAR);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
				GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
				GL10.GL_CLAMP_TO_EDGE);
		Bitmap texBitmap = npot(bitmap);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, texBitmap, 0);
		Texture texture = new Texture(id, bitmap.getWidth(),
				bitmap.getHeight(), texBitmap.getWidth(), texBitmap.getHeight());
		texBitmap.recycle();
		return texture;
	}

	private static final Bitmap npot(Bitmap bitmap) {
		int texWidth = npot(bitmap.getWidth());
		int texHeight = npot(bitmap.getHeight());
		if (texWidth == bitmap.getWidth() && texHeight == bitmap.getHeight()) {
			return bitmap;
		}
		Bitmap texture = Bitmap.createBitmap(texWidth, texHeight,
				Config.ARGB_8888);
		Canvas canvas = new Canvas(texture);
		canvas.drawBitmap(bitmap, (texture.getWidth() - bitmap.getWidth()) / 2,
				(texture.getHeight() - bitmap.getHeight()) / 2, new Paint());
		Canvas.freeGlCaches();
		return texture;
	}

	private static int npot(int val) {
		int npot = 1;
		while (npot < val) {
			npot *= 2;
		}
		return npot;
	}
}
