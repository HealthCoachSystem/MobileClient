package com.example.healthcoachbluetooth;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import io.esense.esenselib.ESenseConfig;
import io.esense.esenselib.ESenseEvent;
import io.esense.esenselib.ESenseSensorListener;


public class SensorListenerManager extends AppCompatActivity implements ESenseSensorListener {
    private int global_cnt = 0;
    private long timeStamp;
    private double[] accel;
    private double[] gyro;
    private double acc_rms,gyro_rms, previousrms = 0, MA_rms;
    private boolean In = false;
    private int count =0;

    private String xAcc="";
    private String yAcc="";
    private String zAcc="";
    private String xRot="";
    private String yRot="";
    private String zRot="";
    private String AccRms="";
    private String RotRms="";
    private String roll="";
    private String pitch="";
    private int nDataCount=0;



    Context context;

    ESenseConfig eSenseConfig;

    public SensorListenerManager(Context context) {
        this.context = context;
        eSenseConfig = new ESenseConfig();
    }

    @Override
    public void onSensorChanged(ESenseEvent evt)  throws JSONException {
        nDataCount++; // window size 만큼 카운트
        accel = evt.convertAccToG(eSenseConfig);
        gyro = evt.convertGyroToDegPerSecond(eSenseConfig);

        // 운동 카운팅용
        acc_rms = Math.sqrt((Math.pow(accel[0], 2) + Math.pow(accel[1], 2) + Math.pow(accel[2], 2)));
        gyro_rms = Math.sqrt((Math.pow(gyro[0], 2) + Math.pow(gyro[1], 2) + Math.pow(gyro[2], 2)));

        // 서버에 보내기 위해 ','로 분리
        xAcc+=accel[0]+",";
        yAcc+=accel[1]+",";
        zAcc+=accel[2]+",";
        xRot+=gyro[0]+",";
        yRot+=gyro[1]+",";
        zRot+=gyro[2]+",";
        AccRms+=acc_rms+",";
        RotRms+=gyro_rms+",";
        roll+=Math.atan(accel[1]/accel[2])+",";
        pitch+=Math.atan(accel[0]/accel[2])+",";

        timeStamp = evt.getTimestamp();


        //Log.d("accelx", ""+accel[0]);
        //Log.d("accely", ""+accel[1]);
        //Log.d("accelz", ""+accel[2]);
        //Log.d("gyrox", ""+gyro[0]);
        //Log.d("gyroy", ""+gyro[1]);
        //Log.d("gyroz", ""+gyro[2]+"\n");


        MA_rms = 0.2*previousrms + 0.8*acc_rms;

       if(MA_rms < 0.7){
           if(In == false){
               count++ ;
               In = true;

               Log.d("count", ""+count);
           }
       }
       else{
           if(In == true){
               In = false;
           }
       }

        previousrms = acc_rms;

        if (nDataCount==30) {
            // json 만들고
            JSONObject data = new JSONObject();

            xAcc=xAcc.substring(0,xAcc.length()-1);
            yAcc=yAcc.substring(0,yAcc.length()-1);
            zAcc=zAcc.substring(0,zAcc.length()-1);
            xRot=xRot.substring(0,xRot.length()-1);
            yRot=yRot.substring(0,yRot.length()-1);
            zRot=zRot.substring(0,zRot.length()-1);
            AccRms=AccRms.substring(0,AccRms.length()-1);
            RotRms=RotRms.substring(0,RotRms.length()-1);
            roll=roll.substring(0,roll.length()-1);
            pitch=pitch.substring(0,pitch.length()-1);


            data.put("xAcc",xAcc);
            data.put("yAcc",yAcc);
            data.put("zAcc",zAcc);
            data.put("xRot",xRot);
            data.put("yRot",yRot);
            data.put("zRot",zRot);
            data.put("AccRms",AccRms);
            data.put("RotRms",RotRms);
            data.put("roll",roll);
            data.put("pitch",pitch);

            String url="http://34.67.193.96:2431";

            NetworkTask networkTask = new NetworkTask(url, data);
            networkTask.execute();

            nDataCount=0;

            xAcc="";
            yAcc="";
            zAcc="";
            xRot="";
            yRot="";
            zRot="";
            AccRms="";
            RotRms="";
            roll="";
            pitch="";
        }
    }

    public class NetworkTask extends AsyncTask<Void, Void, String> {
        String url;
        JSONObject values;
        NetworkTask(String url, JSONObject values){
            this.url = url;
            this.values = values;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progress bar를 보여주는 등등의 행위
        }
        @Override
        protected String doInBackground(Void... params) {
            String result;
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            result = requestHttpURLConnection.request(url, values);
            return result; // 결과가 여기에 담깁니다. 아래 onPostExecute()의 파라미터로 전달됩니다.
        }
        @Override
        protected void onPostExecute(String result) {
            // 통신이 완료되면 호출됩니다.
            // 결과에 따른 UI 수정 등은 여기서 합니다.
            //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            Log.d("result", result);
            global_cnt++;
            Log.d("cnt", Integer.toString(global_cnt));
        }
    }

}
