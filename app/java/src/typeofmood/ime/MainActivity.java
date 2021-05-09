package typeofmood.ime;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.navigation.NavigationView;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.Window;
import android.view.WindowManager;

import typeofmood.ime.latin.settings.SettingsActivity;
import typeofmood.ime.latin.setup.SetupWizardActivity;
import typeofmood.ime.notificationhandler.NotificationHelper;
import typeofmood.ime.notificationhandler.NotificationHelperPHQ9;


public class MainActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private static NotificationHelper mNotificationHelper;
    private static NotificationCompat.Builder nb;

    private static NotificationHelperPHQ9 mNotificationHelperPHQ9;
    private static NotificationCompat.Builder nb2;


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
        mNotificationHelper = new NotificationHelper(getApplicationContext());
        mNotificationHelperPHQ9 = new NotificationHelperPHQ9(getApplicationContext());

        nb=new NotificationCompat.Builder(getApplicationContext(),"TypeOfMoodChannelID");
        nb2=new NotificationCompat.Builder(getApplicationContext(),"TypeOfMoodChannelID");




        NavigationView navigationView = findViewById(R.id.nav_view);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("user_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String pref_ID= pref.getString("ID", "");
        long latest_phq9_date= pref.getLong("latest_phq9_date", 0);
        editor.apply();

        Menu nav_Menu = navigationView.getMenu();
        nav_Menu.findItem(R.id.nav_Mood2).setVisible(false);
        invalidateOptionsMenu();
        if(pref_ID.equals("test")||pref_ID.equals("emulatortest")){
            nav_Menu.findItem(R.id.nav_dbDebug).setVisible(true);
            invalidateOptionsMenu();
        }

        long phq9_period=System.currentTimeMillis()-latest_phq9_date;
        long two_weeks=(long)(1000*60*60*24*14);// 1000 millis * 60 seconds * 60 minutes * 24 hours *14 days//1000*60*60*24*14
        if(phq9_period>two_weeks){
            String title = "TypeOfMood";
            String message = "Please Expand to describe your mood!";
            nb2 = mNotificationHelperPHQ9.getTypeOfMoodNotification(title, message, nb2);
            mNotificationHelperPHQ9.getManager().notify(mNotificationHelperPHQ9.notification_id, nb2.build());
        }


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
                                nb = mNotificationHelper.getTypeOfMoodNotification(title, message, nb);
                                mNotificationHelper.getManager().notify(mNotificationHelper.notification_id, nb.build());
                                break;
                            case R.id.nav_Mood2:
                                menuItem.setChecked(false);
//                                NotificationHelperPhysical mNotificationHelperPhysical = new NotificationHelperPhysical(getApplicationContext());
//                                NotificationCompat.Builder nbPhysical = mNotificationHelperPhysical.getTypeOfMoodNotification("TypeOfMood", "Please Expand to describe your mood!");
//                                mNotificationHelperPhysical.getManager().notify(mNotificationHelperPhysical.notification_id, nbPhysical.build());
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
