package edu.ucla.tft.hilos;

/**
 * Interface implemented by classes which have a <i>thumbnail</i> view.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public interface ThumbnailProvider {
	public Resource getThumbnail();

	/**
	 * A {@link Resource} which has a corresponding thumbnail image.
	 * 
	 * @author Jasleen Singh (jasleen@cs.ucla.edu)
	 * 
	 */
	public static class Resource_wThumbnail extends Resource implements
			ThumbnailProvider {
		private Resource thumbnail;

		public Resource_wThumbnail(Resource copy, Resource thumbnail) {
			super(copy);
			this.thumbnail = thumbnail;
		}

		@Override
		public Resource getThumbnail() {
			return thumbnail;
		}
	}
}
