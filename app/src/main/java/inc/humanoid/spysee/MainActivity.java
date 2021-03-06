package inc.humanoid.spysee;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    Button wifiButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        final Intent intent =new Intent(this,FullscreenActivity.class);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(intent);
            }
        });

//        TODO action bar butoon animation stop
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        wifiButton = (Button) findViewById(R.id.wifi_button);
        final WifiConfiguration wifi =new WifiConfiguration();
        wifi.SSID= "\"SpySee 96\"";
        wifi.preSharedKey= "\"10091996\"";

        wifiButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg1 ){
                @SuppressLint("WifiManagerLeak") WifiManager wifimanager = (WifiManager)getSystemService(WIFI_SERVICE);
                int netID = wifimanager != null ? wifimanager.addNetwork(wifi) : 0;
                if (wifimanager != null) {
                    wifimanager.disconnect();
                    wifimanager.enableNetwork(netID,true);
                    wifimanager.reconnect();
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent =new Intent(this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_basic_mode) {
            Intent intent =new Intent(this,FullscreenActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_landscape_mode) {
            Intent intent =new Intent(this,FullscreenActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            // Intent intent =new Intent(this,ShareActivity.class);
            // startActivity(intent);
        } else if (id == R.id.nav_contact_us) {
            // Intent intent =new Intent(this,ContactUsActivity.class);
            // startActivity(intent);
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
