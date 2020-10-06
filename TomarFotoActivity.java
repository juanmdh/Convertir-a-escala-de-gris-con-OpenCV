package com.dermaapp.juanjosue;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class TomarFotoActivity  extends AppCompatActivity {


    ImageView imgviewtomarfoto;
    EditText txtcorreo;
    Button  diagnosticar;

    private static final  int REQUEST_PERMISSION_GALLERY=100;
    private static final  int REQUEST_IMAGE_GALLERY=101;
    private static final  int REQUEST_PERMISSION_CAMERA=102;
    private static final  int REQUEST_IMAGE_CAMERA=102;
    String guardarruta;
    Uri photo;


    Bitmap grayBitmap,imagenBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tomarfoto);
        getSupportActionBar().hide();  /*PARA QUITAR EL ACTION BAR*/

        OpenCVLoader.initDebug();

        diagnosticar = (Button) findViewById(R.id.diagnosticar);
        diagnosticar.setEnabled(false);

        // Recibe el dato del MenuUsuario
        txtcorreo = (EditText) findViewById(R.id.txtcorreo);
        String dato=getIntent().getStringExtra("datosparaeltomarfoto");
        txtcorreo.setText(dato);
        txtcorreo.setEnabled(false);
        txtcorreo.setInputType(InputType.TYPE_NULL);

        imgviewtomarfoto = (ImageView) findViewById(R.id.imgviewtomarfoto); //imagen de tomar foto


    }



    //METODO : Permiso para la camara
    public void PermisoCamara(){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
            if(ActivityCompat.checkSelfPermission(TomarFotoActivity.this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
                TomarFotoCamara();
            }else{
                ActivityCompat.requestPermissions(TomarFotoActivity.this,new String[]{Manifest.permission.CAMERA},REQUEST_PERMISSION_CAMERA);
            }
        }else{
            TomarFotoCamara();
        }
    }

    //METODO : Permiso para el almacenamiento interno
    public void PermisoGaleria(){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
            if(ActivityCompat.checkSelfPermission(TomarFotoActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                AbrirGaleria();
            }else{
                ActivityCompat.requestPermissions(TomarFotoActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_PERMISSION_GALLERY);
            }
        }else{
            AbrirGaleria();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==REQUEST_PERMISSION_GALLERY){
            if(permissions.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED ){
                AbrirGaleria();
            }else if(permissions.length>0 && grantResults[0]== PackageManager.PERMISSION_DENIED ){
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.activity_mini_mensaje, (ViewGroup) findViewById(R.id.miniContenedor));
                TextView text = (TextView) layout.findViewById(R.id.tvMensaje);
                text.setText("Es necesario aceptar los permisos");
                Toast toast = new Toast(getApplicationContext());
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setView(layout);
                toast.show();
            }
        }



        if(requestCode==REQUEST_PERMISSION_CAMERA){
            if(permissions.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED ){
                TomarFotoCamara();
            }else if(permissions.length>0 && grantResults[0]== PackageManager.PERMISSION_DENIED ){
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.activity_mini_mensaje, (ViewGroup) findViewById(R.id.miniContenedor));
                TextView text = (TextView) layout.findViewById(R.id.tvMensaje);
                text.setText("Es necesario aceptar los permisos");
                Toast toast = new Toast(getApplicationContext());
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setView(layout);
                toast.show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode==REQUEST_IMAGE_GALLERY){
            if(resultCode==Activity.RESULT_OK && data!=null){
                 photo=data.getData();
                 //imgviewtomarfoto.setImageURI(photo);
                 try{
                    imagenBitmap= MediaStore.Images.Media.getBitmap(this.getContentResolver(),photo);
                }catch(IOException e){
                    e.printStackTrace();
                }
                imgviewtomarfoto.setImageBitmap(imagenBitmap);

            }else{
                Log.i("TAG","Result"+ resultCode);
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.activity_mini_mensaje, (ViewGroup) findViewById(R.id.miniContenedor));
                TextView text = (TextView) layout.findViewById(R.id.tvMensaje);
                text.setText("No se selecciono la imagen");
                Toast toast = new Toast(getApplicationContext());
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setView(layout);
                toast.show();
            }
        }

        if(requestCode==REQUEST_IMAGE_CAMERA){
            if(resultCode==Activity.RESULT_OK  ){
                imgviewtomarfoto.setImageURI(Uri.parse(guardarruta));
            }else{
                Log.i("TAG","Result"+ resultCode);
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.activity_mini_mensaje, (ViewGroup) findViewById(R.id.miniContenedor));
                TextView text = (TextView) layout.findViewById(R.id.tvMensaje);
                text.setText("No se tomo la foto");
                Toast toast = new Toast(getApplicationContext());
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setView(layout);
                toast.show();
            }
        }



        super.onActivityResult(requestCode, resultCode, data);
    }
    public void tomarfoto (View view){
        Cargarimagen();
        diagnosticar.setEnabled(true);
    }
    //METODO : Menu de seleccion de imagen
    private void Cargarimagen() {
        final CharSequence[] opcionesparacargarimagen={"Tomar foto","Cargar imagen","Cancelar"};
        final  AlertDialog.Builder alertOpciones = new AlertDialog.Builder(TomarFotoActivity.this);
        alertOpciones.setTitle("Seleccione una opción: ");
        alertOpciones.setItems(opcionesparacargarimagen, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(opcionesparacargarimagen[i].equals("Tomar foto")){
                    PermisoCamara();
                } else {
                    if(opcionesparacargarimagen[i].equals("Cargar imagen")){
                        PermisoGaleria();
                    }else{
                        dialogInterface.dismiss();
                    }
                }
            }
        });
        alertOpciones.show();
    }
    //METODO : Tomar foto con la camara
    private void TomarFotoCamara(){
        Intent camaraintent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(camaraintent.resolveActivity(getPackageManager())!=null){
            //startActivityForResult(camaraintent,REQUEST_IMAGE_CAMERA);
            File fotofile=null;
            try {
                fotofile = creararchivofoto();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(fotofile!=null){
                Uri fotouri= FileProvider.getUriForFile(this,"com.dermaapp.juanjosue",fotofile);
                camaraintent.putExtra(MediaStore.EXTRA_OUTPUT,fotouri);
                startActivityForResult(camaraintent,REQUEST_IMAGE_CAMERA);
            }
        }
    }
    //getExternalFilesDir()   -  getExternalStorageDirectory
    private File creararchivofoto() throws IOException {
        String timeStamp=new SimpleDateFormat("yyyyMMdd_HH-mm-ss", Locale.getDefault()).format(new Date());
        String nombrefoto= "DermaApp_"+ timeStamp +"_";
        File almacenamiento= getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File foto= File.createTempFile(nombrefoto,".jpg",almacenamiento);
        guardarruta = foto.getAbsolutePath();
        return foto;
    }

    private void AbrirGaleria(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,REQUEST_IMAGE_GALLERY);
    }

    public  void guardarImagenTratada(View view) {
//        ColorMatrix matrix = new ColorMatrix();
//        matrix.setSaturation(0);
//        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
//        imgviewtomarfoto.setColorFilter(filter);
//         https://www.youtube.com/watch?v=GhQ6CHh4Enk


        Mat Rgba= new Mat();
        Mat grayMat= new Mat();
        BitmapFactory.Options convertir = new BitmapFactory.Options();
        convertir.inDither= false;
        convertir.inSampleSize=4;
        int  width= imagenBitmap.getWidth();
        int  height= imagenBitmap.getHeight();
        grayBitmap= Bitmap.createBitmap(width,height,Bitmap.Config.RGB_565);
        //Bitmap a Mat
        Utils.bitmapToMat(imagenBitmap,Rgba);
        Imgproc.cvtColor(Rgba,grayMat,Imgproc.COLOR_RGB2GRAY);
        Utils.matToBitmap(grayMat,grayBitmap);
        imgviewtomarfoto.setImageBitmap(grayBitmap);
    }


    //METODO: Explicacion
    public  void Interrogante (View view){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.activity_mini_mensaje,(ViewGroup) findViewById(R.id.miniContenedor));
        TextView text=(TextView)layout.findViewById(R.id.tvMensaje);text.setText("Pulse la imagen de la camara para abrir el menu de opciones");
        Toast toast = new  Toast( getApplicationContext());
        //toast.setGravity(Gravity.CENTER_HORIZONTAL, 0,0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
    //METODO: Atras
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK);
        Intent atraslogeorecuperar= new Intent(this,MenuUsuarioActivity.class);
        //Pasa el dato al SplashActivityExplicacionModificar
        atraslogeorecuperar.putExtra("datoparaelmenu",txtcorreo.getText().toString());
        startActivity(atraslogeorecuperar);
        return false;
    }

}
