package edu.ucla.tft.hilos.galerie;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import edu.ucla.tft.hilos.Network;
import edu.ucla.tft.hilos.Resource;

public class ResourceFloatingImage extends FloatingImage {
	public final int index;
	public final Resource r;

	public ResourceFloatingImage(int index, Resource r) {
		this.index = index;
		this.r = r;
		if (!Resource.isImageType(r.resourceType)) {
			throw new RuntimeException("Resource must be of image type");
		}
	}

	private static Matrix yFlipMatrix;

	static {
		yFlipMatrix = new Matrix();
		yFlipMatrix.postScale(1, -1); // flip Y axis
	}

	@Override
	protected Bitmap getBitmap(Context context) {
		Bitmap bitmap = BitmapFactory.decodeStream(Network
				.getResourceInputStream(r.id, r.resourceType, false));
		Bitmap flipped = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), yFlipMatrix, false);
		bitmap.recycle();
		return flipped;
	}
}
