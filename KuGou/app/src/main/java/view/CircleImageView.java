package view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class CircleImageView extends android.support.v7.widget.AppCompatImageView {
    private Paint mPaint;
    private int mRadius;
    private float mScale;
    public CircleImageView(Context context){
        super(context);
    }
    public CircleImageView(Context context, AttributeSet attrs){
        super(context,attrs);
    }
    public CircleImageView(Context context, AttributeSet attrs,int defStyleAttr){
        super(context,attrs,defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size=Math.min(getMeasuredWidth(),getMeasuredHeight());
        mRadius=size/2;
        setMeasuredDimension(size,size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint=new Paint();
        Bitmap bitmap=drawableToBitmap(getDrawable());
        BitmapShader bitmapShader=new BitmapShader(bitmap,Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        mScale=(mRadius*2.0f)/Math.min(bitmap.getWidth(),bitmap.getHeight());
        Matrix matrix=new Matrix();
        matrix.setScale(mScale,mScale);
        bitmapShader.setLocalMatrix(matrix);
        mPaint.setShader(bitmapShader);
        canvas.drawCircle(mRadius,mRadius,mRadius,mPaint);
    }
    private Bitmap drawableToBitmap(Drawable drawable){
        if(drawable instanceof BitmapDrawable){
            return ((BitmapDrawable) drawable).getBitmap();
        }
        int w=drawable.getIntrinsicWidth();
        int h=drawable.getIntrinsicHeight();
        Bitmap bitmap=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bitmap);
        drawable.setBounds(0,0,w,h);
        drawable.draw(canvas);
        return bitmap;
    }
}