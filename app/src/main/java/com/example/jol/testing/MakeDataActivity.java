package com.example.jol.testing;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
    private ArrayList<ResultModel> finalData = new ArrayList<>();

    //UBAH VARIABLE DIBAWAH SESUAI KEBUTUHAN
    private static final int NUM_DATA_PER_SECOND = 10;
    //THRESHOLD
    private static final double THRESHOLD_X = 0.89611171777;
    private static final double THRESHOLD_Y = 10.342042659;
    private static final double THRESHOLD_Z = 0.053279248366666;

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
                processData();
            }
        });
    }

    private void processData() {
        File data = new File(path);

        try {
            InputStream inputStream = new FileInputStream(data);
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheet(" Sensor Acc Data ");
            int rowsCount = sheet.getPhysicalNumberOfRows();
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
            int currDataCount = 1, numOfSec = 1;
            double jumX = 0, jumY = 0, jumZ = 0;
            Log.d("sizeRow", String.valueOf(rowsCount - 1));
            for (int r = 1; r < rowsCount; r++) {
                Row row = sheet.getRow(r);
                String x = getCellAsString(row, 1, formulaEvaluator);
                String y = getCellAsString(row, 2, formulaEvaluator);
                String z = getCellAsString(row, 3, formulaEvaluator);
                double lat = Double.parseDouble(getCellAsString(row, 4, formulaEvaluator));
                double lon = Double.parseDouble(getCellAsString(row, 5, formulaEvaluator));

                if (currDataCount <= NUM_DATA_PER_SECOND) {
                    //selama data yang dibaca kurang dari banyaknya jumlah data dalam 1 detik maka x,y,z akan ditambahkan
                    jumX += Double.parseDouble(x);
                    jumY += Double.parseDouble(y);
                    jumZ += Double.parseDouble(z);

                    if (r == rowsCount - 1 && currDataCount == NUM_DATA_PER_SECOND) {
                        //data paling akhir harus diolah juga
                        String hasil = "FALSE";
                        String time = String.valueOf(numOfSec);
                        int thX = 0, thY = 0, thZ = 0;
                        double avgX = jumX / NUM_DATA_PER_SECOND;
                        double avgY = jumY / NUM_DATA_PER_SECOND;
                        double avgZ = jumZ / NUM_DATA_PER_SECOND;
                        if (avgX > THRESHOLD_X)
                            thX = 1;
                        if (avgY > THRESHOLD_Y)
                            thY = 1;
                        if (avgZ > THRESHOLD_Z)
                            thZ = 1;
                        if (thX + thY + thZ >= 2)
                            hasil = "1";

                        ResultModel model = new ResultModel();
                        model.setTime(time);
                        model.setAvgX(String.valueOf(avgX));
                        model.setAvgY(String.valueOf(avgY));
                        model.setAvgZ(String.valueOf(avgZ));
                        model.setThreshX(String.valueOf(thX));
                        model.setThreshY(String.valueOf(thY));
                        model.setThreshZ(String.valueOf(thZ));
                        model.setLatitude(lat);
                        model.setLongitude(lon);
                        model.setHasil(hasil);

                        finalData.add(model);
                    }
                } else {
                    //jika sudah = 10 maka data bisa diolah
                    String hasil = "FALSE";
                    String time = String.valueOf(numOfSec);
                    int thX = 0, thY = 0, thZ = 0;
                    double avgX = jumX / NUM_DATA_PER_SECOND;
                    double avgY = jumY / NUM_DATA_PER_SECOND;
                    double avgZ = jumZ / NUM_DATA_PER_SECOND;
                    if (avgX > THRESHOLD_X)
                        thX = 1;
                    if (avgY > THRESHOLD_Y)
                        thY = 1;
                    if (avgZ > THRESHOLD_Z)
                        thZ = 1;
                    if (thX + thY + thZ >= 2)
                        hasil = "1";

                    ResultModel model = new ResultModel();
                    model.setTime(time);
                    model.setAvgX(String.valueOf(avgX));
                    model.setAvgY(String.valueOf(avgY));
                    model.setAvgZ(String.valueOf(avgZ));
                    model.setThreshX(String.valueOf(thX));
                    model.setThreshY(String.valueOf(thY));
                    model.setThreshZ(String.valueOf(thZ));
                    model.setLatitude(lat);
                    model.setLongitude(lon);
                    model.setHasil(hasil);

                    finalData.add(model);
                    numOfSec++;
                    currDataCount = 1;
                    jumX = 0;
                    jumY = 0;
                    jumZ = 0;

                    jumX += Double.parseDouble(x);
                    jumY += Double.parseDouble(y);
                    jumZ += Double.parseDouble(z);
                }
                currDataCount++;
            }
            saveDatatoExcel();
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

    private void saveDatatoExcel() {
        //Create Blank workbook atau file excel
        final XSSFWorkbook workbook = new XSSFWorkbook();
        //Create a blank sheet
        XSSFSheet spreadsheet = workbook.createSheet(" Result ");
        //Create row object
        XSSFRow headerRow;

        String[] columns = {"Time(s)", "rata2 x/s", "rata2 y/s", "rata2 z/s",
                "Threshold X", "Threshold Y", "Threshold Z", "Latitude", "Longitude", "Hasil"};
        //Header
        headerRow = spreadsheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
        }

        // Create Other rows and cells with sensor data
        int rowNum = 1;
        for (ResultModel data : finalData) {
            XSSFRow row = spreadsheet.createRow(rowNum++);
            row.createCell(0).setCellValue(data.getTime());
            row.createCell(1).setCellValue(data.getAvgX());
            row.createCell(2).setCellValue(data.getAvgY());
            row.createCell(3).setCellValue(data.getAvgZ());
            row.createCell(4).setCellValue(data.getThreshX());
            row.createCell(5).setCellValue(data.getThreshY());
            row.createCell(6).setCellValue(data.getThreshZ());
            row.createCell(7).setCellValue(data.getLatitude());
            row.createCell(8).setCellValue(data.getLongitude());
            row.createCell(9).setCellValue(data.getHasil());
//            System.out.println(data.getX() + "," + data.getY() + "," + data.getZ());
        }
        showDialog(workbook);
    }

    private void saveFile(XSSFWorkbook workbook, String fileName) {
        // Write the output to a file
        FileOutputStream fileOut = null;
        try {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/Sensor_Data_Result");
            myDir.mkdirs();
            String fname = "";
            fname = "SensorData_Result_" + fileName + ".xlsx";
            File file = new File(myDir, fname);
            if (file.exists())
                file.delete();
            fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
            fileOut.close();
            // Closing the workbook
            workbook.close();
            // Tell the media scanner about the new file so that it is
            // immediately available to the user.
            MediaScannerConnection.scanFile(this,
                    new String[]{file.toString()}, null,
                    (path, uri) -> {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    });
            Log.d("path file", "Sensor data has been saved to Data_Sensor.xlsx in " + myDir.getPath() + "!");
            Toast.makeText(MakeDataActivity.this,
                    "Sensor data Result has been saved to " + fname + " in " + myDir.getPath() + "!", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException eF) {
            Log.d("FileNotFound Exc", eF.getMessage());
            eF.printStackTrace();
        } catch (IOException e) {
            Log.d("IO Exc", e.getMessage());
            e.printStackTrace();
        }
    }

    private void showDialog(XSSFWorkbook workbook) {
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
