package com.aidabolsari.fileconverterapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ConverterActivity extends AppCompatActivity {
    private static final int READ_REQUEST_CODE = 42;
    RequestQueue mRequestQueue;
    private Uri uri;
    String extTo;
    String id;
    String encodedResult;
    final static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/converted_files/";
    Spinner spinner2;
    Spinner spinner1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_converter);
        mRequestQueue = Volley.newRequestQueue(this);
        Log.i("MYTAG", Environment.getExternalStorageDirectory().getAbsolutePath());
        spinner2 = (Spinner) findViewById(R.id.spinner);
        spinner1 = (Spinner) findViewById(R.id.spinner2);
//
//        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//        StrictMode.setVmPolicy(builder.build());
    }

    public void onClickButtonConvert(View view) {
        extTo = spinner2.getSelectedItem().toString();
        String extFrom = spinner1.getSelectedItem().toString();
        if (extTo.equals(extFrom) || extTo.equals("") || extTo.equals(" "))
            return;

        if (uri != null) {
            postConvertedFile();
        }
        else
            Log.i("MYTAG", "URI IS NULL");
    }

    public void onClickFileUpload(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                this.uri = resultData.getData();
                Log.i("MYTAG", "uri: " + uri);
                File file = new File(uri.getPath());
                Log.i("MYTAG", "file: " + file);
                Log.i("MYTAG", "name" + file.getName());
                Log.i("MYTAG", getFileName(uri));
            }
        }

    }

    private String encodeFileToBase64Binary(Uri uri) throws IOException {
        InputStream iStream = getContentResolver().openInputStream(uri);
        byte[] bytes = getBytes(iStream);
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void decodeFileToBase64Binary(String base64) throws IOException {
        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);

        String fileName = getFileName(uri).substring(0, getFileName(uri).indexOf(".")) + "." + extTo;
        Log.d("MYTAG", fileName);
        try {
            new File(path).mkdir();
            File file = new File(path + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file,true);
            fileOutputStream.write(bytes);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    File file1 = new File(path + fileName);
                    // Uri uri = Uri.fromFile(file1);
                    Uri uri = FileProvider.getUriForFile(ConverterActivity.this, BuildConfig.APPLICATION_ID + ".provider", file1);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extTo);
                    Log.i("MYTAG", mime);
                    Log.i("MYTAG", uri.getPath());
                    Log.i("MYTAG", "uri: " + uri);
                    intent.setDataAndType(uri, mime);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                }
            }, 5000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getConvertedFile() {
        Log.i("MYTAG", "12 ");
        String url = "https://api.convertio.co/convert/" + id + "/dl/base64";
        Log.i("MYTAG", url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.i("MYTAG", "JSON: ");
                    Log.i("MYTAG", "JSON: " + response);
                    JSONObject data = response.getJSONObject("data");
                    Log.i("MYTAG", "JSON: ");
                    encodedResult = data.getString("content");
                    Log.i("MYTAG", "JSON: ");
                    decodeFileToBase64Binary(encodedResult);
                    Log.i("MYTAG", "JSON: ");
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }, Throwable::printStackTrace);

        mRequestQueue.add(request);
    }

//    response -> {
//        try {
//            JSONObject data = response.getJSONObject("data");
//            encodedResult = data.getString("content");
//            decodeFileToBase64Binary(encodedResult);
//        } catch (JSONException | IOException e) {
//            e.printStackTrace();
//        }
//    }

    public void postConvertedFile() {
        try {
            String URL = "https://api.convertio.co/convert";
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("apikey", "c05b337db5db05bc74c335275c7d1ec0");
            jsonBody.put("input", "base64");
            jsonBody.put("file", encodeFileToBase64Binary(uri));
            jsonBody.put("filename", getFileName(this.uri));
            jsonBody.put("outputformat", extTo);

            Log.i("MYTAG", "JSON: " + jsonBody);

            JsonObjectRequest jsonOblect = new JsonObjectRequest(Request.Method.POST, URL, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Toast.makeText(getApplicationContext(), "Response:  " + response.toString(), Toast.LENGTH_SHORT).show();
                    try {
                        Log.i("MYTAG", "JSON: " + response);
                        JSONObject data = response.getJSONObject("data");
                        Log.i("MYTAG", "ID: " + data.getString("id"));
                        id = data.getString("id");
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getConvertedFile();
                            }
                        }, 5000);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    onBackPressed();
                }
            });
            mRequestQueue.add(jsonOblect);

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        Log.i("MYTAG", "#################");
    }

    @SuppressLint("Range")
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}