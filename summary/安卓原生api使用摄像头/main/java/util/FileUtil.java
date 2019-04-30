package util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

public class FileUtil {
	private static final  String TAG = "FileUtil";
	private static final File parentPath = Environment.getExternalStorageDirectory();
	public static   String storagePath = "";
	public static String DST_FOLDER_NAME = "PlayCamera";

	/**
	 * @return
	 */
	private static String initPath(){
		if(storagePath.equals("")){
			storagePath = "/mnt/extsd"+"/" + DST_FOLDER_NAME;
			File f = new File(storagePath);
			if(!f.exists()){
				f.mkdir();
			}
		}
		return storagePath;
	}

	/**
	 * @param b
	 */
	public static void saveBitmap(Bitmap b){

		String path =initPath();
		long dataTake = System.currentTimeMillis();
		String jpegName = path + "/" + dataTake +".jpeg";
//		Log.i(TAG, "saveBitmap:jpegName = " + jpegName);
		try {
			FileOutputStream fout = new FileOutputStream(jpegName);
			BufferedOutputStream bos = new BufferedOutputStream(fout);
			b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			bos.flush();
			bos.close();
//			Log.i(TAG, "saveBitmap:ok");
		} catch (IOException e) {
//			Log.i(TAG, "saveBitmap:nothing");
			e.printStackTrace();
		}
	}
}
