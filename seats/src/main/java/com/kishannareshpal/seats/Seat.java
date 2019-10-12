package com.kishannareshpal.seats;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

public class Seat extends AppCompatImageView {

    public enum SeatMode {
        SELECTED,
        AVAILABLE
    }

    private AttributeSet set = null;
    private Context ctx;

    //
    private int fullWidth, fullHeight;
    private int padding = 12;
    private int animatedStrokeWidth;
    private RectF oval;
    private Paint main_paint, stroke_paint;
    private VectorDrawableCompat seat_icon;
    private SeatMode mode = SeatMode.AVAILABLE;
    private ValueAnimator valueAnimator;

    // attrs
    private int seatId = 0;
    private boolean isSelected = false;
    private boolean isDriver = false;


    public void toggleSelected(int maxAllowedSelection, int currSelectionNumber) {
        if (mode == SeatMode.AVAILABLE) {
            if ((maxAllowedSelection == -1) || (currSelectionNumber < maxAllowedSelection)) {
                setSeatMode(SeatMode.SELECTED);
                this.isSelected = true;
            }
        } else if (mode == SeatMode.SELECTED){
            setSeatMode(SeatMode.AVAILABLE);
            this.isSelected = false;
        }
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    private void setSeatMode(SeatMode mode) {
        this.mode = mode;
        switch (mode){
            case AVAILABLE:
                // available to select
                main_paint.setColor(ContextCompat.getColor(ctx, R.color.md_grey_300));
                break;

            case SELECTED:
                // selected
                main_paint.setColor(ContextCompat.getColor(ctx, R.color.md_grey_300));
                stroke_paint.setColor(ContextCompat.getColor(ctx, R.color.md_primary));
                valueAnimator.start();
                return;
        }
        invalidate();
    }


    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public int getSeatId() {
        return this.seatId;
    }

    /**
     * Constructors
     */
    public Seat(Context context) {
        super(context);
        this.ctx = context;
        init(); //
    }
    public Seat(Context context, boolean isDriver) {
        super(context);
        this.ctx = context;
        this.isDriver = isDriver;
        init(); //
    }
    public Seat(Context context, AttributeSet set) {
        super(context, set);
        this.ctx = context;
        this.set = set;
        init(); //
    }

    void init() {
        oval = new RectF();
        main_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        stroke_paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // icon
        int image = isDriver ? R.drawable.ic_steering_wheel_icon : R.drawable.ic_seat_icon;
        seat_icon = VectorDrawableCompat.create(ctx.getResources(), image, null);

        // This paint is used for seats that are available to select
        main_paint.setStyle(Paint.Style.FILL);
        int color = isDriver ? R.color.md_grey_100 : R.color.md_grey_300;
        main_paint.setColor(ContextCompat.getColor(ctx, color));

        // stroke paint
        stroke_paint.setStrokeCap(Paint.Cap.ROUND);
        stroke_paint.setStyle(Paint.Style.STROKE);
        stroke_paint.setColor(ContextCompat.getColor(ctx, R.color.md_primary));

        // setup value animator
        valueAnimator = ValueAnimator.ofInt(0, 8);
        valueAnimator.setDuration(100);
        valueAnimator.setInterpolator(new FastOutSlowInInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animatedStrokeWidth = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        fullWidth = w;
        fullHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = fullWidth - getPaddingLeft() - getPaddingRight() - padding;
        int height = fullWidth - getPaddingTop() - getPaddingBottom() - padding;

        // big background circle
        float cx            = fullWidth / 2;
        float cy            = fullHeight / 2;
        float circle_radius = width / 2;

        oval.top    = padding;
        oval.left   = padding;
        oval.right  = width;
        oval.bottom = height;
        canvas.drawCircle(cx, cy, circle_radius, main_paint);

        int inner_padding = 30;
        seat_icon.setBounds(inner_padding, inner_padding, width + padding - inner_padding, width + padding - inner_padding);

        if (mode == SeatMode.SELECTED) {
            stroke_paint.setStrokeWidth(animatedStrokeWidth);
            canvas.drawCircle(cx, cy, circle_radius, stroke_paint);
        }

        seat_icon.draw(canvas);

    }

}