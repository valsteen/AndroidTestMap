package org.codesoup;

import java.security.InvalidAlgorithmParameterException;
import java.util.List;
import java.util.Vector;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Toast;
import com.google.android.maps.*;
import fi.foyt.foursquare.api.*;
import fi.foyt.foursquare.api.entities.Checkin;
import fi.foyt.foursquare.api.entities.CompactVenue;
import fi.foyt.foursquare.api.entities.CompleteUser;
import fi.foyt.foursquare.api.entities.Location;

public class TestMapActivity extends MapActivity {
 
	private static final String CLIENT_ID = "CLIENT_ID_PLACEHOLDER";
	private static final String CLIENT_SECRET = "CLIENT_SECRET_PLACEHOLDER";
	private static final String CALLBACK_URL = "http://codesoup.org/android/4sq/";	
	
	private MapView mapView;
	private String fsqToken; 
	private FoursquareApi foursquareapi ;
	
	private FoursquareApi getFourSquareApi() {
		if (foursquareapi == null) foursquareapi = new FoursquareApi(CLIENT_ID, CLIENT_SECRET, CALLBACK_URL);
		return foursquareapi;
	}
	
	TestOverlay overlay;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        /* setup overlay on map. Only used for onTap for now */
        
        mapView = (MapView)findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        Drawable marker = getResources().getDrawable(R.drawable.defaultmarker);
        marker.setBounds((int)-marker.getIntrinsicWidth()/2, (int)-marker.getIntrinsicHeight()/2,
        	 (int)marker.getIntrinsicWidth()/2, (int)marker.getIntrinsicHeight()/2);
        
        overlay = new TestOverlay(new GeoPoint(0,0), marker);
        mapView.getOverlays().add(overlay);
    }

    private void test4sq() {
    	FoursquareApi fsq = getFourSquareApi();
    	try {
			Result<Checkin[]> checkins = fsq.checkinsRecent(null, null, null);
			overlay.setCheckins(checkins.getResult());
		} catch (FoursquareApiException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	/*
    	String name;
		try {
			Result<CompleteUser> user = fsq.user(null);
			if (user != null) {
				CompleteUser result = user.getResult();
				if (result != null) {
					name = result.getFirstName();	
				} else {
					name = "result is null";
				}
			} else {
			    name = "user is null";
			}
			
		} catch (FoursquareApiException e) {
			name = "foursquare error";
			e.printStackTrace();
		}
    	Toast.makeText(this, name, Toast.LENGTH_SHORT).show();
    	*/
    }
    
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	class TestOverlay extends ItemizedOverlay<OverlayItem> {
		Vector<GeoPoint> points; 
		
		public TestOverlay(GeoPoint center, Drawable defaultMarker) {
			super(defaultMarker);
			populate();
		}
		
		@Override
		public boolean onTap(GeoPoint p, MapView mapView) {
			//mapView.getController().setCenter(p);
			//mapView.getController().zoomIn();
			
			if (fsqToken == null) {
				Intent intent = new Intent(TestMapActivity.this, ActivityWebView.class);
				intent.putExtra("url", TestMapActivity.this.getFourSquareApi().getAuthenticationUrl());
		        TestMapActivity.this.startActivityForResult(intent, 1);
			} else {
				test4sq();
			}
			return true;
		}
		@Override
		public void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow) {
			super.draw(canvas, mapView, shadow);
		};
		@Override
		protected OverlayItem createItem(int i) {
			return new OverlayItem(points.get(i), "", "");
		}

		@Override
		public int size() {
			if (points == null) return 0;
			return points.size();
		}
		
		public void setCheckins(Checkin[] checkins) {
			points = new Vector<GeoPoint>();
			for (Checkin checkin : checkins) {
				Location location = checkin.getLocation();
				if (location != null) {
					GeoPoint geopoint = new GeoPoint((int)(location.getLat()*1E6), (int)(location.getLng()*1E6));
					points.add(geopoint);
				} else {
					CompactVenue venue = checkin.getVenue();
					if (venue != null) {
						location = venue.getLocation();
						if (location != null) {
							GeoPoint geopoint = new GeoPoint((int)(location.getLat()*1E6), (int)(location.getLng()*1E6));
							points.add(geopoint);
						}
					}
				}
			}
			if (points.size() > 0) {
				populate();
				mapView.getController().setCenter(getCenter());
				mapView.getController().setZoom(13);
				mapView.invalidate();
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == 1) {
			fsqToken = data.getStringExtra("token");
			if (fsqToken != null) {
				try {
					foursquareapi.authenticateCode(fsqToken);
					Toast.makeText(this, "Token: " + fsqToken, Toast.LENGTH_SHORT).show();
					test4sq();					
				} catch (FoursquareApiException e) {
					Toast.makeText(this, "Error while settings token " + fsqToken, Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
				return;
			} 
		} 
			 
		Toast.makeText(this, "Token activity error", Toast.LENGTH_SHORT).show();		
	}
}
