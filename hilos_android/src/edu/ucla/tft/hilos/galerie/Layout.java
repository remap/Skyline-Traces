package edu.ucla.tft.hilos.galerie;

public interface Layout {
	public String name();

	public void layout(ResourceFloatingImage image);

	public static class Linear implements Layout {
		@Override
		public String name() {
			return "Linear";
		}

		@Override
		public void layout(ResourceFloatingImage image) {
			image.setLocation(image.index * 2, 0, -10);
		}
	}

	public static class Radial implements Layout {
		@Override
		public String name() {
			return "Radial";
		}

		@Override
		public void layout(ResourceFloatingImage image) {
			float angle = image.index * 6;
			image.setLocation((float) (20 * Math.cos(angle)), 0,
					(float) (-20 * Math.sin(angle)));
		}
	}

	public static class Geo implements Layout {
		@Override
		public String name() {
			return "Geographic Coordinates";
		}

		private static final double centerLat = 34.067360,
				centerLon = -118.233747;

		@Override
		public void layout(ResourceFloatingImage image) {
			image.setLocation((float) ((image.r.longitude - centerLon) * 1000),
					(float) (Math.random() * 50 - 25),
					-(float) ((image.r.latitude - centerLat) * 1000));
		}
	}
}
