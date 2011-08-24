package edu.ucla.tft.hilos.maplet;

import org.osmdroid.ResourceProxy.string;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;

/**
 * A {@link TileSource} for custom tiles used on the map. A remote web service
 * returns appropriate tile image depending on the current theme index,
 * specified in the base url.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class HilosXYTileSource extends OnlineTileSourceBase {
	public HilosXYTileSource(final String aName, final string aResourceId,
			final int aZoomMinLevel, final int aZoomMaxLevel,
			final int aTileSizePixels, final String aImageFilenameEnding,
			final String... aBaseUrl) {
		super(aName, aResourceId, aZoomMinLevel, aZoomMaxLevel,
				aTileSizePixels, aImageFilenameEnding, aBaseUrl);
	}

	@Override
	public String getTileURLString(final MapTile aTile) {
		String tileURLString = String.format(getBaseUrl()
				+ aTile.getZoomLevel() + "/" + aTile.getX() + "/"
				+ aTile.getY() + mImageFilenameEnding);
		return tileURLString;
	}
}
