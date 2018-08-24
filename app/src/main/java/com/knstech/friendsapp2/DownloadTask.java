package com.knstech.friendsapp2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTask extends AsyncTask<String, Integer, String> {

    private Context context;
    private PowerManager.WakeLock mWakeLock;
    ProgressDialog progressDialog;
    private String filePath1,filename,fileType;

    public DownloadTask(Context context, ProgressDialog progressDialog,String filename,String fileType){
        this.context=context;
        this.progressDialog=progressDialog;
        this.filename =filename;
        this.fileType = fileType;
    }

    @Override
    protected String doInBackground(String... strings) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection =null;
        try{
            URL url = new URL(strings[0]);
            connection = (HttpURLConnection)url.openConnection();
            connection.connect();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                return "Server returned HTTP "+ connection.getResponseCode()+ " "+ connection.getResponseMessage();
            }

            int fileLength = connection.getContentLength();

            input = connection.getInputStream();
            File f=null;

            if(fileType.equals("application")){
            f= new File(Environment.getExternalStorageDirectory()+"/Friends_App"+"/DOC"+"/"+filename);
            }
            else if(fileType.equals("video")){
                f= new File(Environment.getExternalStorageDirectory()+"/Friends_App"+"/VIDEO"+"/"+filename);

            }
            else if(fileType.equals("audio")){
                f= new File(Environment.getExternalStorageDirectory()+"/Friends_App"+"/AUDIO"+"/"+filename);

            }

            filePath1 = f.getAbsolutePath();
            output = new FileOutputStream(f);
            byte data[]= new byte[4096];
            long total = 0;
            int count =0;
            while((count = input.read(data)) != -1){

                if (isCancelled()){
                    input.close();
                    return null;
                }

                total+=count;
                if(fileLength > 0 ){
                    publishProgress((int)(total*100/fileLength));
                }
                output.write(data, 0 , count);
            }
        }
        catch (Exception e){
            return e.toString();
        }
        finally {
            try{

                if(output != null){
                    output.close();
                }
                if (input != null){
                    input.close();
                }
            }
            catch (IOException e){

            }
            if(connection != null)
                connection.disconnect();
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,getClass().getName());
        mWakeLock.acquire();
        progressDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(String s) {
        mWakeLock.release();
        progressDialog.dismiss();
        if(s != null){
            Toast.makeText(context, "Download Error", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(context, "File Downloaded", Toast.LENGTH_SHORT).show();
    }
}
