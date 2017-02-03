package edu.neu.madcourse.ruihaohuang.about;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.Spanned;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import edu.neu.madcourse.ruihaohuang.R;

public class AboutActivity extends AppCompatActivity {

    private final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getStringArray(R.array.my_information)[0]);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });


        ((TextView) findViewById(R.id.text_basic_information))
                .setText(fromHtml(getMyBasicInformation()));
        ((ImageView) findViewById(R.id.image_head_shot))
                .setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),
                        R.drawable.about_head_shot));

        // Initialize its content to PERMISSION DENIED
        ((TextView) findViewById(R.id.text_IMEI))
                .setText(String.format(getString(R.string.description_IMEI),
                        getString(R.string.info_permission_denied)));
        // Check for permission
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_PHONE_STATE);
        // Failed: request for new permission
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(AboutActivity.this,
                    Manifest.permission.READ_PHONE_STATE)) {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.toast_request_permission_read_phone_state),
                        Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(AboutActivity.this,
                        new String[] {Manifest.permission.READ_PHONE_STATE},
                        MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
            }
        } else {  // Succeed: access IMEI with permission
            ((TextView) findViewById(R.id.text_IMEI))
                    .setText(String.format(getString(R.string.description_IMEI),
                            ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId()));
        }
    }

    String getMyBasicInformation() {
        String html = "";
        String[] descriptions = getResources().getStringArray(R.array.descriptions);
        String[] myInformation = getResources().getStringArray(R.array.my_information);
        for (int i = 0; i < descriptions.length; ++i) {
            html += "<p>";
            html += String.format(descriptions[i], myInformation[i]);
            html += "</p>";
        }

        return html;
    }

    /**
     * Reference: http://stackoverflow.com/questions/37904739/html-fromhtml-deprecated-in-android-n
     * @param html a html string
     * @return Spanned object that can be formatted by TextView
     */
    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ((TextView) findViewById(R.id.text_IMEI))
                            .setText(String.format(getString(R.string.description_IMEI),
                                    ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId()));
                }
                return;
            default:
        }
    }
}
