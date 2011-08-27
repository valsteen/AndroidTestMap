package org.codesoup;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.google.android.maps.*;

public class TestMapActivity extends MapActivity {
 
/*	private static final String CLIENT_ID = "CLIENT_ID_PLACEHOLDER";
	private static final String CLIENT_SECRET = "CLIENT_SECRET_PLACEHOLDER";*/
	
	private MapView mapView;
	private String fsqToken; 
	
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

	// Calls a URI and returns the answer as a JSON object
	private JSONObject executeHttpGet(String uri) throws Exception{
		HttpGet req = new HttpGet(uri);

		HttpClient client = new DefaultHttpClient();
		HttpResponse resLogin = client.execute(req);
		BufferedReader r = new BufferedReader(
				new InputStreamReader(resLogin.getEntity()
						.getContent()));
		StringBuilder sb = new StringBuilder();
		String s = null;
		while ((s = r.readLine()) != null) {
			sb.append(s);
		}

		return new JSONObject(sb.toString());
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
		
		@Override
		public boolean onTap(GeoPoint p, MapView mapView) {
			//mapView.getController().setCenter(p);
			//mapView.getController().zoomIn();
			if (fsqToken == null) {
				Intent intent = new Intent(TestMapActivity.this, ActivityWebView.class);
		        TestMapActivity.this.startActivityForResult(intent, 1);
			} else {
				Toast.makeText(TestMapActivity.this, "Already got: " + fsqToken, Toast.LENGTH_SHORT).show();
			}
			return true;
		}
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == 1) {
			fsqToken = data.getStringExtra("token");
			if (fsqToken != null) {
				Toast.makeText(this, "Token: " + fsqToken, Toast.LENGTH_SHORT).show();
				return;
			} 
		} 
			 
		Toast.makeText(this, "Token activity error", Toast.LENGTH_SHORT).show();		
	}
}
