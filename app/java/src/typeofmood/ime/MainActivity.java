package typeofmood.ime;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import typeofmood.ime.latin.settings.SettingsActivity;
import typeofmood.ime.latin.setup.SetupWizardActivity;
import typeofmood.ime.notificationhandler.NotificationHelper;
import typeofmood.ime.notificationhandler.NotificationHelperPhysical;


public class MainActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT >= 21) { // set status bar color
            Window window = getWindow();

            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            // finally change the color
            window.setStatusBarColor(ContextCompat.getColor(getApplication(),R.color.my_statusbar_color));
        }




        Fragment fr = new MainActivityHomeFragment();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_place, fr);
        fragmentTransaction.commit();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        Fragment fr=null;
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);

                        switch (menuItem.getItemId()) {
                            case R.id.nav_Calendar:
                                fr = new MainActivityCalendarFragment();
                                break;
                            case R.id.nav_Home:
                                fr=new MainActivityHomeFragment();
                                break;
                            case R.id.nav_Settings:
                                menuItem.setChecked(false);
                                final Intent intent = new Intent();
                                intent.setClass(getApplicationContext(), SettingsActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra(SettingsActivity.EXTRA_SHOW_HOME_AS_UP, false);
                                intent.putExtra(SettingsActivity.EXTRA_ENTRY_KEY,false);
                                startActivity(intent);
////                            final Intent intent = new Intent();
////                            intent.setClass(getApplicationContext(), SetupWizardActivity.class);
////                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
////                                    | Intent.FLAG_ACTIVITY_NEW_TASK);
////                            startActivity(intent);
                                break;
                            case R.id.nav_Mood:
                                menuItem.setChecked(false);
                                String title = "TypeOfMood";
                                String message = "Please Expand to describe your mood!";
                                NotificationHelper mNotificationHelper = new NotificationHelper(getApplicationContext());
                                NotificationCompat.Builder nb = mNotificationHelper.getTypeOfMoodNotification(title, message);
                                mNotificationHelper.getManager().notify(mNotificationHelper.notification_id, nb.build());
                                break;
                            case R.id.nav_Mood2:
                                menuItem.setChecked(false);
                                NotificationHelperPhysical mNotificationHelperPhysical = new NotificationHelperPhysical(getApplicationContext());
                                NotificationCompat.Builder nbPhysical = mNotificationHelperPhysical.getTypeOfMoodNotification("TypeOfMood", "Please Expand to describe your mood!");
                                mNotificationHelperPhysical.getManager().notify(mNotificationHelperPhysical.notification_id, nbPhysical.build());
                                break;
                            case R.id.nav_Statistics:
                                fr=new MainActivityStatisticsFragment();
                                break;
                            case R.id.nav_dbDebug:
                                fr=new MainActivityDBDebugFragment();
                                menuItem.setChecked(false);
                                break;
                            case R.id.nav_Setup:
//                                final Intent intent3 = new Intent();
//                                intent3.setClass(getApplicationContext(), SetupWizardActivity.class);
//                                intent3.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
//                                        | Intent.FLAG_ACTIVITY_NEW_TASK);
//                                startActivity(intent3);
                                final Intent intent3 = new Intent();
                                intent3.setClass(getApplicationContext(), SetupWizardActivity.class);
                                intent3.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
                                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent3);
                                break;
                            default:
                                fr=null;
                                break;

                        }



                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();
                        if(fr!=null){
                            FragmentManager fm = getFragmentManager();
                            FragmentTransaction fragmentTransaction = fm.beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_place, fr);
                            fragmentTransaction.commit();
                        }

                        return true;
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        if(android.os.Build.VERSION.SDK_INT >= 21)
        {
            finishAndRemoveTask();
        }
    }

}
