package com.example.kechaval.mycamera;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.net.URI;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class CameraV0Activity extends AppCompatActivity {

    private String CARPETA_RAIZ= "misImagenesCamera/";
    private String RUTA_IMAGEN =CARPETA_RAIZ+ "misFotos";
    private String path;
    private final static int COD_SELECT=10;
    private final static int COD_PHOTO=20;
    private static int COP_CAMERA_SD_PERMISSIONS=100;
    private ImageView imageView;
    Button btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_v0);
        imageView = findViewById(R.id.ivPhoto);
        btn = findViewById(R.id.btnCapture);

        if(validarPermisos()){
            btn.setEnabled(true);
        }else {
            btn.setEnabled(false);
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cargarImagen();
            }
        });
    }

    private boolean validarPermisos() {

        //validamos si nuestro dispositivo es menor a M
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.M){
            return true;
        }

        if ((checkSelfPermission(CAMERA)== PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED)){
            return true;
        }
        if ((shouldShowRequestPermissionRationale(CAMERA))||
                (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE))){
            cargarDialogPermisos();
        }else {
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,CAMERA},COP_CAMERA_SD_PERMISSIONS);
        }


        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode==COP_CAMERA_SD_PERMISSIONS){
            if (grantResults.length==2 && grantResults[0]==PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                btn.setEnabled(true);
            }else {
                solicitarPermisosManual();
            }
        }
    }

    private void solicitarPermisosManual() {
        final CharSequence[] opciones ={"Si","No" };
        AlertDialog.Builder dialogBuilder= new AlertDialog.Builder(CameraV0Activity.this);
        dialogBuilder.setTitle("Configurar los permisos de forma manual?");
        dialogBuilder.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (opciones[which].equals("Si")){
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package",getPackageName(),null);
                    intent.setData(uri);
                    startActivity(intent);
                }else {
                    Toast.makeText(getApplicationContext(), "Los Permisos no fueron aceptados por el usuario. ", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });

        dialogBuilder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,CAMERA},COP_CAMERA_SD_PERMISSIONS);
            }
        });
    }

    private void cargarDialogPermisos() {

        AlertDialog.Builder dialog= new AlertDialog.Builder(CameraV0Activity.this);
        dialog.setTitle("Permisos Desactivados");
        dialog.setMessage("Debe aceptar los permisos para utlizar la APP");

        dialog.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,CAMERA},COP_CAMERA_SD_PERMISSIONS);
            }
        });
        dialog.show();
    }

    private void cargarImagen(){
       final CharSequence[] opciones ={"Tomar Foto","Cargar Imagen", "Cancelar"};
       final AlertDialog.Builder alertOciones= new AlertDialog.Builder(CameraV0Activity.this);
       alertOciones.setTitle("Seleccione una opción");
       alertOciones.setItems(opciones, new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
               if (opciones[which].equals("Tomar Foto")){
                   tomarFoto();
                   //Toast.makeText(CameraV0Activity.this, "Tomar Foto", Toast.LENGTH_SHORT).show();
               }
               else
               {
                   if (opciones[which].equals("Cargar Imagen")){
                       Intent intent= new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                       intent.setType("image/*");
                       startActivityForResult(intent.createChooser(intent,"Selecciona APP"),COD_SELECT);


                   }
               }

           }
       });
        alertOciones.show();

    }

    private void tomarFoto() {

        File file= new File(Environment.getExternalStorageDirectory(),RUTA_IMAGEN);
        boolean exists = file.exists();
        String nombre = "";
        if (exists==false){
            exists = file.mkdirs();
        }
        if (exists){
            nombre=(System.currentTimeMillis()/100)+".jpg";
        }

        path =  Environment.getExternalStorageDirectory()+File.separator+RUTA_IMAGEN+File.separator+nombre;

        File imagen= new File(path);

        Intent intent = null;
        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
            String authorities = getApplicationContext().getPackageName()+".provider";
            Uri imageUri= FileProvider.getUriForFile(this,authorities,imagen);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);

        }else
        {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imagen));
        }

        startActivityForResult(intent,COD_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK){

            switch (requestCode){
                case COD_SELECT:
                    Uri imagePath= data.getData();
                    imageView.setImageURI(imagePath);
                    break;

                case COD_PHOTO:
                    MediaScannerConnection.scanFile(this, new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("Ruta Almacenamiento","Path: "+path);

                        }
                    });

                    Bitmap bitmap= BitmapFactory.decodeFile(path);
                    imageView.setImageBitmap(bitmap);

                    break;
            }


        }
    }

}
