package cl.adachersoft.location.views;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import cl.adachersoft.location.MapsActivity;
import cl.adachersoft.location.R;
import cl.adachersoft.location.background.LocationByAddress;
import cl.adachersoft.location.background.LocationCallback;

import static android.R.attr.start;

public class MainActivity extends AppCompatActivity implements LocationCallback {

    private static int PETICION_PERMISO_LOCALIZACION = 1;
    public static final String LAT = "LAT";
    public static final String LNG = "LNG";
    private EditText streetEt, districtEt, regionEt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        streetEt = (EditText) findViewById(R.id.stretEt);
        districtEt = (EditText) findViewById(R.id.districtEt);
        regionEt = (EditText) findViewById(R.id.regionEt);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PETICION_PERMISO_LOCALIZACION);
        }

        Button button = (Button) findViewById(R.id.sendBtn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String street = streetEt.getText().toString();
                String district = districtEt.getText().toString();
                String region = districtEt.getText().toString();
                new GetLocation(MainActivity.this).execute(street, district, region);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PETICION_PERMISO_LOCALIZACION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("LOGTAG", "Permiso concedido");
            } else {
                Log.e("LOGTAG", "Permiso denegado");
            }
        }
    }

    private class GetLocation extends LocationByAddress {

        private ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

        public GetLocation(LocationCallback callback) {
            super(callback);
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            progressDialog.dismiss();
        }
    }


    @Override
    public void noAddress() {

        Handler h = new Handler(Looper.getMainLooper());
        h.post(new Runnable() {
            public void run() {
                mensaje("Dirección inválida");
            }
        });

    }

    @Override
    public void fail() {

        Handler h = new Handler(Looper.getMainLooper());
        h.post(new Runnable() {
            public void run() {
                mensaje("Sin acceso a la red, favor vuelva a intentar");
            }
        });

    }

    @Override
    public void success(String lat, String lng) {
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        intent.putExtra(LAT, lat);
        intent.putExtra(LNG, lng);
        startActivity(intent);

    }

    private void mensaje(String mensaje) {

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Atención:");
        alertDialog.setMessage(mensaje);
        alertDialog.setButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.setIcon(R.drawable.common_google_signin_btn_text_dark_normal);
        alertDialog.show();
    }

}
