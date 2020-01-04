package weakling.segunda.mano.utils;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadManager extends AsyncTask<String, Void, InputStream> {
    private OnCompletedListener listener;

    public DownloadManager(){}

    public DownloadManager(OnCompletedListener listener){
        this.listener = listener;
    }

    protected InputStream doInBackground(String... url){
        try{
            return new URL(url[0]).openStream();
        }
        catch(MalformedURLException e){ }
        catch(IOException e) { }
        return null;
    }

    @Override
    protected void onPostExecute(InputStream inputStream) {
        listener.onComplete(inputStream);
    }

    public interface OnCompletedListener{

        public void onComplete(InputStream inputStream);

    }
}
