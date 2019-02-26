package com.canal.defectdetection.Activities;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.canal.defectdetection.Adapter.AttachmentAdapter;
import com.canal.defectdetection.R;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class ReportFormActivity extends AppCompatActivity {

    private static final int FILE_SELECT_CODE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    Spinner whenSpinner, whatSpinner;
    LinearLayout attachLayout;
    RecyclerView imageRecycler;
    Button cancelButton, sendButton;
    AttachmentAdapter attachmentAdapter;
    List<String> imageList;
    TextView complaintNo;
    Toolbar toolbar;
    List<String> testList;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_form);
        imageList = new ArrayList<>();
        initializeView();
        checkForPermission();
        assignAdapter();

    }

    private void checkForPermission() {
        Permissions.check(ReportFormActivity.this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                getString(R.string.permission_info), new Permissions.Options()
                        .setRationaleDialogTitle("Info"),
                new PermissionHandler() {
                    @Override
                    public void onGranted() {
                        attachLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showFileChooser();
                            }
                        });

                        sendButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new generateAsyncTask().execute();
                            }
                        });


                        cancelButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                imageList.clear();
                                whatSpinner.setSelection(0);
                                whenSpinner.setSelection(0);
                                complaintNo.setText("");
                                attachmentAdapter.notifyDataSetChanged();
                            }
                        });
                    }

                    @Override
                    public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                        Toast.makeText(context, "Denied:\n" + Arrays.toString(deniedPermissions.toArray()),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public boolean onBlocked(Context context, ArrayList<String> blockedList) {
                        Toast.makeText(context, "Blocked:\n" + Arrays.toString(blockedList.toArray()),
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public void onJustBlocked(Context context, ArrayList<String> justBlockedList,
                                              ArrayList<String> deniedPermissions) {
                        Toast.makeText(context, "Blocked:\n" + Arrays.toString(deniedPermissions.toArray()),
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }


    public class generateAsyncTask extends AsyncTask<Void, Integer, Void> {


        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(ReportFormActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Generating pdf...");
        }

        @Override
        protected Void doInBackground(Void... mApi) {
            generatePDF();
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(Void result) {
            progressDialog.dismiss();
            Toast.makeText(ReportFormActivity.this, getString(R.string.pdf_generate), Toast.LENGTH_LONG).show();
            imageList.clear();
            whatSpinner.setSelection(0);
            whenSpinner.setSelection(0);
            complaintNo.setText("");
            attachmentAdapter.notifyDataSetChanged();
            super.onPostExecute(result);


        }
    }


    public void generatePDF() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String dateForm = dateFormat.format(new Date());
        String pdfFileName = "DefectPDF_" + dateForm + ".pdf";
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/CanalDefects";
        File dir = new File(path);
        if (!dir.exists())
            dir.mkdirs();
        File filePDF = new File(dir, pdfFileName);
        Rectangle pageSize = new Rectangle(PageSize.A4);
        Document doc = new Document(pageSize);
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(filePDF);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        PdfWriter pdfWriter = null;
        try {
            pdfWriter = PdfWriter.getInstance(doc, fOut);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        Rectangle rect = new Rectangle(10, 10, 580, 830);
        pdfWriter.setBoxSize("art", rect);
        doc.open();
        String whenValue = testList.get(whenSpinner.getSelectedItemPosition());
        String whatValue = testList.get(whenSpinner.getSelectedItemPosition());
        try {
            com.itextpdf.text.Font headingFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 15, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            com.itextpdf.text.Font bfBold12 = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 12);
            com.itextpdf.text.Font bf12 = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 12);
            com.itextpdf.text.Paragraph heading =
                    new com.itextpdf.text.Paragraph(new Phrase("\nReport Form\n\n", headingFont));
            heading.setAlignment(Element.ALIGN_CENTER);
            try {
                doc.add(heading);
            } catch (com.itextpdf.text.DocumentException e) {
                e.printStackTrace();
            }
            PdfPTable mainTable = new PdfPTable(2);
            mainTable.setTotalWidth(rect.getWidth() - 20);
            mainTable.setLockedWidth(true);

            PdfPCell cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            com.itextpdf.text.Paragraph paragraph = new com.itextpdf.text.Paragraph();
            PdfPTable table = new PdfPTable(1);
            table.setHorizontalAlignment(Element.ALIGN_LEFT);
            headerCell(table, getString(R.string.complaint_no), Element.ALIGN_LEFT, 1, bfBold12);
            headerCell(table, getString(R.string.what_did), Element.ALIGN_LEFT, 1, bfBold12);
            headerCell(table, getString(R.string.when_did), Element.ALIGN_LEFT, 1, bfBold12);
            paragraph.add(table);
            cell.addElement(paragraph);
            mainTable.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new com.itextpdf.text.Paragraph();
            table = new PdfPTable(1);
            table.setHorizontalAlignment(Element.ALIGN_LEFT);
            insertCell(table, complaintNo.getText().toString(), Element.ALIGN_LEFT, 1, bf12);
            insertCell(table, whatValue, Element.ALIGN_LEFT, 1, bf12);
            insertCell(table, whenValue, Element.ALIGN_LEFT, 1, bf12);
            paragraph.add(table);
            cell.addElement(paragraph);
            mainTable.addCell(cell);
            doc.add(mainTable);

            int fract = imageList.size() / 3;
            if (fract == 0) {
                doc.newPage();
                int remain = 3 - imageList.size();
                mainTable = new PdfPTable(3);
                mainTable.setTotalWidth(rect.getWidth() - 20);
                mainTable.setLockedWidth(true);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                if (imageList != null) {
                    for (String image : imageList) {
                        Image img = Image.getInstance(image.trim());
                        float scaler = ((doc.getPageSize().getWidth() - doc.leftMargin()
                                - doc.rightMargin()) / img.getWidth()) * 100;
                        img.setAbsolutePosition(10, 10);
                        img.scalePercent(scaler);
                        img.setAlignment(Element.ALIGN_CENTER);
                        cell = new PdfPCell(img, true);
                        cell.setPaddingLeft(10);
                        cell.setPaddingRight(10);
                        cell.setBorder(Rectangle.NO_BORDER);
                        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        cell.setColspan(1);
                        mainTable.addCell(cell);

                    }
                }

                for (int i = 0; i < remain; i++) {
                    cell = new PdfPCell();
                    cell.setBorder(Rectangle.NO_BORDER);
                    cell.addElement(new Paragraph(""));
                    mainTable.addCell(cell);
                }
                doc.add(mainTable);
                stream.close();
            } else {
                for (int i = 0; i < fract; i++) {
                    doc.newPage();
                    int remain = imageList.size() % 3;
                    mainTable = new PdfPTable(3);
                    mainTable.setTotalWidth(rect.getWidth() - 20);
                    mainTable.setLockedWidth(true);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    if (imageList != null) {
                        for (String image : imageList) {
                            Image img = Image.getInstance(image.trim());
                            float scaler = ((doc.getPageSize().getWidth() - doc.leftMargin()
                                    - doc.rightMargin()) / img.getWidth()) * 100;
                            img.setAbsolutePosition(10, 10);
                            img.scalePercent(scaler);
                            img.setAlignment(Element.ALIGN_CENTER);
                            cell = new PdfPCell(img, true);
                            cell.setPaddingLeft(10);
                            cell.setPaddingRight(10);
                            cell.setBorder(Rectangle.NO_BORDER);
                            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                            cell.setColspan(1);
                            mainTable.addCell(cell);

                        }
                    }
                    if (remain > 0)
                        for (i = 0; i < remain; i++) {
                            cell = new PdfPCell();
                            cell.setBorder(Rectangle.NO_BORDER);
                            cell.addElement(new Paragraph(""));
                            mainTable.addCell(cell);
                        }
                    doc.add(mainTable);
                    stream.close();
                }
            }

            doc.close();
        } catch (DocumentException ignored) {
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public void insertCell(PdfPTable table, String text, int align, int colspan, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase("  " + text.trim(), font));
        cell.setFixedHeight(40f);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setColspan(colspan);
        if (text.trim().equalsIgnoreCase("")) {
            cell.setMinimumHeight(10f);
        }
        table.addCell(cell);

    }

    public void headerCell(PdfPTable table, String text, int align, int colspan, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase("   " + text.trim(), font));
        cell.setFixedHeight(40f);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setColspan(colspan);
        if (text.trim().equalsIgnoreCase("")) {
            cell.setMinimumHeight(10f);
        }
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }


    private void assignAdapter() {
        toolbar.setTitle(getString(R.string.report_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        SharedPreferences sp = getSharedPreferences(getString(R.string.key_code), MODE_PRIVATE);
        int code = sp.getInt(getString(R.string.code), 0);
        if (code <= 0) {
            code = 1;
        } else {
            code++;
        }
        sp.edit().putInt(getString(R.string.code), code).commit();
        String newKey = "CDD" + code;
        complaintNo.setText(newKey);
        testList = new ArrayList<>();
        testList.add("test");
        testList.add("test");
        testList.add("test");
        testList.add("test");
        testList.add("test");
        ArrayAdapter<String> whatAdapter = new ArrayAdapter<String>(ReportFormActivity.this, R.layout.simple_spinner_item, testList);
        whatAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
        whatSpinner.setAdapter(whatAdapter);

        ArrayAdapter<String> whenAdapter = new ArrayAdapter<String>(ReportFormActivity.this, R.layout.simple_spinner_item, testList);
        whenAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
        whenSpinner.setAdapter(whenAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(ReportFormActivity.this, RecyclerView.HORIZONTAL,
                false);
        imageRecycler.setLayoutManager(layoutManager);
        attachmentAdapter = new AttachmentAdapter(ReportFormActivity.this, imageList);
        imageRecycler.setAdapter(attachmentAdapter);


    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(ReportFormActivity.this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String path = getPath(this, uri);
            imageList.add(path);
            attachmentAdapter.notifyDataSetChanged();

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static String getPath(Context context, Uri uri) {
        boolean isKitKatOrAbove;
        if (Build.VERSION.SDK_INT >= 19) {
            isKitKatOrAbove = true;
        } else {
            isKitKatOrAbove = false;
        }
        if (isKitKatOrAbove && DocumentsContract.isDocumentUri(context, uri)) {
            String[] split;
            if (isExternalStorageDocument(uri)) {
                split = DocumentsContract.getDocumentId(uri).split(":");
                if ("primary".equalsIgnoreCase(split[0])) {
                    return Environment.getExternalStorageDirectory() + "/" + split[REQUEST_IMAGE_CAPTURE];
                }
                return null;
            } else if (isDownloadsDocument(uri)) {
                return getDataColumn(context, ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(DocumentsContract.getDocumentId(uri)).longValue()), null, null);
            } else if (!isMediaDocument(uri)) {
                return null;
            } else {
                split = DocumentsContract.getDocumentId(uri).split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = "_id=?";
                String[] selectionArgs = new String[REQUEST_IMAGE_CAPTURE];
                selectionArgs[0] = split[REQUEST_IMAGE_CAPTURE];
                return getDataColumn(context, contentUri, "_id=?", selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        } else {
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
            return null;
        }
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = "_data";
        String[] projection = new String[REQUEST_IMAGE_CAPTURE];
        projection[0] = "_data";
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            String string = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
            return string;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private void initializeView() {
        complaintNo = (TextView) findViewById(R.id.complaint_no);
        whenSpinner = (Spinner) findViewById(R.id.whenSpinner);
        whatSpinner = (Spinner) findViewById(R.id.whatSpinner);
        attachLayout = (LinearLayout) findViewById(R.id.attachLayout);
        imageRecycler = (RecyclerView) findViewById(R.id.imageRecycler);
        cancelButton = (Button) findViewById(R.id.cancel);
        sendButton = (Button) findViewById(R.id.send);
        toolbar = (Toolbar) findViewById(R.id.toolBar);
    }
}
