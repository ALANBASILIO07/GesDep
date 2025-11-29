package com.uaemex.gesdep;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * VideoView personalizado que escala el video para cubrir toda la pantalla
 * El video se recorta proporcionalmente si es necesario
 */
public class FullScreenVideoView extends VideoView {

    private int videoWidth;
    private int videoHeight;

    public FullScreenVideoView(Context context) {
        super(context);
    }

    public FullScreenVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FullScreenVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(videoWidth, widthMeasureSpec);
        int height = getDefaultSize(videoHeight, heightMeasureSpec);

        if (videoWidth > 0 && videoHeight > 0) {
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);

            // Calcular escala para cubrir toda la pantalla
            float videoRatio = (float) videoWidth / videoHeight;
            float screenRatio = (float) widthSize / heightSize;

            if (videoRatio > screenRatio) {
                // El video es mas ancho, ajustar por altura
                width = (int) (heightSize * videoRatio);
                height = heightSize;
            } else {
                // El video es mas alto, ajustar por ancho
                width = widthSize;
                height = (int) (widthSize / videoRatio);
            }
        }

        setMeasuredDimension(width, height);
    }

    public void setVideoSize(int width, int height) {
        videoWidth = width;
        videoHeight = height;
        requestLayout();
    }
}