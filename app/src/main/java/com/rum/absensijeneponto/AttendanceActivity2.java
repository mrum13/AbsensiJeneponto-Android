package com.rum.absensijeneponto;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.rum.absensijeneponto.api.RetrofitClient;
import com.rum.absensijeneponto.api.response.GetAbsenResponse;
import com.rum.absensijeneponto.api.response.GetUserResponse;
import com.rum.absensijeneponto.facemodul.SimilarityClassifier;
import com.rum.absensijeneponto.locationmodule.GpsTracker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceActivity2 extends AppCompatActivity {
    private String header, keteranganAbsen, idJenisAbsen, idUser, token, namaPegawai, waktuAbsenMasuk, waktuAbsenPulang, statusMasuk, statusPulang;
    private TextView tvHeader, timeDesc, tvLocationAttendance, tvNamePegawaiAttendance;
    private Date currentTime;
    private Calendar currentTimeDebug, currentTimeDebugMasuk;
    private Date cTime;
    private Date tm1;
    private Date tm2;
    private Date tm3;
    private Date pc3;
    private Date pc2;
    private Date pc1;
    private ImageView imgTime, imgCheckName, imgBack, imgCheckLocation, imgCheckKiri, imgCheckKanan;
    private CircularProgressIndicator circularProgressIndicator;

    private Calendar inLateEarly1;
    private Calendar inLateLast1;
    private Calendar inLateEarly2;
    private Calendar inLateLast2;
    private Calendar inLateEarly3;
    private Calendar inLateLast3;

    private Calendar outLateEarly1;
    private Calendar outLateLast1;
    private Calendar outLateEarly2;
    private Calendar outLateLast2;
    private Calendar outLateEarly3;
    private Calendar outLateLast3;

    private Button btnAbsen;
    private RadioGroup radioGroup;
    private RadioButton radioButton;

    private GpsTracker gpsTracker;
    private String latUser, longUser;
    private String latApi, longApi, namaLokasiKerja, latApelApi, longApelApi;

    SimpleDateFormat periodeFormat, tanggalFormat, waktuAbsenFormat;

    //module face recognition
    private Interpreter tfLite;
    private String modelFile="facenet.tflite";
    private FaceDetector detector;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewViewAttendance;
    private ProcessCameraProvider cameraProvider;
    private HashMap<String, SimilarityClassifier.Recognition> registered = new HashMap<>(); //saved Faces
    private int inputSize=160;
    private int[] intValues;
    boolean isModelQuantized=false;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private float[][] embeddings;
    private float IMAGE_MEAN = 128.0f;
    private float IMAGE_STD = 128.0f;
    private int OUTPUT_SIZE=512;
    private CameraSelector cameraSelector;
    int cam_face=CameraSelector.LENS_FACING_FRONT;
    boolean start=true,flipX=false;
    private String nameUser;
    private String faceChar;
    private boolean kiri = false, kanan = false, firstValidationFace = false, secondValidationFace = false;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance2);
        initComponent();

        btnAbsen.setText("Kedipkan mata perlahan untuk absen");

        token = Preferences.getToken(AttendanceActivity2.this);

        header = getIntent().getStringExtra("key_header");
        tvHeader.setText(header);

        String patternPeriode, patternTanggal, patternWaktuAbsen;

        patternPeriode = "MMMM-yyyy";
        patternTanggal = "yyyy-MM-dd";
        patternWaktuAbsen = "HH:mm";

        periodeFormat = new SimpleDateFormat(patternPeriode);
        tanggalFormat = new SimpleDateFormat(patternTanggal);
        waktuAbsenFormat = new SimpleDateFormat(patternWaktuAbsen);

        disableButton();

        hideCircularProgress();

        //default value radio
        int selectedID = radioGroup.getCheckedRadioButtonId();
        radioButton = (RadioButton) findViewById(selectedID);
        if (radioButton.getText().equals("Hadir")){
            idJenisAbsen = "3";
            keteranganAbsen = "Hadir - hadir";
        }

        cameraPermission();
        getCurrentLocation();
        getUser();
        getTimeFunction();
        getWaktuMasukdanKeluar();

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AttendanceActivity2.this, AttendanceActivity.class));
            }
        });

        btnAbsen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAllDataAndPostData();
            }
        });
    }

    void initComponent() {
        imgBack = findViewById(R.id.img_back);
        tvHeader = findViewById(R.id.tv_header);
        timeDesc = findViewById(R.id.tv_time_attendance);
        tvLocationAttendance = findViewById(R.id.tv_location_attendance);
        imgTime = findViewById(R.id.img_check_time);
        imgCheckLocation = findViewById(R.id.img_check_location);
        circularProgressIndicator = findViewById(R.id.circular_progress);
        btnAbsen = findViewById(R.id.btn_attendance);
        radioGroup = findViewById(R.id.radio_group1);
        tvNamePegawaiAttendance = findViewById(R.id.tv_name_pegawai_attendance);
        imgCheckName = findViewById(R.id.img_check_name);
        imgCheckKiri = findViewById(R.id.img_check_kiri);
        imgCheckKanan = findViewById(R.id.img_check_kanan);

        previewViewAttendance=findViewById(R.id.camera_selfie);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void enableButton() {
        btnAbsen.setEnabled(true);
        btnAbsen.setTextColor(getColor(R.color.white));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void disableButton() {
        btnAbsen.setEnabled(false);
        btnAbsen.setTextColor(getColor(R.color.black_text));
    }

    private void cameraBind() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);


        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this in Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    //Step Initial Default Camera :
    private MappedByteBuffer loadModelFile(Activity activity, String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private float[][] convertStringToFloat2(String facenya2){
        String[] rowWithoutSplit = facenya2.replace("[","").split("]");
        int width = rowWithoutSplit.length;
        String cells[] = rowWithoutSplit[0].split(",");
        int height = cells.length;
        float[][] output = new float[width][height];

        for (int i=0; i<width; i++) {
            String cells2[] = rowWithoutSplit[i].split(",");
            for(int j=0; j<height; j++) {
                output[i][j] = Float.parseFloat(cells2[j]);
            }
        }

        return output;
    }

    private static byte[] YUV_420_888toNV21(Image image) {

        int width = image.getWidth();
        int height = image.getHeight();
        int ySize = width*height;
        int uvSize = width*height/4;

        byte[] nv21 = new byte[ySize + uvSize*2];

        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer(); // Y
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer(); // U
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer(); // V

        int rowStride = image.getPlanes()[0].getRowStride();
        assert(image.getPlanes()[0].getPixelStride() == 1);

        int pos = 0;

        if (rowStride == width) { // likely
            yBuffer.get(nv21, 0, ySize);
            pos += ySize;
        }
        else {
            long yBufferPos = -rowStride; // not an actual position
            for (; pos<ySize; pos+=width) {
                yBufferPos += rowStride;
                yBuffer.position((int) yBufferPos);
                yBuffer.get(nv21, pos, width);
            }
        }

        rowStride = image.getPlanes()[2].getRowStride();
        int pixelStride = image.getPlanes()[2].getPixelStride();

        assert(rowStride == image.getPlanes()[1].getRowStride());
        assert(pixelStride == image.getPlanes()[1].getPixelStride());

        if (pixelStride == 2 && rowStride == width && uBuffer.get(0) == vBuffer.get(1)) {
            // maybe V an U planes overlap as per NV21, which means vBuffer[1] is alias of uBuffer[0]
            byte savePixel = vBuffer.get(1);
            try {
                vBuffer.put(1, (byte)~savePixel);
                if (uBuffer.get(0) == (byte)~savePixel) {
                    vBuffer.put(1, savePixel);
                    vBuffer.position(0);
                    uBuffer.position(0);
                    vBuffer.get(nv21, ySize, 1);
                    uBuffer.get(nv21, ySize + 1, uBuffer.remaining());

                    return nv21; // shortcut
                }
            }
            catch (ReadOnlyBufferException ex) {
                // unfortunately, we cannot check if vBuffer and uBuffer overlap
            }

            // unfortunately, the check failed. We must save U and V pixel by pixel
            vBuffer.put(1, savePixel);
        }

        // other optimizations could check if (pixelStride == 1) or (pixelStride == 2),
        // but performance gain would be less significant

        for (int row=0; row<height/2; row++) {
            for (int col=0; col<width/2; col++) {
                int vuPos = col*pixelStride + row*rowStride;
                nv21[pos++] = vBuffer.get(vuPos);
                nv21[pos++] = uBuffer.get(vuPos);
            }
        }

        return nv21;
    }

    //looping face with realtime
    private Pair<String, Float> findNearest(float[] emb) {

        Pair<String, Float> ret = null;
        for (Map.Entry<String, SimilarityClassifier.Recognition> entry : registered.entrySet()) {

            final String name = entry.getKey();
            final float[] knownEmb = ((float[][]) entry.getValue().getExtra())[0];

            float distance = 0;
            for (int i = 0; i < emb.length; i++) {
                float diff = emb[i] - knownEmb[i];
                distance += diff*diff;
            }
            distance = (float) Math.sqrt(distance);
            if (ret == null || distance < ret.second) {
                ret = new Pair<>(name, distance);
            }
        }

        return ret;

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void recognizeImage(final Bitmap bitmap) {

        ByteBuffer imgData = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4);

        imgData.order(ByteOrder.nativeOrder());

        intValues = new int[inputSize * inputSize];

        //get pixel values from Bitmap to normalize
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        imgData.rewind();

        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                int pixelValue = intValues[i * inputSize + j];
                if (isModelQuantized) {
                    // Quantized model
                    imgData.put((byte) ((pixelValue >> 16) & 0xFF));
                    imgData.put((byte) ((pixelValue >> 8) & 0xFF));
                    imgData.put((byte) (pixelValue & 0xFF));
                } else { // Float model
                    imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);

                }
            }
        }
        //imgData is input to our model
        Object[] inputArray = {imgData};

        Map<Integer, Object> outputMap = new HashMap<>();

        embeddings = new float[1][OUTPUT_SIZE]; //output of model will be stored in this variable

        outputMap.put(0, embeddings);

        tfLite.runForMultipleInputsOutputs(inputArray, outputMap); //Run model

        float distance = Float.MAX_VALUE;
        String id = "0";
        String label = "?";

        //Compare new face with saved Faces.
        if (registered.size() > 0) {
            final Pair<String, Float> nearest = findNearest(embeddings[0]);//Find closest matching face

            if (nearest != null) {
                final String name = nearest.first;
                distance = nearest.second;

                Log.d("AccurateFace", "Loss Value: "+distance);
                Log.d("AccurateFace", "Name Value: "+name);
                Log.d("NearestFace", "Nearest Face Value: "+nearest);

                if(distance<=0.950f) //If distance between Closest found face is more than 0.900 ,then output UNKNOWN face.
                {
                    firstValidationFace = true;
                    tvNamePegawaiAttendance.setText(namaPegawai);
                    imgCheckName.setImageResource(R.drawable.ic_check);

                    Log.d("FirstValidation", "First Validation Face: "+firstValidationFace);
                    if((idJenisAbsen.equals("3") || idJenisAbsen.equals("8") || idJenisAbsen.equals("7")) && (tvLocationAttendance.getText().equals("Anda telah di area kerja")) && (kiri==true) && (kanan==true)) {
                        enableButton();
                    } else if((tvLocationAttendance.getText().equals("Anda telah di area kerja")) && (idJenisAbsen.equals("10") || idJenisAbsen.equals("5"))) {
                        disableButton();
                    } else if((tvLocationAttendance.getText().equals("Anda di luar area kerja")) && (idJenisAbsen.equals("10") || idJenisAbsen.equals("5"))) {
                        enableButton();
                    } else if((tvLocationAttendance.getText().equals("Anda di luar area kerja")) && (idJenisAbsen.equals("3") || idJenisAbsen.equals("8") || idJenisAbsen.equals("7"))) {
                        disableButton();
                    } else {
                        imgCheckName.setImageResource(R.drawable.ic_failed);
                        tvNamePegawaiAttendance.setText(namaPegawai);
                        disableButton();
                    }
                } else {
                    imgCheckName.setImageResource(R.drawable.ic_failed);
                    tvNamePegawaiAttendance.setText("Wajah tidak dikenal");
                    disableButton();


                }
                System.out.println("nearest: " + name + " - distance: " + distance);
            } else {
                Log.d("NearestFace", "Nearest Face Value: "+nearest);
            }
        }
    }

    private Bitmap toBitmap(Image image) {

        byte[] nv21=YUV_420_888toNV21(image);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);

        byte[] imageBytes = out.toByteArray();

        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    private static Bitmap rotateBitmap(
            Bitmap bitmap, int rotationDegrees, boolean flipX, boolean flipY) {
        Matrix matrix = new Matrix();

        // Rotate the image back to straight.
        matrix.postRotate(rotationDegrees);

        // Mirror the image along the X or Y axis.
        matrix.postScale(flipX ? -1.0f : 1.0f, flipY ? -1.0f : 1.0f);
        Bitmap rotatedBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        // Recycle the old bitmap if it has changed.
        if (rotatedBitmap != bitmap) {
            bitmap.recycle();
        }
        return rotatedBitmap;
    }

    private static Bitmap getCropBitmapByCPU(Bitmap source, RectF cropRectF) {
        Bitmap resultBitmap = Bitmap.createBitmap((int) cropRectF.width(),
                (int) cropRectF.height(), Bitmap.Config.ARGB_8888);
        Canvas cavas = new Canvas(resultBitmap);

        // draw background
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setColor(Color.WHITE);
        cavas.drawRect(//from  w w  w. ja v  a  2s. c  om
                new RectF(0, 0, cropRectF.width(), cropRectF.height()),
                paint);

        Matrix matrix = new Matrix();
        matrix.postTranslate(-cropRectF.left, -cropRectF.top);

        cavas.drawBitmap(source, matrix, paint);

        if (source != null && !source.isRecycled()) {
            source.recycle();
        }

        return resultBitmap;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();

        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(cam_face)
                .build();

        preview.setSurfaceProvider(previewViewAttendance.getSurfaceProvider());
        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(640, 480))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) //Latest frame is shown
                        .build();

        Executor executor = Executors.newSingleThreadExecutor();
        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {

                InputImage image = null;

                @SuppressLint({"UnsafeExperimentalUsageError", "UnsafeOptInUsageError"})
                // Camera Feed-->Analyzer-->ImageProxy-->mediaImage-->InputImage(needed for ML kit face detection)

                Image mediaImage = imageProxy.getImage();

                if (mediaImage != null) {
                    image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                    System.out.println("Rotation "+imageProxy.getImageInfo().getRotationDegrees());
                }


                //Process acquired image to detect faces
                Task<List<Face>> result =
                        detector.process(image)
                                .addOnSuccessListener(
                                        new OnSuccessListener<List<Face>>() {
                                            @RequiresApi(api = Build.VERSION_CODES.M)
                                            @Override
                                            public void onSuccess(List<Face> faces) {

                                                if(faces.size()!=0) {
                                                    Face face = faces.get(0); //Get first face from detected faces

                                                    //mediaImage to Bitmap
                                                    Bitmap frame_bmp = toBitmap(mediaImage);

                                                    int rot = imageProxy.getImageInfo().getRotationDegrees();

                                                    //Adjust orientation of Face
                                                    Bitmap frame_bmp1 = rotateBitmap(frame_bmp, rot, false, false);

                                                    //Get bounding box of face
                                                    RectF boundingBox = new RectF(face.getBoundingBox());

                                                    float rotY = face.getHeadEulerAngleY();
//                                                    float smileDetect = face.getSmilingProbability();
                                                    float leftEye = face.getLeftEyeOpenProbability();
                                                    float rightEye = face.getRightEyeOpenProbability();

                                                    Log.d("Eye", "Eye Value Left: "+leftEye+" | Eye Value Right: "+rightEye);

                                                    if (rotY>=25.0) { //wajah menghadap kiri
                                                        if (!firstValidationFace) {
                                                            Toast.makeText(AttendanceActivity2.this, "Wajah kiri tidak sesuai", Toast.LENGTH_LONG).show();
                                                            kiri=false;
                                                        }else {
                                                            kiri=true;
                                                        }

                                                    }
                                                    if (rotY<=-25.0) { //wajah menghadap kanan
                                                        if (!firstValidationFace) {
                                                            Toast.makeText(AttendanceActivity2.this, "Wajah kanan tidak sesuai", Toast.LENGTH_LONG).show();
                                                            kanan=false;
                                                        }else {
                                                            kanan=true;
                                                        }
                                                    }

                                                    if (kiri==true) {
                                                        imgCheckKiri.setImageResource(R.drawable.ic_check);
                                                    } else {
                                                        imgCheckKiri.setImageResource(R.drawable.ic_failed);
                                                    }

                                                    if (kanan==true) {
                                                        imgCheckKanan.setImageResource(R.drawable.ic_check);
                                                    } else {
                                                        imgCheckKanan.setImageResource(R.drawable.ic_failed);
                                                    }

                                                    if (btnAbsen.isEnabled()) {
                                                        if(leftEye<0.1 || leftEye<0.1) {
//                                                            btnAbsen.performClick();
                                                            getAllDataAndPostData();
                                                        }
                                                    }

                                                    Log.d("RotateHead", "Hadap Kiri atau kanan: "+rotY);

                                                    //Crop out bounding box from whole Bitmap(image)
                                                    Bitmap cropped_face = getCropBitmapByCPU(frame_bmp1, boundingBox);

                                                    if(flipX)
                                                        cropped_face = rotateBitmap(cropped_face, 0, flipX, false);
                                                    //Scale the acquired Face to 112*112 which is required input for model
                                                    Bitmap scaled = getResizedBitmap(cropped_face, 160, 160);

                                                    if(start) //start==true (if true, start to recognize image)
                                                        recognizeImage(scaled); //Send scaled bitmap to create face embeddings.
                                                    System.out.println(boundingBox);
                                                    try {
                                                        Thread.sleep(10);  //Camera preview refreshed every 10 millisec(adjust as required)
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                                else
                                                {
                                                    imgCheckName.setImageResource(R.drawable.ic_failed);
                                                    tvNamePegawaiAttendance.setText("Arahkan Wajah ke kamera");
                                                    disableButton();
                                                }
                                            }
                                        })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Task failed with an exception
                                                // ...
                                                System.out.print("tidak ada wajah");
                                            }
                                        })
                                .addOnCompleteListener(new OnCompleteListener<List<Face>>() {
                                    @Override
                                    public void onComplete(@NonNull Task<List<Face>> task) {

                                        imageProxy.close(); //v.important to acquire next frame for analysis
                                    }
                                });


            }
        });
        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageAnalysis, preview);
    }

    void faceRecognitionModuleInit() {

        SimilarityClassifier.Recognition result = new SimilarityClassifier.Recognition(
                "0", "", -1f);
        result.setExtra(convertStringToFloat2(faceChar));

        registered.put(namaPegawai,result);

        //Load model
        try {
            tfLite=new Interpreter(loadModelFile(AttendanceActivity2.this,modelFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Initialize Face Detector
        FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .build();
        detector = FaceDetection.getClient(highAccuracyOpts);
        cameraBind();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void cameraPermission() {
        //CAMERA PERMISSION
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }
    }

    private void compareLocation(String latApiStr, String longApiStr, String latUserStr, String longUserStr ) {
        double latApiDbl = Double.parseDouble(latApiStr);
        double longApiDbl = Double.parseDouble(longApiStr);

        double latUserDbl = Double.parseDouble(latUserStr);
        double longUserDbl = Double.parseDouble(longUserStr);

        //0.2 = miles = 322 meter
        if(distance(latApiDbl,longApiDbl,latUserDbl,longUserDbl) < 0.1) {
            tvLocationAttendance.setText("Anda telah di area kerja");
            imgCheckLocation.setImageResource(R.drawable.ic_check);

            faceRecognitionModuleInit();
        } else{
            tvLocationAttendance.setText("Anda di luar area kerja");
            imgCheckLocation.setImageResource(R.drawable.ic_failed);

            faceRecognitionModuleInit();
        }
    }

    private double distance(double lat1, double lng1, double lat2, double lng2) {

        double earthRadius = 3958.75; // in miles, change to 6371 for kilometer output

        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);

        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);

        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double dist = earthRadius * c;

        Log.d("locationUser", "distance: "+String.valueOf(dist));

        return dist; // output distance, in MILES
    }

    private void getCurrentLocation() {
        gpsTracker = new GpsTracker(AttendanceActivity2.this);
        if(gpsTracker.canGetLocation()){
            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();
            latUser = String.valueOf(latitude);
            longUser = String.valueOf(longitude);
        }else{
            gpsTracker.showSettingsAlert();
        }
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_presents:
                if (checked) {
                    if(timeDesc.getText().equals("Tepat Waktu")) {
                        keteranganAbsen = "Hadir - hadir";
                        idJenisAbsen = "3";
                    } else if (timeDesc.getText().equals("Terlambat 1") || timeDesc.getText().equals("Terlambat 2") || timeDesc.getText().equals("Terlambat 3")) {
                        keteranganAbsen = "Hadir - Telat Masuk";
                        idJenisAbsen = "8";
                    } else if (timeDesc.getText().equals("Pulang Cepat 1") || timeDesc.getText().equals("Pulang Cepat 2") || timeDesc.getText().equals("Pulang Cepat 3")) {
                        keteranganAbsen = "Hadir - Cepat Pulang";
                        idJenisAbsen = "7";
                    }
                }
                break;
            case R.id.radio_permission:
                if (checked){
                    keteranganAbsen = "Tidak Hadir - Izin";
                    idJenisAbsen = "10";
                }
                break;
            case R.id.radio_sick:
                if (checked) {
                    keteranganAbsen = "Tidak Hadir - Sakit";
                    idJenisAbsen = "5";
                }
                break;
        }
    }

    void getUser() {
        Call<List<GetUserResponse>> call= RetrofitClient.getApi().getUser("Bearer "+token);

        call.enqueue(new Callback<List<GetUserResponse>>() {
            @Override
            public void onResponse(Call<List<GetUserResponse>> call, Response<List<GetUserResponse>> response) {
                if (response.isSuccessful()) {
                    List<GetUserResponse> data = response.body();

                    String latApi, longApi;

                    Log.d("face_char", "getUserIDName: "+data.get(0).getPegawai().getFace_character());

                    getUserIDName(data.get(0).getPegawai().getUserId(), data.get(0).getName(), data.get(0).getPegawai().getFace_character());


                    latApi = data.get(0).getPegawai().getLokasi().getLatLocation();
                    longApi = data.get(0).getPegawai().getLokasi().getLongLocation();



                    compareLocation(latApi, longApi, latUser, longUser);
                }
            }

            @Override
            public void onFailure(Call<List<GetUserResponse>> call, Throwable t) {
                Toast.makeText(AttendanceActivity2.this, t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    void getUserIDName(String id, String name, String face) {
        idUser = id;
        namaPegawai = name;
        faceChar = face;


    }

    void getTimeFunction() {


        tm1 = new Date();
        tm2 = new Date();
        tm3 = new Date();
        pc3 = new Date();
        pc2 = new Date();
        pc1 = new Date();


        inLateEarly1 = Calendar.getInstance();
        inLateLast1 = Calendar.getInstance();
        inLateEarly2 = Calendar.getInstance();
        inLateLast2 = Calendar.getInstance();
        inLateEarly3 = Calendar.getInstance();
        inLateLast3 = Calendar.getInstance();

        outLateEarly1 = Calendar.getInstance();
        outLateLast1 = Calendar.getInstance();
        outLateEarly2 = Calendar.getInstance();
        outLateLast2 = Calendar.getInstance();
        outLateEarly3 = Calendar.getInstance();
        outLateLast3 = Calendar.getInstance();

        cTime = new Date();

        currentTimeDebug =  Calendar.getInstance();
        //NOTED: ACTIVATED ONLY ON DEBUG MODE CODE BELOW to set time
//        currentTimeDebug.setTime(cTime);
//        currentTimeDebug.set(Calendar.HOUR_OF_DAY, 16);
//        currentTimeDebug.set(Calendar.MINUTE,30);
//        currentTimeDebug.set(Calendar.SECOND,0);

        currentTimeDebugMasuk =  Calendar.getInstance();
        //NOTED: ACTIVATED ONLY ON DEBUG MODE CODE BELOW to set time
//        currentTimeDebugMasuk.setTime(cTime);
//        currentTimeDebugMasuk.set(Calendar.HOUR_OF_DAY, 7);
//        currentTimeDebugMasuk.set(Calendar.MINUTE,30);
//        currentTimeDebugMasuk.set(Calendar.SECOND,0);

        batasAbsenMasuk();
        batasAbsenPulang();

        if (currentTimeDebug.getTime().before(inLateEarly1.getTime()) || currentTimeDebug.getTime().equals(inLateEarly1.getTime())) {
            timeDesc.setText("Tepat Waktu");
            imgTime.setImageResource(R.drawable.ic_check);
        } else if(currentTimeDebug.getTime().after(inLateEarly1.getTime()) && currentTimeDebug.getTime().before(inLateLast1.getTime())) {
            timeDesc.setText("Terlambat 1");
            imgTime.setImageResource(R.drawable.ic_failed);
        } else if(currentTimeDebug.getTime().equals(inLateEarly2.getTime()) || currentTimeDebug.getTime().after(inLateEarly2.getTime()) && currentTimeDebug.getTime().before(inLateLast2.getTime())) {
            timeDesc.setText("Terlambat 2");
            imgTime.setImageResource(R.drawable.ic_failed);
        }  else if(currentTimeDebug.getTime().equals(inLateEarly3.getTime()) || currentTimeDebug.getTime().after(inLateEarly3.getTime()) && currentTimeDebug.getTime().before(inLateLast3.getTime())) {
            timeDesc.setText("Terlambat 3");
            imgTime.setImageResource(R.drawable.ic_failed);
        }  else if(currentTimeDebug.getTime().equals(outLateEarly1.getTime()) || currentTimeDebug.getTime().after(outLateEarly1.getTime()) && currentTimeDebug.getTime().before(outLateLast1.getTime())) {
            timeDesc.setText("Pulang Cepat 1");
            imgTime.setImageResource(R.drawable.ic_failed);
        } else if(currentTimeDebug.getTime().equals(outLateEarly2.getTime()) || currentTimeDebug.getTime().after(outLateEarly2.getTime()) && currentTimeDebug.getTime().before(outLateLast2.getTime())) {
            timeDesc.setText("Pulang Cepat 2");
            imgTime.setImageResource(R.drawable.ic_failed);
        }  else if(currentTimeDebug.getTime().equals(outLateEarly3.getTime()) || currentTimeDebug.getTime().after(outLateEarly3.getTime()) && currentTimeDebug.getTime().before(outLateLast3.getTime())) {
            timeDesc.setText("Pulang Cepat 3");
            imgTime.setImageResource(R.drawable.ic_failed);
        } else if (currentTimeDebug.getTime().after(outLateLast1.getTime()) || currentTimeDebug.getTime().equals(outLateLast1.getTime())) {
            timeDesc.setText("Tepat Waktu");
            imgTime.setImageResource(R.drawable.ic_check);
        }
    }

    void getWaktuMasukdanKeluar(){
        if (tvHeader.getText().equals("Absen Masuk")) {
            waktuAbsenMasuk = waktuAbsenFormat.format(currentTimeDebugMasuk.getTime());
            waktuAbsenPulang = "00:00";
            statusMasuk = "1";
            statusPulang = "0";
        } else if (tvHeader.getText().equals("Absen Pulang")) {
            waktuAbsenMasuk = Preferences.getWaktuMasuk(AttendanceActivity2.this);
            waktuAbsenPulang = waktuAbsenFormat.format(currentTimeDebug.getTime());
            statusMasuk = "1";
            statusPulang = "1";
        }
    }

    void getAllDataAndPostData() {
        showCircularProgress();

        String getPeriodeAbsen, getTanggalAbsen, getIdJenisAbsen, getKeteranganAbsen, getPegawaiId, getWaktuMasuk, getWaktuPulang, getFaceUser, getStatusMasuk, getStatusKeluar;

        getPeriodeAbsen = periodeFormat.format(new Date()).toLowerCase(Locale.ROOT);
        getTanggalAbsen = tanggalFormat.format(new Date());
        getIdJenisAbsen = idJenisAbsen;
        getKeteranganAbsen = keteranganAbsen;
        getPegawaiId = idUser;
        getWaktuMasuk = waktuAbsenMasuk;
        getWaktuPulang = waktuAbsenPulang;
        getStatusMasuk = statusMasuk;
        getStatusKeluar = statusPulang;
        getFaceUser = "valid";

        Log.d("GetAllData", getPegawaiId+" "+getPeriodeAbsen+" "+getTanggalAbsen+" "+getIdJenisAbsen+" "+getWaktuMasuk+" "+getWaktuPulang+" "+getFaceUser+" "+getStatusMasuk+" "+getStatusKeluar);

        postAbsen(getPegawaiId, getPeriodeAbsen, getTanggalAbsen, getIdJenisAbsen, getWaktuMasuk, getWaktuPulang, getKeteranganAbsen, getFaceUser, getStatusMasuk, getStatusKeluar);
    }

    void postAbsen(String pegawaiId, String periode, String tanggalAbsen, String attendanceId, String waktuMasuk, String waktuKeluar, String keterangan, String faceAuth, String statusMasuk, String statusKeluar) {
        Call<List<GetAbsenResponse>> call= RetrofitClient.getApi().getAbsenResponse("Bearer "+token, pegawaiId, periode, tanggalAbsen, attendanceId, waktuMasuk, waktuKeluar, keterangan, faceAuth, statusMasuk, statusKeluar);

        call.enqueue(new Callback<List<GetAbsenResponse>>() {
            @Override
            public void onResponse(Call<List<GetAbsenResponse>> call, Response<List<GetAbsenResponse>> response) {

                if(response.isSuccessful()) {

                    updateStatusAbsenPegawai();

                    hideCircularProgress();
                    List<GetAbsenResponse> data = response.body();

                    Preferences.setWaktuMasuk(getApplicationContext(),data.get(0).getWaktuMasuk());

                    Toast.makeText(AttendanceActivity2.this, "Absen berhasil direkam", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AttendanceActivity2.this, MainActivity.class);
                    intent.putExtra("from_where", "fromLogin");
                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<List<GetAbsenResponse>> call, Throwable t) {
                hideCircularProgress();
                Toast.makeText(AttendanceActivity2.this, "Gagal Absen karena"+t.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    void updateStatusAbsenPegawai() {
        Call<List<Object>> updateAbsen= RetrofitClient.getApi().updateStatusAbsen("Bearer "+token, statusMasuk, statusPulang);

        updateAbsen.enqueue(new Callback<List<Object>>() {
            @Override
            public void onResponse(Call<List<Object>> call, Response<List<Object>> response) {

            }

            @Override
            public void onFailure(Call<List<Object>> call, Throwable t) {
                Toast.makeText(AttendanceActivity2.this, t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    void batasAbsenMasuk() {
        inLateEarly1.setTime(tm1);
        inLateEarly1.set(Calendar.HOUR_OF_DAY, 8);
        inLateEarly1.set(Calendar.MINUTE,00);
        inLateEarly1.set(Calendar.SECOND,01);

        inLateLast1.setTime(tm1);
        inLateLast1.set(Calendar.HOUR_OF_DAY, 8);
        inLateLast1.set(Calendar.MINUTE,31);
        inLateLast1.set(Calendar.SECOND,00);

        inLateEarly2.setTime(tm2);
        inLateEarly2.set(Calendar.HOUR_OF_DAY, 8);
        inLateEarly2.set(Calendar.MINUTE,31);
        inLateEarly2.set(Calendar.SECOND,0);

        inLateLast2.setTime(tm2);
        inLateLast2.set(Calendar.HOUR_OF_DAY, 9);
        inLateLast2.set(Calendar.MINUTE,00);
        inLateLast2.set(Calendar.SECOND,00);

        inLateEarly3.setTime(tm3);
        inLateEarly3.set(Calendar.HOUR_OF_DAY, 9);
        inLateEarly3.set(Calendar.MINUTE,1);
        inLateEarly3.set(Calendar.SECOND,0);

        inLateLast3.setTime(tm3);
        inLateLast3.set(Calendar.HOUR_OF_DAY, 10);
        inLateLast3.set(Calendar.MINUTE,01);
        inLateLast3.set(Calendar.SECOND,00);
    }

    void batasAbsenPulang() {
        outLateEarly1.setTime(pc1);
        outLateEarly1.set(Calendar.HOUR_OF_DAY, 15);
        outLateEarly1.set(Calendar.MINUTE,30);
        outLateEarly1.set(Calendar.SECOND,00);

        outLateLast1.setTime(pc1);
        outLateLast1.set(Calendar.HOUR_OF_DAY, 16);
        outLateLast1.set(Calendar.MINUTE,00);
        outLateLast1.set(Calendar.SECOND,00);

        outLateEarly2.setTime(pc2);
        outLateEarly2.set(Calendar.HOUR_OF_DAY, 15);
        outLateEarly2.set(Calendar.MINUTE,00);
        outLateEarly2.set(Calendar.SECOND,0);

        outLateLast2.setTime(pc2);
        outLateLast2.set(Calendar.HOUR_OF_DAY, 15);
        outLateLast2.set(Calendar.MINUTE,30);
        outLateLast2.set(Calendar.SECOND,00);

        outLateEarly3.setTime(pc3);
        outLateEarly3.set(Calendar.HOUR_OF_DAY, 14);
        outLateEarly3.set(Calendar.MINUTE,0);
        outLateEarly3.set(Calendar.SECOND,0);

        outLateLast3.setTime(pc3);
        outLateLast3.set(Calendar.HOUR_OF_DAY, 15);
        outLateLast3.set(Calendar.MINUTE,00);
        outLateLast3.set(Calendar.SECOND,00);
    }

    void showCircularProgress() {
        circularProgressIndicator.setVisibility(View.VISIBLE);
        btnAbsen.setVisibility(View.GONE);
    }

    void hideCircularProgress() {
        circularProgressIndicator.setVisibility(View.GONE);
        btnAbsen.setVisibility(View.VISIBLE);
    }
}