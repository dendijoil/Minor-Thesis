package com.example.jol.testing;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

public class MakeDataActivity extends AppCompatActivity {
    private Button btOpen, btMake;
    private TextView lokasi;
    private String path;
    boolean ada;
    private View lytRes;
    private ArrayList<ModelSensor> dataSensor = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_data);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btOpen = findViewById(R.id.btnLoad);
        btMake = findViewById(R.id.btnProc);
        lokasi = findViewById(R.id.txtPath);
        lytRes = findViewById(R.id.lyt_info);

        lytRes.setVisibility(View.GONE);

        btOpen.setOnClickListener(v -> FilePickerBuilder.getInstance().setMaxCount(1)
                .setActivityTheme(R.style.AppTheme)
                .pickFile(MakeDataActivity.this));

        btMake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writedataToObject();
            }
        });


    }

    private void writedataToObject() {
        File data = new File(path);

        try {
            InputStream inputStream = new FileInputStream(data);
            HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
            HSSFSheet sheet = workbook.getSheet(" Sensor Acc Data ");
            int rowsCount = sheet.getPhysicalNumberOfRows();
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
            Gson gson = new Gson();

            for (int r = 1; r < rowsCount; r++) {
                Row row = sheet.getRow(r);
                ModelSensor modelSensor = new ModelSensor();

                modelSensor.setTime(getCellAsString(row, 0, formulaEvaluator));
                modelSensor.setX(getCellAsString(row, 1, formulaEvaluator));
                modelSensor.setY(getCellAsString(row, 2, formulaEvaluator));
                modelSensor.setZ(getCellAsString(row, 3, formulaEvaluator));
                modelSensor.setLatitude(Double.parseDouble(getCellAsString(row, 4, formulaEvaluator)));
                modelSensor.setLongitude(Double.parseDouble(getCellAsString(row, 5, formulaEvaluator)));
                modelSensor.setCurrSpeed(getCellAsString(row, 6, formulaEvaluator));
//                Log.d("data", gson.toJson(modelSensor));
                dataSensor.add(modelSensor);
            }
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
                case BOOLEAN:
                    value = "" + cellValue.getBooleanValue();
                    break;
                case NUMERIC:
                    value = "" + cellValue.getNumberValue();
                    break;
                case STRING:
                    value = "" + cellValue.getStringValue();
                    break;
                case BLANK:
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
                            lytRes.setVisibility(View.VISIBLE);
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
