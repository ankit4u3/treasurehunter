
package org.treasurehunter;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.treasurehunter.task.Task;

import android.app.Activity;
import android.content.Context;
import android.database.CursorJoiner.Result;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// You must provide types for the three generic parameters before the code will compile.          
// For more details, see http://developer.android.com/reference/android/os/AsyncTask.html 
class ElectromedicaTask extends AsyncTask {

    @Override
    protected Object doInBackground(Object... params) {
        Log.d("Background", "Text");
        return null;
    }

}
