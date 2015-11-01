package com.loquedev.event4;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.loquedev.event4.qr.IntentIntegrator;
import com.loquedev.event4.qr.IntentResult;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    TextView txtName;
    TextView txtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUI();
        setToolbar();
    }

    public void setUI(){
        txtName = (TextView) findViewById(R.id.txt_username);
        txtStatus = (TextView) findViewById(R.id.txt_status);

        txtName.setText("");
        txtStatus.setText("");
    }

    public void setToolbar(){
        //Configurando que el Toolbar como ActionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_my_toolbar);
        //En este ejemplo, ocultamos el titulo de la aplicación, esto es opcional
        toolbar.setTitle("Event4");
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void readQr(View view){
        //Toast.makeText(this, "Read Qr", Toast.LENGTH_LONG).show();

        //Call the qr reader by intent
        IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
        integrator.initiateScan();
    }

    List<NameValuePair> params;
    String url = "http://event4-jnava.rhcloud.com/AppScript/Acreditar.php";
    String respStr;
    String name;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if(scanResult.getContents() != null){
            params = new ArrayList<NameValuePair>();
            String email = scanResult.getContents().toString();
            params.add(new BasicNameValuePair("Email", email));
            Toast.makeText(getApplicationContext(), email, Toast.LENGTH_SHORT).show();
            new ConsultUser().execute();
        }else{
            Toast.makeText(getApplicationContext(), "No barcode read, tried again!", Toast.LENGTH_SHORT).show();
        }
    }

    private void consultaBdx() {
        try {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            try{
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse resp;
                resp = httpClient.execute(httpPost);

                JSONObject json = new JSONObject(EntityUtils.toString(resp.getEntity()));
                respStr = json.getString("Rpta");

                if(respStr.equals("1")){
                    name = json.getString("Nombre");
                }

            }catch(ClientProtocolException e){
                Toast.makeText(getApplicationContext(), "error HttpResponse", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch(IOException e){
                Toast.makeText(getApplicationContext(), "No connection with the server", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }catch (JSONException e){
                e.printStackTrace();
            }
        }catch(UnsupportedEncodingException e){
            Toast.makeText(getApplicationContext(), "error httppost "+e.toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private class ConsultUser extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            consultaBdx();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(respStr.equals("1")){
                txtName.setText(name);
                txtStatus.setText("User registered");
                txtStatus.setTextColor(Color.parseColor("#2E7D32"));
            }else{
                txtName.setText("");
                txtStatus.setText("User no suscribed");
                txtStatus.setTextColor(Color.parseColor("#FF0000"));
            }
        }
    }

}
