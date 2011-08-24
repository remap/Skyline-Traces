package edu.ucla.tft.hilos;

/**
 * An interface for a filter which determines which resources should be
 * returned.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public interface ResourceFilter {
	public boolean accept(Resource resource);

	/**
	 * A <i>null</i> {@link ResourceFilter} which accepts all resources.
	 */
	public static final ResourceFilter NULL = new ResourceFilter() {
		@Override
		public boolean accept(Resource resource) {
			return true;
		}
	};
}