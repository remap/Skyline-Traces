package edu.ucla.tft.hilos.cloudscape;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Arrays;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import edu.ucla.tft.hilos.Animatable;
import edu.ucla.tft.hilos.AnimationThread;
import edu.ucla.tft.hilos.BaseActivity;
import edu.ucla.tft.hilos.Hilos;
import edu.ucla.tft.hilos.R;
import edu.ucla.tft.hilos.Resource;
import edu.ucla.tft.hilos.ResourceFilter;
import edu.ucla.tft.hilos.Utils;
import edu.ucla.tft.hilos.Keywords.Keyword;
import edu.ucla.tft.hilos.lightbox.LightBoxActivity;
import edu.ucla.tft.hilos.lightbox.LightBoxItemProvider.ResourceLightBoxItemProvider;

/**
 * A transparent overlay which displays the list of keywords related to the
 * currently selected media items. The list is displayed either as:
 * <ul>
 * <li>A tag cloud, with keywords rotating on the surface of a 3D sphere, or</li>
 * <li>A list which can be used to select a particular keyword. Upon selection,
 * information about that keyword appears in a browser window. This view also
 * allows access to all images tagged with the selected keyword.</li>
 * </ul>
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class Cloudscape extends FrameLayout {
	private static class KeywordView implements Comparable<KeywordView> {
		public static interface OnKeywordSelectionListener {
			public void onKeywordSelected(KeywordView keyword);
		}

		public final String text;
		public final String displayText;
		public final Matrix location;

		private static KeywordView selectedKeywordView;
		private OnKeywordSelectionListener listener;
		private float importance;

		public KeywordView(String text, OnKeywordSelectionListener listener) {
			this.text = text;
			if (text.length() < 15) {
				displayText = text;
			} else {
				displayText = text.substring(0, 12) + "...";
			}
			this.listener = listener;
			double x = Math.random() - 0.5;
			double y = Math.random() - 0.5;
			double z = Math.random() - 0.5;
			double length = Math.sqrt(x * x + y * y + z * z);
			x /= length;
			y /= length;
			z /= length;
			location = new Matrix(new double[][] { { x }, { y }, { z }, { 1 } });
			importance = 1.0f;
		}

		public static KeywordView getSelectedKeywordView() {
			return selectedKeywordView;
		}

		public boolean isSelected() {
			return selectedKeywordView == this;
		}

		public void setSelected(boolean selected) {
			if (!selected && isSelected()) {
				selectedKeywordView = null;
			} else if (selected && !isSelected()) {
				if (selectedKeywordView != null) {
					selectedKeywordView.setSelected(false);
				}
				selectedKeywordView = this;
				if (listener != null) {
					listener.onKeywordSelected(this);
				}
			}
		}

		public float getImportance() {
			return importance;
		}

		public void setImportance(float importance) {
			this.importance = Math.min(1, Math.max(0, importance));
		}

		@Override
		public int compareTo(KeywordView another) {
			return (int) (Math.signum(z - another.z));
		}

		private Point proj = new Point();
		private double z;

		public void project(Matrix cam, int radius, int cx, int cy) {
			Matrix m = cam.times(location);
			proj.set((int) (m.get(0, 0) * radius) + cx,
					(int) (m.get(1, 0) * radius) + cy);
			z = m.get(2, 0);
		}

		public Point getProjectedPoint() {
			return proj;
		}

		public double z() {
			return z;
		}

		private Rect bounds = new Rect();

		public void updateBounds(Rect rect) {
			bounds.set(rect);
		}

		public Rect getBounds() {
			return bounds;
		}
	}

	private KeywordView.OnKeywordSelectionListener mKeywordViewListener = new KeywordView.OnKeywordSelectionListener() {
		@Override
		public void onKeywordSelected(KeywordView keyword) {
			mSelectedKeywordText = keyword.text;
			mInfoTitle.setText(mSelectedKeywordText);
			mInfo.setVisibility(VISIBLE);
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						Utils.constructURL(
								Hilos.getHostname() + "/getKeywordInfo",
								"lang", Hilos.getLanguage(), "keyword",
								mSelectedKeywordText).openStream()));
				StringWriter sw = new StringWriter();
				Utils.readFully(br, sw);
				br.close();
				String resp = sw.toString().trim();
				if (resp.startsWith("http://")) {
					mInfoWebView.loadUrl(resp);
				} else {
					mInfoWebView.loadData(resp, "text/html", "utf-8");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	public static final int FREE = 0, TRANSITIONING = 1, HELD = 2;
	private static final int BG_OPACITY_ON_HELD = 176;

	private static final float sFreeTextSize = 18.0f, sHeldTextSize = 22.0f;
	private Paint bgPaint;
	private Paint textPaint;
	private Paint textSelectedPaint;
	private Drawable backgroundTextDrawable;
	private Drawable backgroundTextSelectedDrawable;

	private int state;
	private KeywordView[] keywordsArray;
	private int keywordsCount;
	private Matrix cam;
	private double rotx, roty, rotz;
	private AnimationThread animationThread;
	private Rect textBounds = new Rect();

	private String mSelectedKeywordText;
	private View mInfo;
	private TextView mInfoTitle;
	private Button mInfoViewImages;
	private WebView mInfoWebView;

	public Cloudscape(Context context, AttributeSet attrs) {
		super(context, attrs);
		setBackgroundColor(Color.TRANSPARENT);
		rotateTo(0, 0, 0);
		keywordsArray = new KeywordView[1];
		Resources res = getResources();
		bgPaint = new Paint();
		bgPaint.setColor(Color.BLACK);
		textPaint = new Paint();
		textPaint.setAntiAlias(true);
		textPaint.setColor(Color.WHITE);
		textPaint.setShadowLayer(2, 2, 2, Color.BLACK);
		textPaint.setTextAlign(Align.LEFT);
		textPaint.setTextSize(sFreeTextSize);
		textSelectedPaint = new Paint();
		textSelectedPaint.setAntiAlias(true);
		textSelectedPaint.setColor(Color.BLACK);
		textSelectedPaint.setTextSize(sHeldTextSize);
		textSelectedPaint.setTextAlign(Align.LEFT);
		backgroundTextDrawable = res.getDrawable(R.drawable.bg_highlight_text);
		backgroundTextSelectedDrawable = res
				.getDrawable(R.drawable.bg_highlight_text_selected);
		animationThread = new AnimationThread();
		animationThread.register(new Animatable() {
			@Override
			public void update(long delta_ms) {
				if (keywordsCount <= 0) {
					return;
				}
				animationUpdate(delta_ms);
			}

			@Override
			public void init() {
			}
		});
	}

	@Override
	protected void onFinishInflate() {
		mInfo = findViewById(R.id.cloudscape_info);
		mInfoTitle = (TextView) findViewById(R.id.cloudscape_info_title);
		mInfoViewImages = (Button) findViewById(R.id.cloudscape_info_viewimage);
		mInfoViewImages.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mSelectedKeywordText == null) {
					return;
				}
				viewImagesWithKeyword(mSelectedKeywordText);
			}
		});
		mInfoWebView = (WebView) findViewById(R.id.cloudscape_info_webview);
		mInfoWebView.getSettings().setJavaScriptEnabled(true);
	}

	private void viewImagesWithKeyword(final String keywordText) {
		ResourceLightBoxItemProvider provider = new ResourceLightBoxItemProvider();
		provider.setFilter(new ResourceFilter() {
			@Override
			public boolean accept(Resource resource) {
				if (resource.keywords != null) {
					for (Keyword keyword : resource.keywords.toArray()) {
						if (keywordText.equals(keyword.text)) {
							return true;
						}
					}
				}
				return false;
			}
		});
		LightBoxActivity.UseProvider = provider;
		Context ctx = getContext();
		Intent intent = new Intent(ctx, LightBoxActivity.class);
		ctx.startActivity(intent);
	}

	public int getState() {
		return state;
	}

	public void start() {
		animationThread.start();
	}

	public void stop() {
		animationThread.stop();
	}

	private float t;
	private int direction;

	private static final Rect FreeRect = new Rect(30, 30, 200, 200);

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int mx = (int) event.getX();
		int my = (int) event.getY();
		switch (state) {
		case FREE:
			if (FreeRect.contains(mx, my)) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
					((BaseActivity) getContext())
							.playEarcon(BaseActivity.EC_POP);
					t = 0;
					direction = 1;
					state = TRANSITIONING;
				}
				return true;
			}
			break;
		case TRANSITIONING:
			return true;
		case HELD:
			switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
				KeywordView selected = null;
				for (int i = 0; i < keywordsCount; i++) {
					if (keywordsArray[i].getBounds().contains(mx, my)) {
						selected = keywordsArray[i];
						break;
					}
				}
				if (selected != null) {
					((BaseActivity) getContext())
							.playEarcon(BaseActivity.EC_TOCK);
					selected.setSelected(true);
					postInvalidate();
				} else {
					((BaseActivity) getContext())
							.playEarcon(BaseActivity.EC_POP);
					dismiss();
				}
			}
			return true;
		}
		return false;
	}

	public void dismiss() {
		KeywordView selected;
		if ((selected = KeywordView.getSelectedKeywordView()) != null) {
			selected.setSelected(false);
		}
		t = 1;
		direction = -1;
		state = TRANSITIONING;
		mInfo.setVisibility(INVISIBLE);
	}

	private void animationUpdate(long delta_ms) {
		switch (state) {
		case FREE: {
			double d = delta_ms * Math.toRadians(10) / 1000;
			rotateBy(0, d, d);
			postInvalidate();
			break;
		}
		case TRANSITIONING: {
			t += direction * delta_ms * 2.0f / 1000;
			if (t >= 1) {
				t = 1;
				state = HELD;
			} else if (t <= 0) {
				t = 0;
				state = FREE;
			}
			postInvalidate();
		}
			break;
		}
	}

	public void addKeyword(String keyword) {
		keywordsArray[keywordsCount++] = new KeywordView(keyword,
				mKeywordViewListener);
		if (keywordsCount == keywordsArray.length) {
			KeywordView[] newKeywordsArray = new KeywordView[keywordsArray.length * 2];
			System.arraycopy(keywordsArray, 0, newKeywordsArray, 0,
					keywordsCount);
			keywordsArray = newKeywordsArray;
		}
	}

	public void addKeywords(String[] keywords) {
		for (String keyword : keywords) {
			addKeyword(keyword);
		}
	}

	public void clear() {
		keywordsCount = 0;
	}

	public void rotateBy(double drotx, double droty, double drotz) {
		rotateTo(rotx + drotx, roty + droty, rotz + drotz);
	}

	public void rotateTo(double rotx, double roty, double rotz) {
		this.rotx = rotx;
		this.roty = roty;
		this.rotz = rotz;
		cam = Matrix.identity(4, 4);
		cam = Matrix.RotX(rotx).times(cam);
		cam = Matrix.RotY(roty).times(cam);
		cam = Matrix.RotZ(rotz).times(cam);
		cam = Matrix.Trans(0, 0, -2).times(cam);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		switch (state) {
		case FREE:
			drawFree(canvas);
			break;
		case TRANSITIONING:
			drawTransitioning(canvas);
			break;
		case HELD:
			drawHeld(canvas);
			break;
		}
	}

	private void drawFree(Canvas canvas) {
		for (int i = 0; i < keywordsCount; i++) {
			keywordsArray[i].project(cam,
					Math.min(FreeRect.width(), FreeRect.height()) / 2,
					FreeRect.left + FreeRect.width() / 2, FreeRect.top
							+ FreeRect.height() / 2);
		}
		Arrays.sort(keywordsArray, 0, keywordsCount);
		for (int i = 0; i < keywordsCount; i++) {
			KeywordView keyword = keywordsArray[i];
			// textPaint.setTextSize((float) ((keyword.z + 4) * 12));
			Point p = keyword.getProjectedPoint();
			drawKeyword(keyword, canvas, p.x, p.y);
		}
	}

	private void drawTransitioning(Canvas canvas) {
		int width = getWidth();
		int height = getHeight();
		int left = 60;
		int top = 90;
		int dy = keywordsCount <= 1 ? 0 : (height - top * 2)
				/ (keywordsCount - 1);
		float t2 = t * t;
		bgPaint.setAlpha((int) (t2 * BG_OPACITY_ON_HELD));
		canvas.drawRect(0, 0, width, height, bgPaint);
		for (int i = 0; i < keywordsCount; i++) {
			KeywordView keyword = keywordsArray[i];
			int ex = left;
			int ey = top + dy * i;
			float esize = direction > 0 ? sHeldTextSize : sFreeTextSize;
			float ssize = direction > 0 ? sFreeTextSize : sHeldTextSize;
			textPaint.setTextSize(ssize + (esize - ssize) * t2);
			Point p = keyword.getProjectedPoint();
			drawKeyword(keyword, canvas, (int) (p.x + (ex - p.x) * t2),
					(int) (p.y + (ey - p.y) * t2));
		}
	}

	private void drawHeld(Canvas canvas) {
		int width = getWidth();
		int height = getHeight();
		int left = 60;
		int top = 90;
		int dy = keywordsCount <= 1 ? 0 : (height - top * 2)
				/ (keywordsCount - 1);
		bgPaint.setAlpha(BG_OPACITY_ON_HELD);
		canvas.drawRect(0, 0, width, height, bgPaint);
		for (int i = 0; i < keywordsCount; i++) {
			KeywordView keyword = keywordsArray[i];
			int ex = left;
			int ey = top + dy * i;
			float esize = sHeldTextSize;
			textPaint.setTextSize(esize);
			drawKeyword(keyword, canvas, ex, ey);
		}
	}

	private void drawKeyword(KeywordView keyword, Canvas canvas, int x, int y) {
		String text = mInfo.getVisibility() != View.VISIBLE ? keyword.text
				: keyword.displayText;
		textPaint.getTextBounds(text, 0, text.length(), textBounds);
		textBounds.offsetTo(x, y - textBounds.height());
		textBounds.left -= 15;
		textBounds.top -= 15;
		textBounds.right += 15;
		textBounds.bottom += 15;
		keyword.updateBounds(textBounds);
		if (state == HELD) {
			// Drawable d = keyword.isSelected() ?
			// backgroundTextSelectedDrawable
			// : backgroundTextDrawable;
			if (keyword.isSelected()) {
				Drawable d = backgroundTextSelectedDrawable;
				d.setBounds(textBounds);
				d.draw(canvas);
			}
		}
		canvas.drawText(text, x, y, keyword.isSelected() ? textSelectedPaint
				: textPaint);
	}
}
