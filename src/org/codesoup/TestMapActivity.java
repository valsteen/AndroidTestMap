package org.codesoup;

//import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class TestMapActivity extends MapActivity {
 
	private MapView mapView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mapView = (MapView)findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        Drawable marker = getResources().getDrawable(R.drawable.defaultmarker);
        marker.setBounds((int)-marker.getIntrinsicWidth()/2, (int)-marker.getIntrinsicHeight()/2,
        	 (int)marker.getIntrinsicWidth()/2, (int)marker.getIntrinsicHeight()/2);
        TestOverlay overlay = new TestOverlay(new GeoPoint(0,0), marker);
        mapView.getOverlays().add(overlay);
    }

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}

class TestOverlay extends ItemizedOverlay<OverlayItem> {
	private OverlayItem foreveralone ; 
	
	public void setCenter(GeoPoint p) {
		foreveralone = new OverlayItem(p, "My point", "Is great");
		populate();
	}
	
	public TestOverlay(GeoPoint center, Drawable defaultMarker) {
		super(defaultMarker);
		setCenter(center);
	}

	/*@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		//mapView.getController().setCenter(p);
		//mapView.getController().zoomIn();
		return super.onTap(p, mapView);
	}*/
	@Override
	public void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
	};
	@Override
	protected OverlayItem createItem(int i) {
		return foreveralone;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return foreveralone == null ? 0 : 1;
	}	
}
