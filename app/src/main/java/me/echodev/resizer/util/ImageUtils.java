package me.echodev.resizer.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.File;
import java.io.IOException;

/**
 * Created by K.K. Ho on 3/9/2017.
 */

public class ImageUtils {
    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }

    public static File getScaledImage(int targetLength, int quality, Bitmap.CompressFormat compressFormat,
            String outputDirPath, String outputFilename, File sourceImage) throws IOException {
        File directory = new File(outputDirPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Prepare the new file name and path
        String outputFilePath = FileUtils.getOutputFilePath(compressFormat, outputDirPath, outputFilename, sourceImage);

        // Write the resized image to the new file
        Bitmap scaledBitmap = getScaledBitmap(targetLength, sourceImage);
        FileUtils.writeBitmapToFile(scaledBitmap, compressFormat, quality, outputFilePath);

        return new File(outputFilePath);
    }

    public static Bitmap getScaledBitmap(int targetLength, File sourceImage) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(sourceImage.getAbsolutePath(), options);

        // Get the dimensions of the original bitmap
        int originalWidth = options.outWidth;
        int originalHeight = options.outHeight;
        float aspectRatio = (float) originalWidth / originalHeight;
        int rotationInDegrees = 0;
        Matrix matrix = new Matrix();
        // Calculate the target dimensions
        int targetWidth, targetHeight;

        // Process EXIF information
        try {
          ExifInterface exif = new ExifInterface(sourceImage.getAbsolutePath());
          int rotation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
          rotationInDegrees = exifToDegrees(rotation);
          if (rotation != 0) {matrix.preRotate(rotationInDegrees);}
        } catch (IOException e) {
          // Silently fail
        }

        if (originalWidth > originalHeight) {
            targetWidth = targetLength;
            targetHeight = Math.round(targetWidth / aspectRatio);
        } else {
            aspectRatio = 1 / aspectRatio;
            targetHeight = targetLength;
            targetWidth = Math.round(targetHeight / aspectRatio);
        }

        // Scale bitmap
        bitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
        if (rotationInDegrees == 0) {
          return bitmap;
        }
        // Rotate bitmap if necessary EXIF rotated
        return Bitmap.createBitmap(bitmap, 0, 0, targetWidth, targetHeight, matrix, true);
    }
}
