package geonet.obd.reader.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.List;

import geonet.obd.reader.R;
import geonet.obd.reader.trips.TripListAdapter;
import geonet.obd.reader.trips.TripLog;
import geonet.obd.reader.trips.TripRecord;

public class TripListActivity extends RoboAppCompatActivity {

    private List<TripRecord> records;
    private TripLog triplog = null;
    private TripListAdapter adapter = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        ListView lv = (ListView) findViewById(geonet.obd.reader.R.id.tripList);
        triplog = TripLog.getInstance(this.getApplicationContext());
        records = triplog.readAllRecords();
        adapter = new TripListAdapter(this, records);
        lv.setAdapter(adapter);
        registerForContextMenu(lv);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            // finish the activity
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
