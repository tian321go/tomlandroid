package com.axeac.app.sdk.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import com.axeac.app.sdk.R;
import com.axeac.app.sdk.activity.ImageShowActivity;
import com.axeac.app.sdk.adapters.MulitPointTouchListener;
import com.axeac.app.sdk.ui.base.Component;
import com.axeac.app.sdk.utils.ImageUtils;
import com.axeac.app.sdk.utils.StaticObject;

/**
 * describe:Graphic controls
 * 图形控件
 * @author axeac
 * @version 1.0.0
 */
public class Chart extends Component {

    private LinearLayout valLayout;
    private ImageView mChartView;
    private ImageView turnLeft, turnRight, chart_showimg;
    private Bitmap img;

    /**
     * 是否显示标题栏
     * <br>默认值为true
     * */
    private boolean toolbar = true;
    /**
     * 旋转角度
     * <br>默认值为0
     * */
    private int ScaleAngle = 0;

    /**
     * 图片地址数据
     * */
    private String data;
    /**
     * 是否异步加载
     * <br>默认值为true
     * */
    private boolean lazyLoading = true;

    public Chart(Activity ctx) {
        super(ctx);
        this.returnable = false;
        valLayout = (LinearLayout) LayoutInflater.from(ctx).inflate(R.layout.axeac_chart, null);
        mChartView = (ImageView) valLayout.findViewById(R.id.chart_view);
        turnLeft = (ImageView) valLayout.findViewById(R.id.chart_turnleft);
        turnRight = (ImageView) valLayout.findViewById(R.id.chart_turnright);
        chart_showimg = (ImageView) valLayout.findViewById(R.id.chart_showimg);
    }

    /**
     * 设置图片地址数据
     * @param data
     * 图片地址数据
     * */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * 设置是否异步加载
     * @param lazyLoading
     * 可选值 true|false
     * */
    public void setLazyLoading(String lazyLoading) {
        this.lazyLoading = Boolean.parseBoolean(lazyLoading);
    }

    /**
     * 执行方法
     * */
    @Override
    public void execute() {
        if (!this.visiable) return;
        if (!this.toolbar)
            valLayout.findViewById(R.id.toolbar).setVisibility(View.GONE);
        if (data != null || !data.equals("")) {

            Glide.with(ctx)
                    .load(StaticObject.getImageUrl(data))
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                            img = showImg(resource);
                            if (img != null) {
                                mChartView.setImageBitmap(img);
                            }
                        }
                    });
        }
        turnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnLeft();
                valLayout.invalidate();
            }
        });
        turnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnRight();
                valLayout.invalidate();
            }
        });
        chart_showimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(ctx, ImageShowActivity.class);
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(it);
            }
        });
        mChartView.setOnTouchListener(new MulitPointTouchListener(mChartView));
    }

    /**
     * 向左旋转图片
     * */
    public void turnLeft() {
        try {
            ScaleAngle--;
            int bmpWidth = img.getWidth();
            int bmpHeight = img.getHeight();
            Matrix matrix = new Matrix();
            matrix.setRotate(90 * ScaleAngle);
            Bitmap newBit = Bitmap.createBitmap(img, 0, 0, bmpWidth, bmpHeight, matrix, true);
            mChartView.setImageBitmap(newBit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 向右旋转图片
     * */
    public void turnRight() {
        try {
            ScaleAngle++;
            int bmpWidth = img.getWidth();
            int bmpHeight = img.getHeight();
            Matrix matrix = new Matrix();
            matrix.setRotate(90 * ScaleAngle);
            Bitmap newBit = Bitmap.createBitmap(img, 0, 0, bmpWidth, bmpHeight, matrix, true);
            mChartView.setImageBitmap(newBit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View getView() {
        return valLayout;
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public void repaint() {

    }

    @Override
    public void starting() {

    }

    @Override
    public void end() {

    }

    /**
     * 显示图片
     * @param bitmap
     * 显示的图片
     * */
    public Bitmap showImg(Bitmap bitmap) {
        int bwidth = bitmap.getWidth();
        int bheiht = bitmap.getHeight();
        StaticObject.img = bitmap;
        Bitmap newBitmap = null;
        if (this.width == "-1" && this.height == -1) {
            if (bwidth <= StaticObject.deviceWidth) {
                newBitmap = bitmap;
                chart_showimg.setVisibility(View.GONE);
            } else {
                chart_showimg.setVisibility(View.VISIBLE);
                double percent = Double.valueOf(StaticObject.deviceWidth + "D") / Double.valueOf(bwidth + "D");
                newBitmap = ImageUtils.resizeImage(bitmap, StaticObject.deviceWidth, (int) (bheiht * percent));
            }
        } else {
            if (this.width == "-1") {
                double percent = Double.valueOf(height + "D") / Double.valueOf(bheiht + "D");
                newBitmap = ImageUtils.resizeImage(bitmap, (int) (bwidth * percent), height);
            } else if (this.height == -1) {
                double percent = Double.valueOf(width + "D") / Double.valueOf(bwidth + "D");
                newBitmap = ImageUtils.resizeImage(bitmap, Integer.parseInt(width), (int) (bheiht * percent));
            } else {
                newBitmap = bitmap;
            }
        }
        return newBitmap;
    }

    /**
     * 设置是否显示标题栏
     * @param toolbar
     * 可选值 true|false
     * */
    public void setToolbar(String toolbar) {
        this.toolbar = "true".equals(toolbar);
    }

}