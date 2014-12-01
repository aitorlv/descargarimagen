package com.example.aitor.descargarimagen;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class Principal extends Activity  {
    EditText url,etnombre;
    ImageView cargadorImagen;
    RadioButton rbprivada,rbpublica;
    RadioGroup grupo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_principal);
            url = (EditText) findViewById(R.id.ruta);
            etnombre = (EditText) findViewById(R.id.nombre);
            grupo = (RadioGroup) findViewById(R.id.grupoLugar);
            rbprivada = (RadioButton) findViewById(R.id.privada);
            rbpublica = (RadioButton) findViewById(R.id.publica);
            cargadorImagen = (ImageView) findViewById(R.id.imageView);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(cargadorImagen.getDrawable()!=null) {
            Bitmap imagen = ((BitmapDrawable) cargadorImagen.getDrawable()).getBitmap();
            outState.putParcelable("imagen", imagen);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState!=null) {
            cargadorImagen.setImageBitmap((Bitmap) savedInstanceState.getParcelable("imagen"));
        }
    }



//cogemos los valores delos campos de texto, llamamos a los metodos para saber el nombre del archivo para gaurdar
// lugra de memoria  etc etc,
//y contruimos el array con los datos que le pasaremos a la hebra.
   public void descargarImg(View v) {
       String rutadescarga,nombre;
       String [] ruta = new String[3] ;
       rutadescarga=url.getText().toString();
       if(rutadescarga.compareTo("")!=0) {
           ruta[0] = rutadescarga;
           nombre=etnombre.getText().toString();
           if(nombreDeFoto(nombre,rutadescarga).compareTo("mala")!=0) {
               ruta[1] = nombreDeFoto(nombre, rutadescarga);
               if (lugarDeGuardado(grupo.getCheckedRadioButtonId()) != null) {
                   ruta[2] = lugarDeGuardado(grupo.getCheckedRadioButtonId());
                   Hebradescarga hd = new Hebradescarga();
                   hd.execute(ruta);
               } else {
                   tostada("Debes seleccionar donde guardar la imagen");
               }
           }else{
               tostada("Extension inapropiada");
           }
       }else{
           tostada("Debes indicar una ruta");
       }

   }

//vemos si el campo nombre este relleno, si lo esta montamos una cadena con el nombre y el tipo,
// si no retornamos el nombre de descarga
   public String nombreDeFoto(String nombre,String ruta){
       String tipo;
       tipo=obtenerTipo(ruta);
       if(nombre.length()!=0){
           if(tipo.compareTo(".jpg")!=0 && tipo.compareTo(".png")!=0 && tipo.compareTo(".gif")!=0){
               return "mala";
           }else{
               return nombre+""+tipo;
           }
       }else{
           if(tipo.compareTo(".jpg")!=0 && tipo.compareTo(".png")!=0 && tipo.compareTo(".gif")!=0){
               return "mala";
           }else{
               return nombreTipo(ruta);
           }
       }
   }

    //vemos en que lugar ha de ser guardada la imagen
    public String lugarDeGuardado(int lugar){
        if(lugar==R.id.privada){
            return "privada";
        }else if(lugar==R.id.publica){
            return "publica";
        }
            return null;

    }

    // si el campo nombre esta relleno sacamos el formato con el que se descarga la imagen
    public String obtenerTipo(String ruta){
       return  ruta.substring(ruta.length()-4,ruta.length());

    }

    //si el campo nomber esta vacio sacamos el nombre y el foramato con el que se descarga la foto
    public String nombreTipo(String ruta){
        String [] todos=ruta.split("/");
        return (todos[todos.length-1]);
    }

    //metodo para mosotrar mensajes
    public void tostada(String cadena){
        Toast.makeText(this,cadena,Toast.LENGTH_SHORT).show();
    }


    //--------------------------------------------HEBRA-----------------------------------
    //creamos la hebra que s encargara de la descarga

    private class Hebradescarga extends AsyncTask<String,Void,Bitmap> {
        ProgressDialog pd;
        String nom,memo;

        //creamos el ProgressDialog para que se muestre mientras se descarga la foto
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd=new ProgressDialog(Principal.this);
            pd.setMessage("Bajando imagen");
            pd.setCancelable(true);
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.show();
        }
//llamamos al metodo descargar encargado de descargar la imagen
        @Override
        protected Bitmap doInBackground(String... params) {
            nom=params[1];
            memo=params[2];
            Bitmap img=descargar(params[0]);
            return img;
        }

        //cerramos el dialogo e intentamos guardar la foto,esta solo se guardara si hay memoria suficiente
        //muestra la imagen en el imageview
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            pd.dismiss();
            //Bitmap a =((BitmapDrawable)cargadorImagen.getDrawable()).getBitmap();
            if(guardarImagen(nom,memo,bitmap).compareTo("nomemoria")!=0) {
                cargadorImagen.setImageBitmap(bitmap);
            }else{
                tostada("memoria insuficiente");
            }



        }

//le llega la ruta de donde descargamos la imagen y descarga la imagen
        public Bitmap descargar(String descarga) {
            URL imageUrl = null;
            Bitmap imagen = null;
            try{
                imageUrl = new URL(descarga);
                HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
                conn.connect();
                imagen = BitmapFactory.decodeStream(conn.getInputStream());
            }catch(IOException ex){
                ex.printStackTrace();
            }

            return imagen;
        }

        //guarda la imagen comprobando antes si hay memoria suficiente
        public String guardarImagen (String nombre, String memoria, Bitmap imagen){
            File myPath=null,rutadeguardado=null;
            if(memoria.compareTo("privada")==0) {
                rutadeguardado=getExternalFilesDir(null);
                myPath = new File(rutadeguardado, nombre);

            }else if(memoria.compareTo("publica")==0){
                rutadeguardado=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                myPath = new File(rutadeguardado, nombre);
            }
            if(tamanoOcupado(rutadeguardado)) {
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(myPath);
                    imagen.compress(Bitmap.CompressFormat.JPEG, 10, fos);
                    fos.flush();
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                return myPath.getAbsolutePath();
            }else {
                return"nomemoria";
            }
        }

        //se encarga ver de si la memoria es suficiente
        public boolean tamanoOcupado(File memoria){
            float total=(float)memoria.getTotalSpace();
            float libre=(float)memoria.getFreeSpace();
            float disponible=(libre/total)*100;
            if(disponible>10){
                return true;
            }else{
                return false;
            }
        }
    }

}
