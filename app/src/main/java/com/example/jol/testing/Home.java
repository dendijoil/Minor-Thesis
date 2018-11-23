package com.example.jol.testing;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.Random;


public class Home extends AppCompatActivity implements SensorEventListener, IBaseGpsListener {

    private TextView xText, yText, zText;
    private Sensor SensorKu;
    private SensorManager SM;
    private Button btStart, btStop, btSave, btMap;
    private ArrayList<ModelSensor> dataSensor = new ArrayList<>();
    private String state = "", X, Y, Z;
    TextView resetClock;
    Chronometer mChronometer;
    int timerCount = 1;
    double longitude = 0, latitude = 0;
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 2;
    private static final int MY_PERMISSIONS_REQUEST_COARSE_LOCATION = 4;
    String strUnits = "miles/hour";
    String strCurrentSpeed, strSpeed;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Ini baru Loh dari wildan new
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // membuat sensor

        SM = (SensorManager) getSystemService(SENSOR_SERVICE);

        // sensor accelerometer

        SensorKu = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // menambahkan textViewnya
        xText = findViewById(R.id.xText);
        yText = findViewById(R.id.yText);
        zText = findViewById(R.id.zText);
        resetClock = findViewById(R.id.chronoReset);

        // menghubungkan view button dengan controllernya
        btStart = findViewById(R.id.btnStart);
        btStop = findViewById(R.id.btnStop);
        btSave = findViewById(R.id.btnSave);
        btMap = findViewById(R.id.btnMap);
        mChronometer = findViewById(R.id.chronometer);

        btMap.setEnabled(false);
//        dummyData();

