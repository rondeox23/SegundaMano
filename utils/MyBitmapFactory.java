package weakling.segunda.mano.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class MyBitmapFactory {

    public static Bitmap getImagefromUri(Context context, Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri,"r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    public static float convertDpToPx(Context context, float dp){
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static float convertPxToDp(Context context, float px){
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static class GetImageFromUrl extends AsyncTask<String, Void, Bitmap> {

        public GetImageFromUrl(){}

        protected Bitmap doInBackground(String... uri){
            Bitmap icon = null;
            try{
                InputStream in = new URL(uri[0]).openStream();
                icon = BitmapFactory.decodeStream(in);
            }catch(Exception e){
                return null;
            }
            return icon;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(listener!=null){
                listener.onPostExecute(bitmap);
            }
        }

        private SuccessListener listener;

        public GetImageFromUrl(SuccessListener listener){
            this.listener = listener;
        }

        public interface SuccessListener{

            public void onPostExecute(Bitmap result);

        }

    }

}
