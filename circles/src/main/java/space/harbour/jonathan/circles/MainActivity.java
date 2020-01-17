package space.harbour.jonathan.circles;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import space.harbour.jonathan.circles.services.WhatsappAccessibilityService;

public class MainActivity extends AppCompatActivity
        implements CircleChooserFragment.SendCircle {

    private final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    private DataManager dataManager;
    private WhatsAppManager wam;

    private MessagesBankFragment messagesBankFragment;
    private CircleFragment circleFragment;
    private CircleChooserFragment circleChooserFragment;
    private AllContactsFragment contactsFragment;
    private Fragment currentFragment;

    private MessagesBankFragment getMessageFragment() {
        if (messagesBankFragment == null)
            messagesBankFragment = MessagesBankFragment.newInstance();
        return messagesBankFragment;
    }

    private CircleFragment getCircleFragment() {
        if (circleFragment == null)
            circleFragment = CircleFragment.newInstance();
        return circleFragment;
    }

    private AllContactsFragment getContactsFragment() {
        if (contactsFragment == null)
            contactsFragment = AllContactsFragment.newInstance();
        return contactsFragment;
    }

    private CircleChooserFragment getCircleChooserFragment() {
        if (circleChooserFragment == null)
            circleChooserFragment = CircleChooserFragment.newInstance();
        return circleChooserFragment;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public WhatsAppManager getWam() {
        return wam;
    }

    private boolean isAccessibilityOn(Context context, Class<? extends AccessibilityService> clazz) {
        int accessibilityEnabled = 0;
        final String service = context.getPackageName() + "/" + clazz.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getApplicationContext().getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException ignored) {
        }

        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                colonSplitter.setString(settingValue);
                while (colonSplitter.hasNext()) {
                    String accessibilityService = colonSplitter.next();

                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Accessibility issues
        if (!isAccessibilityOn(getApplicationContext(), WhatsappAccessibilityService.class)) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            getApplicationContext().startActivity(intent);
        }

        // Contacts permissions
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted; request the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }

        // Initialize services
        wam = new WhatsAppManager(getApplicationContext());
        wam.loadContacts();
        dataManager = new DataManager(getApplicationContext());
        currentFragment = getCircleChooserFragment();

        // Bottom navigation managing
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        bottomNavigationView.getMenu().getItem(1).setChecked(true);

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.hide(currentFragment);
                        switch (item.getItemId()) {
                            case R.id.navigation_friends:
                                currentFragment = getCircleChooserFragment().setService("Contacts");
                                break;
                            case R.id.navigation_messages_bank:
                                currentFragment = getCircleChooserFragment().setService("Messages");
                                break;
                            case R.id.navigation_all_contacts:
                                currentFragment = getContactsFragment();
                                break;
                        }
                        transaction.show(currentFragment);
                        transaction.commit();
                        return true;
                    }
                });

        // Manually initializing fragments and displaying the first fragment (One time only!)
        // Get a circle from the JSON
        String initCircle = dataManager.getAllCircles().get(0);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.frame_layout, getMessageFragment().setCircle(initCircle));
        transaction.hide(getMessageFragment());
        transaction.add(R.id.frame_layout, getCircleFragment().setCircle(initCircle));
        transaction.hide(getCircleFragment());
        transaction.add(R.id.frame_layout, getContactsFragment());
        transaction.hide(getContactsFragment());
        transaction.add(R.id.frame_layout, getCircleChooserFragment());
        transaction.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataManager.saveData();
    }

    @Override
    public void sendCircle(String circle, String service) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.hide(getCircleChooserFragment());
        if (service.equals("Messages")) {
            currentFragment = getMessageFragment().setCircle(circle);
            ((MessagesBankFragment) currentFragment).refreshMessages();
            transaction.show(currentFragment);
        } else {
            currentFragment = getCircleFragment().setCircle(circle);
            ((CircleFragment) currentFragment).refreshFriendsList();
            transaction.show(currentFragment);
        }
        transaction.commit();
    }
}
