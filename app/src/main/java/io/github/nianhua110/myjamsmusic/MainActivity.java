package io.github.nianhua110.myjamsmusic;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import io.github.nianhua110.myjamsmusic.Drawers.NavigationDrawerFragment;
import io.github.nianhua110.myjamsmusic.ListViewFragment.ListViewFragment;
import io.github.nianhua110.myjamsmusic.Utils.Common;

public class MainActivity extends AppCompatActivity {
    //Context and Common object(s).
    private Context mContext;
    private Common mApp;
    //UI elements.
    private FrameLayout mDrawerParentLayout;
    private DrawerLayout mDrawerLayout;
    private RelativeLayout mNavDrawerLayout;
    private RelativeLayout mCurrentQueueDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
   // private QueueDrawerFragment mQueueDrawerFragment;
    private Menu mMenu;

    //Current fragment params.
    private Fragment mCurrentFragment;
    public static int mCurrentFragmentId;
    public static int mCurrentFragmentLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = getApplicationContext();
        mApp = (Common) getApplicationContext();

       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);
       // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
       // setSupportActionBar(toolbar);
        //Context and Common object(s).
        //Init the UI elements.
        mDrawerParentLayout = (FrameLayout) findViewById(R.id.main_activity_root);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_activity_drawer_root);
        mNavDrawerLayout = (RelativeLayout) findViewById(R.id.nav_drawer_container);
        mCurrentQueueDrawerLayout = (RelativeLayout) findViewById(R.id.current_queue_drawer_container);

        loadDrawerFragments();
        ListViewFragment listViewFragment = new ListViewFragment();
        switchContent(listViewFragment);
    }

    /**
     * Loads the drawer fragments.
     */
    private void loadDrawerFragments() {
        //Load the navigation drawer.
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_drawer_container, new NavigationDrawerFragment())
                .commit();

        //Load the current queue drawer.
      //  mQueueDrawerFragment = new QueueDrawerFragment();
    //    getSupportFragmentManager().beginTransaction()
   //             .replace(R.id.current_queue_drawer_container, mQueueDrawerFragment)
  //              .commit();

    }


    /**
     * Loads the specified fragment into the target layout.
     */
    public void switchContent(Fragment fragment) {
        // Reset action bar
        //getActionBar().setDisplayHomeAsUpEnabled(true);
       // getActionBar().setDisplayShowHomeEnabled(true);
        //getActionBar().setDisplayShowCustomEnabled(false);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainActivityContainer, fragment)
                .commit();

        //Close the drawer(s).
     //   mDrawerLayout.closeDrawer(Gravity.START);
      //  invalidateOptionsMenu();

    }

    /**
     * Sets the entire activity-wide theme.
     */
    private void setTheme() {
        //Set the UI theme.
    /*    if (mApp.getCurrentTheme()==Common.DARK_THEME) {
            setTheme(R.style.AppTheme);
        } else {
            setTheme(R.style.AppThemeLight);
        }*/

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
