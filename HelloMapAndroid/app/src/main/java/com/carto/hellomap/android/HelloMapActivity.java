package com.carto.hellomap.android;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.carto.core.BinaryData;
import com.carto.core.MapPos;
import com.carto.datasources.LocalVectorDataSource;

import com.carto.datasources.MBTilesTileDataSource;
import com.carto.graphics.Bitmap;
import com.carto.layers.CartoBaseMapStyle;
import com.carto.layers.CartoOnlineVectorTileLayer;
import com.carto.layers.CartoVectorTileLayer;
import com.carto.layers.VectorLayer;
import com.carto.layers.VectorTileLayer;
import com.carto.projections.Projection;
import com.carto.styles.MarkerStyle;
import com.carto.styles.MarkerStyleBuilder;
import com.carto.ui.MapClickInfo;
import com.carto.ui.MapEventListener;
import com.carto.ui.MapView;
import com.carto.vectorelements.Marker;
import com.carto.vectortiles.VectorTileDecoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

public class HelloMapActivity extends Activity {

    static final String LICENSE = "XTUMwQ0ZDNGo4cFZKMklMZHlFQVdZditGYzduazV4QzZBaFVBbkJzRUExMmhqVnFxSEY3bkpTUFVyM0M2NzdRPQoKYXBwVG9rZW49YzQxYTM5ZjktN2I5MC00MThhLTkyZjUtN2I0ODljZDYxZmFhCnBhY2thZ2VOYW1lPWNvbS5jYXJ0by5oZWxsb21hcC5hbmRyb2lkCm9ubGluZUxpY2Vuc2U9MQpwcm9kdWN0cz1zZGstYW5kcm9pZC00LioKd2F0ZXJtYXJrPWNhcnRvZGIK";

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        MapView.registerLicense(LICENSE, getApplicationContext());

        // Set view from layout resource
        setContentView(R.layout.activity_hello_map);
        mapView = (MapView) this.findViewById(R.id.map_view);

        // Add base map
//        CartoOnlineVectorTileLayer baseLayer = new CartoOnlineVectorTileLayer(CartoBaseMapStyle.CARTO_BASEMAP_STYLE_VOYAGER);
//        mapView.getLayers().add(baseLayer);

        // Offline map data source
        String mbTileFile = "ghana.mbtiles";

        MBTilesTileDataSource tileDataSource = null;
        try {
            String localDir = getExternalFilesDir(null).toString();
            copyAssetToSDCard(getAssets(), mbTileFile, localDir);

            String path = localDir + "/" + mbTileFile;
            Log.i("Hello map","copy done to " + path);
            tileDataSource = new MBTilesTileDataSource(path);
            mapView.getOptions().setWatermarkBitmap(getBitmapFromAsset(this,localDir+"/blank.png"));
        } catch (IOException e) {
            e.printStackTrace();

        }

        // Create tile decoder based on Voyager style and VectorTileLayer
        VectorTileDecoder tileDecoder = CartoVectorTileLayer.createTileDecoder(CartoBaseMapStyle.CARTO_BASEMAP_STYLE_VOYAGER);
        VectorTileLayer offlineLayer = new VectorTileLayer(tileDataSource, tileDecoder);
        mapView.getLayers().add(offlineLayer);
//        mapView.getOptions().setWatermarkScale(0.1f);

        // Set default location and zoom
        MapPos gh = mapView.getOptions().getBaseProjection().fromWgs84(new MapPos(-1.0307118,7.9527706));
        mapView.setFocusPos(gh, 0);
        mapView.setZoom(10, 0);

        Marker marker = addMarkerToPosition(gh);

        mapView.setMapEventListener(new MyMapEventListener(marker));

        setTitle("Hello Map");
    }
    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream istr;
        android.graphics.Bitmap bitmap = null;
        Bitmap cartoBitmap = null;
        try {
            istr = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(istr);
            ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getByteCount());
            bitmap.copyPixelsToBuffer(byteBuffer);
            byteBuffer.rewind();
            cartoBitmap = Bitmap.createFromCompressed(new BinaryData(byteBuffer.array()));
        } catch (IOException e) {
            // handle exception
        }

        return cartoBitmap ;
//        return Bitmap.createFromCompressed(new BinaryData());
    }


    public void copyAssetToSDCard(AssetManager assetManager, String fileName, String toDir) throws IOException {

        InputStream in = assetManager.open(fileName);
        File outFile = new File(toDir, fileName);

        // NB! Remember to check if storage is available and has enough space

        if (outFile.exists()) {
            // File already exists, no need to recreate
            return;
        }

        OutputStream out = new FileOutputStream(outFile);
        copyFile(in, out);
        in.close();
        in = null;
        out.flush();
        out.close();
        out = null;
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {

        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
    private Marker addMarkerToPosition(MapPos position)
    {
        // Create a new layer and add it to the map
        Projection projection = mapView.getOptions().getBaseProjection();
        LocalVectorDataSource datasource = new LocalVectorDataSource(projection);
        VectorLayer layer = new VectorLayer(datasource);
        mapView.getLayers().add(layer);

        // Build Marker style
        MarkerStyleBuilder builder = new MarkerStyleBuilder();
        builder.setSize(20);

        // CARTO has its own Color class. Do not mix up with android.graphics.Color
        builder.setColor(new com.carto.graphics.Color(Color.WHITE));

        MarkerStyle style = builder.buildStyle();

        // Create the actual Marker and add it to the data source
        Marker marker = new Marker(position, style);
        datasource.add(marker);

        return marker;
    }

    /*********************
        MAP CLICK LISTENER
    **********************/
    private class MyMapEventListener extends MapEventListener {

        private int[] colors = { Color.BLUE, Color.RED, Color.YELLOW, Color.GREEN };

        private Marker marker;

        private Random random;

        public MyMapEventListener(Marker marker) {
            this.marker = marker;
            random = new Random();
        }

        @Override
        public void onMapClicked(MapClickInfo mapClickInfo) {
            super.onMapClicked(mapClickInfo);

            MarkerStyleBuilder builder = new MarkerStyleBuilder();

            // Set random size (within reasonable limits)
            int size = getRandomInt(15, 50);
            builder.setSize(size);

            // Set random color from our list
            int color = colors[getRandomInt(0, colors.length)];
            builder.setColor(new com.carto.graphics.Color(color));

            // Set a new style to the marker
            marker.setStyle(builder.buildStyle());
        }

        private int getRandomInt(int min, int max) {
            return random.nextInt(max - min) + min;
        }
    }
}
