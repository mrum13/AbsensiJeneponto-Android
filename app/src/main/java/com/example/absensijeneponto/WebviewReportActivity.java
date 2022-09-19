package com.example.absensijeneponto;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class WebviewReportActivity extends AppCompatActivity {
    private String firstDate, lastDate;
    private int idUser;
    private ImageView btnBack;
    private WebView webView;
    private Button btnDownload;
    private URL url;
    private DownloadManager manager;
    private CircularProgressIndicator circularProgressIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_report);

        btnBack = findViewById(R.id.ic_back);
        webView = findViewById(R.id.web_view);
        btnDownload = findViewById(R.id.btn_download_pdf);
        circularProgressIndicator = findViewById(R.id.circular_progress);

        firstDate = getIntent().getStringExtra("first_date");
        lastDate = getIntent().getStringExtra("last_date");
        idUser = getIntent().getIntExtra("idUser", 0);

        if (firstDate == null || lastDate == null || idUser == 0) {
            Toast.makeText(this, "Gagal get data Tanggal atau Id User", Toast.LENGTH_SHORT).show();
        } else {
            circularProgressIndicator.setVisibility(View.GONE);
            webView.loadUrl("https://www.absensi-dinkes.my.id/absensi/detail/start="+firstDate+"/end="+lastDate+"/user_id="+idUser);
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

//        btnDownload.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
//                    ActivityCompat.requestPermissions(PdfViewerReportActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
//                    Toast.makeText(PdfViewerReportActivity.this, "Mohon izinkan akses penyimpanan !", Toast.LENGTH_SHORT).show();
//                } else {
//                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse("https://dikerja.bulukumbakab.go.id/laporan/viewexport/rekapitulasi_pegawai/"+firstDate+"/"+lastDate+"/"+idUser+".pdf" + ""));
//                    request.setTitle("laporan_absen_dikerja");
//                    request.setMimeType("applcation/pdf");
//                    request.allowScanningByMediaScanner();
//                    request.setAllowedOverMetered(true);
//                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "laporan_absen_dikerja");
//                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
//                    dm.enqueue(request);
//                    Toast.makeText(PdfViewerReportActivity.this, "Laporan telah diunduh, silahkan cek file manager", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

    }

    private void DownloadFunction() {
        manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
//        Uri uri = Uri.parse("https://dikerja.bulukumbakab.go.id/laporan/viewexport/rekapitulasi_pegawai/"+firstDate+"/"+lastDate+"/"+idUser+".pdf");
        Uri uri = Uri.parse("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf");
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        long reference = manager.enqueue(request);
    }

    // create an async task class for loading pdf file from URL.
    class RetrivePDFfromUrl extends AsyncTask<String, Void, InputStream> {
        @Override
        protected InputStream doInBackground(String... strings) {
            // we are using inputstream
            // for getting out PDF.
            InputStream inputStream = null;
            try {
                URL url = new URL(strings[0]);
                // below is the step where we are
                // creating our connection.
                HttpURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                if (urlConnection.getResponseCode() == 200) {
                    // response is success.
                    // we are getting input stream from url
                    // and storing it in our variable.
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                }

            } catch (IOException e) {
                // this is the method
                // to handle errors.
                e.printStackTrace();
                return null;
            }
            return inputStream;
        }

//        @Override
//        protected void onPostExecute(InputStream inputStream) {
//            // after the execution of our async
//            // task we are loading our pdf in our pdf view.
//            webView.fromStream(inputStream).load();
//            circularProgressIndicator.setVisibility(View.GONE);
//        }
    }
}