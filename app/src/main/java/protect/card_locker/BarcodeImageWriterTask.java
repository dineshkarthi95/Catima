package protect.card_locker;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.lang.ref.WeakReference;

/**
 * This task will generate a barcode and load it into an ImageView.
 * Only a weak reference of the ImageView is kept, so this class will not
 * prevent the ImageView from being garbage collected.
 */
class BarcodeImageWriterTask extends AsyncTask<Void, Void, Bitmap>
{
    private static final String TAG = "LoyaltyCardLocker";

    private final WeakReference<ImageView> imageViewReference;
    private final String cardId;
    private final BarcodeFormat format;
    private final int imageHeight;
    private final int imageWidth;

    public BarcodeImageWriterTask(ImageView imageView, String cardIdString,
                                  BarcodeFormat barcodeFormat)
    {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<>(imageView);

        cardId = cardIdString;
        format = barcodeFormat;
        imageHeight = imageView.getHeight();
        imageWidth = imageView.getWidth();
    }

    public Bitmap doInBackground(Void... params)
    {
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix bitMatrix;
        try
        {
            bitMatrix = writer.encode(cardId, format, imageWidth, imageHeight, null);

            final int WHITE = 0xFFFFFFFF;
            final int BLACK = 0xFF000000;

            int bitMatrixWidth = bitMatrix.getWidth();
            int bitMatrixHeight = bitMatrix.getHeight();

            int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

            for (int y = 0; y < bitMatrixHeight; y++)
            {
                int offset = y * bitMatrixWidth;
                for (int x = 0; x < bitMatrixWidth; x++)
                {
                    int color = bitMatrix.get(x, y) ? BLACK : WHITE;
                    pixels[offset + x] = color;
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight,
                    Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, bitMatrixWidth, 0, 0, bitMatrixWidth, bitMatrixHeight);
            return bitmap;
        }
        catch (WriterException | IllegalArgumentException e)
        {
            Log.e(TAG, "Failed to generate barcode", e);
        }

        return null;
    }

    protected void onPostExecute(Bitmap result)
    {
        ImageView imageView = imageViewReference.get();
        if(imageView == null)
        {
            // The ImageView no longer exists, nothing to do
            return;
        }

        imageView.setImageBitmap(result);
    }
}