        //action ketika button start diklik
        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initLocation();
                timerCount = 1;
                state = "start";
                mChronometer.setBase(SystemClock.elapsedRealtime());
                mChronometer.start();
                //Jalankan sensor ketika tombol start diklik
                dataSensor = new ArrayList<>();
                SM.registerListener(Home.this, SensorKu, SensorManager.SENSOR_DELAY_NORMAL);
                Toast.makeText(Home.this, "Sensor Running..!", Toast.LENGTH_SHORT).show();
            }
        });

        //action ketika button stop diklik
        btStop.setOnClickListener(view -> {
            timerCount = 1;
            state = "stop";
            mChronometer.stop();
            //Stop Sensor saat tombol stop diklik
            SM.unregisterListener(Home.this, SensorKu);
            locationManager.removeUpdates(this);
            Toast.makeText(Home.this, "Sensor Stopped...!", Toast.LENGTH_SHORT).show();
            if (!dataSensor.isEmpty())
                btMap.setEnabled(true);
        });

        //action ketika button save diklik
        btSave.setOnClickListener(view -> {
            //tombol save hanya bisa berfungsi ketika tombol stop sudah ditekan dan dataSensor tidak kosong
            if (state.equals("stop") && !dataSensor.isEmpty()) {
//                    Toast.makeText(Home.this, "tes", Toast.LENGTH_SHORT).show();
                //kode savenya dibuat ke method lain aja, klo ditulis disini kepanjangan, gaenak diliat :v
                saveDatatoExcel();
            } else {
                Toast.makeText(Home.this, "Data Sensor kosong / sensor masih berjalan !", Toast.LENGTH_SHORT).show();
            }
        });

        btMap.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            intent.putExtra("data", dataSensor);
            startActivity(intent);
        });

        resetClock.setOnClickListener(v -> {
            mChronometer.setBase(SystemClock.elapsedRealtime());
            timerCount = 1;
        });

        mChronometer.setOnChronometerTickListener(chronometer -> {
            long timeElapsed = SystemClock.elapsedRealtime() - mChronometer.getBase();
            int hours = (int) (timeElapsed / 3600000);
            int minutes = (int) (timeElapsed - hours * 3600000) / 60000;
            int second = (int) (timeElapsed - hours * 3600000 - minutes * 60000) / 1000;
            System.out.println("second : " + (second + 1));
            timerCount = second + 1;
        });
    }

    private void dummyData() {
        dataSensor.add(new ModelSensor("time", "x", "y", "z", "0", -6.8924471, 107.6111778));
        dataSensor.add(new ModelSensor("time", "x", "y", "z", "0", -6.974028, 107.62834));
    }

    private void initLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        } else {
            //get Location
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, this);
            updateSpeed(null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initLocation();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Failed To init Location, please Restart the Application !", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    private void saveDatatoExcel() {
        //Create Blank workbook atau file excel
        final HSSFWorkbook workbook = new HSSFWorkbook();
        //Create a blank sheet
        HSSFSheet spreadsheet = workbook.createSheet(" Sensor Acc Data ");
        //Create row object
        HSSFRow headerRow;

        String[] columns = {"Time", "X", "Y", "Z", "Latitude", "Longitude", "Speed(meter/second)"};
        //Header
        headerRow = spreadsheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
        }

        // Create Other rows and cells with sensor data
        int rowNum = 1;
        for (ModelSensor data : dataSensor) {
            HSSFRow row = spreadsheet.createRow(rowNum++);
            row.createCell(0).setCellValue(data.getTime());
            row.createCell(1).setCellValue(data.getX());
            row.createCell(2).setCellValue(data.getY());
            row.createCell(3).setCellValue(data.getZ());
            row.createCell(4).setCellValue(data.getLatitude());
            row.createCell(5).setCellValue(data.getLongitude());
            row.createCell(6).setCellValue(data.getCurrSpeed());
//            System.out.println(data.getX() + "," + data.getY() + "," + data.getZ());
        }
        showDialog(workbook);
    }

    private void saveFile(HSSFWorkbook workbook, String fileName) {
        // Write the output to a file
        FileOutputStream fileOut = null;
        try {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/Sensor_Data");
            myDir.mkdirs();
            String fname = "";
            fname = "SensorData_" + fileName + ".xlsx";
            File file = new File(myDir, fname);
            if (file.exists())
                file.delete();
            fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
            fileOut.close();
            // Closing the workbook
            workbook.close();
            Log.d("path file", "Sensor data has been saved to Data_Sensor.xlsx in " + myDir.getPath() + "!");
            Toast.makeText(Home.this, "Sensor data has been saved to Data_Sensor.xlsx in " + myDir.getPath() + "!", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException eF) {
            Log.d("FileNotFound Exc", eF.getMessage());
            eF.printStackTrace();
        } catch (IOException e) {
            Log.d("IO Exc", e.getMessage());
            e.printStackTrace();
        }
    }

    private void showDialog(HSSFWorkbook workbook) {
        Dialog dialogOption = new Dialog(this);
        dialogOption.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogOption.setContentView(R.layout.dialog_input_filename);
        dialogOption.setCancelable(true);
        ViewGroup.LayoutParams params = dialogOption.getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialogOption.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);


        EditText name = dialogOption.findViewById(R.id.etFileName);
        Button btOk = dialogOption.findViewById(R.id.btOk);

        btOk.setOnClickListener(v -> {
            if (!name.getText().toString().equals("")) {
                saveFile(workbook, name.getText().toString());
                dialogOption.dismiss();
            } else
                Toast.makeText(this, "Please fill the File Name", Toast.LENGTH_SHORT).show();
        });
        dialogOption.show();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        X = String.valueOf(event.values[0]);
        Y = String.valueOf(event.values[1]);
        Z = String.valueOf(event.values[2]);
        xText.setText("X = " + X);
        yText.setText("Y = " + Y);
        zText.setText("Z = " + Z);
        if (dataSensor.size() < (timerCount * 10)) {
            dataSensor.add(new ModelSensor(getTimeRecord(), X, Y, Z, strCurrentSpeed, latitude, longitude));
        }

    }

    private String getTimeRecord() {
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        return dateFormat.format(date);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
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

    private boolean useMetricUnits() {
        // return true will make the speed in meters/second, false in miles/hour
        return true;
    }

    private void updateSpeed(CLocation location) {
        // TODO Auto-generated method stub
        float nCurrentSpeed = 0;

//        If you want to convert Meters/Second to kmph-1 then you need to multipl the Meters/Second answer from 3.6
//
//        Speed from kmph-1 = 3.6 * (Speed from ms-1)

        if (location != null) {
            location.setUseMetricunits(this.useMetricUnits());
            nCurrentSpeed = location.getSpeed();
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            Log.d("speed", String.valueOf(nCurrentSpeed));
            Log.d("latlng", latitude + ", " + longitude);
        }

        Formatter fmt = new Formatter(new StringBuilder());
        fmt.format(Locale.US, "%5.1f", nCurrentSpeed);
        strCurrentSpeed = fmt.toString();
        strCurrentSpeed = strCurrentSpeed.replace(' ', '0');

        if (this.useMetricUnits()) {
            strUnits = "meters/second";
        }
        strSpeed = strCurrentSpeed + " " + strUnits;
        TextView txtCurrentSpeed = (TextView) this.findViewById(R.id.txtCurrentSpeed);
        txtCurrentSpeed.setText(strSpeed);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            CLocation myLocation = new CLocation(location, this.useMetricUnits());
            this.updateSpeed(myLocation);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Please Enable Your GPS !", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onGpsStatusChanged(int event) {

    }
}
