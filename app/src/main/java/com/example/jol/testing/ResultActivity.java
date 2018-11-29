package com.example.jol.testing;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

public class ResultActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private TextView lokasi;
    private String path;
    private boolean ada;
    private View lytRes;
    private ArrayList<ResultModel> finaldata = new ArrayList<>();
    private List<LatLng> points = new ArrayList<>();
    private List<Marker> markers = new ArrayList<>();
    private Polyline gpsTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        lytRes = findViewById(R.id.lyt_info);
        lokasi = findViewById(R.id.txtPath);

        findViewById(R.id.btnLoadResult).setOnClickListener(v -> FilePickerBuilder.getInstance().setMaxCount(1)
                .setActivityTheme(R.style.AppTheme)
                .pickFile(ResultActivity.this));
    }

    private void processData() {
        File data = new File(path);
        finaldata = new ArrayList<>();
        try {
            InputStream inputStream = new FileInputStream(data);
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheet(" Result ");
            int rowsCount = sheet.getPhysicalNumberOfRows();
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
            Log.d("sizeRow", String.valueOf(rowsCount - 1));
            for (int r = 1; r < rowsCount; r++) {
                Row row = sheet.getRow(r);
                ResultModel model = new ResultModel();
                model.setTime(row.getCell(0).getStringCellValue());
                model.setAvgX(row.getCell(1).getStringCellValue());
                model.setAvgY(row.getCell(2).getStringCellValue());
                model.setAvgZ(row.getCell(3).getStringCellValue());
                model.setThreshX(row.getCell(4).getStringCellValue());
                model.setThreshY(row.getCell(5).getStringCellValue());
                model.setThreshZ(row.getCell(6).getStringCellValue());
                model.setLatitude(Double.parseDouble(row.getCell(7).getStringCellValue()));
                model.setLongitude(Double.parseDouble(row.getCell(8).getStringCellValue()));
                model.setHasil(row.getCell(9).getStringCellValue());
                finaldata.add(model);
            }
            initMap();
        } catch (FileNotFoundException notfound) {
            Toast.makeText(this, "File tidak ditemukan !", Toast.LENGTH_SHORT).show();
        } catch (IOException ioeror) {
            Toast.makeText(this, "telah terjadi error ketika membaca file !", Toast.LENGTH_SHORT).show();
        }

    }

 private String getCellAsString(Row row, int colKe, FormulaEvaluator formulaEvaluator) {
        String value = "";

        try {
            Cell cell = row.getCell(colKe);
            CellValue cellValue = formulaEvaluator.evaluate(cell);
            switch (cellValue.getCellType()) {
                case Cell.CELL_TYPE_BOOLEAN:
                    value = "" + cellValue.getBooleanValue();
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    value = "" + cellValue.getNumberValue();
                    break;
                case Cell.CELL_TYPE_STRING:
                    value = "" + cellValue.getStringValue();
                    break;
                case Cell.CELL_TYPE_BLANK:
                    value = "-";
                    break;
                default:
                    Toast.makeText(this, "Unknown Cell Type !", Toast.LENGTH_SHORT).show();
            }
        } catch (NullPointerException nullpointer) {
            Log.e("null pointer", "Ada kolom yang kosong !");
//            Toast.makeText(InputDataFragment.this.getContext(), "Ada kolom yang kosong !", Toast.LENGTH_SHORT).show();
        }
        return value;
    }

    private void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map1);
        mapFragment.getMapAsync(this);
        lytRes.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.CYAN);
        polylineOptions.width(4);
        gpsTrack = mMap.addPolyline(polylineOptions);

        if (!points.isEmpty()) {
            for (Marker m : markers)
                m.remove();
            gpsTrack.remove();
        }
        points = new ArrayList<>();
        for (ResultModel data : finaldata) {
            points.add(new LatLng(data.getLatitude(), data.getLongitude()));
        }
        gpsTrack.setPoints(points);

        for (ResultModel data : finaldata) {
            if (data.getHasil().equals("1"))
                markers.add(mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(data.getLatitude(), data.getLongitude()))
                        .title("time = " + data.getTime())));
        }
        CameraUpdate center = CameraUpdateFactory.newLatLng(points.get(0));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(18);
        mMap.moveCamera(center);
        mMap.animateCamera(zoom);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FilePickerConst.REQUEST_CODE_DOC:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    ArrayList<String> paths = new ArrayList<>();
                    paths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
                    if (paths.size() > 0) {
                        String ext = paths.get(0).substring(paths.get(0).lastIndexOf('.') + 1);
//                        ekstensi = ext;
                        if (ext.equals("xlsx")) {
                            path = paths.get(0);
                            lokasi.setText(paths.get(0));
                            ada = true;
                            processData();
                        } else if (ext.equals("xls")) {
                            Toast.makeText(this,
                                    "Tolong save file anda dengan format excel 2007 - 2013 (.xlsx) !", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this,
                                    "Format file harus berupa .xlsx !", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
        }
    }
}
