/*
 * Copyright (c) 2025, [1Sec team]. All rights reserved.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 */

package com.wangdi.onesec.core;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.wangdi.onesec.utils.BasicUtils;

/**
 * This class provide an API to adjust the properties of the views quickly,
 * which includes: size, position, internationalization, etc.
 *
 * 
 * @author Di Wang
 * @version 1.0
 */
public final class ViewManager
{
    public static final byte RELATIVE_TO_WIDTH = 0;
    public static final byte RELATIVE_TO_HEIGHT = 1;

    private String text;
    private final BasicActivity activity;
    private final View view;
    private final TextView textView;
    private final ViewGroup parent;
    private final ViewGroup.LayoutParams params;

    private final ConstraintLayout.LayoutParams clParams;

    public ViewManager(BasicActivity activity, int id)
    {
        this.activity = activity;
        this.view = activity.findViewById(id);
        this.textView = this.view instanceof TextView ? (TextView)(this.view) : null;
        this.parent = (ViewGroup)(this.view.getParent());
        this.params = this.view.getLayoutParams();
        this.clParams = (this.params instanceof ConstraintLayout.LayoutParams) ? (ConstraintLayout.LayoutParams)(this.view.getLayoutParams()) : null;

        this.update();

        if (this.textView != null)
            this.text = this.textView.getText().toString();
    }

    /**
     * Updates the layout of the parent view. This method measures the parent
     * view to ensure it has the exact width and height as specified, then
     * lays out the parent view using the current left, top, right, and bottom
     * positions. This is useful for recalculating the layout after changes
     * to child views or layout parameters.
     */
    private void update()
    {
        this.parent.measure(
                View.MeasureSpec.makeMeasureSpec(this.parent.getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(this.parent.getHeight(), View.MeasureSpec.EXACTLY)
        );

        this.parent.layout(
                this.parent.getLeft(),
                this.parent.getTop(),
                this.parent.getRight(),
                this.parent.getBottom()
        );
    }

    /**
     * Sets the background of the view using a drawable resource ID. This method
     * posts the operation to the UI thread to ensure that the background is set
     * correctly and efficiently.
     * 
     * @param id                the resource ID of the drawable to set as the background
     * @return                  this object
     */
    public ViewManager setBackGround(int id)
    {
        this.parent.post(() -> this.view.setBackgroundResource(id));
        return this;
    }

    /**
     * <p>
     * Adjusts the vertical position of the current view by a specified bias
     * relative to the parent view's height. This adjustment is performed
     * asynchronously on the UI thread.
     * </p>
     *
     * <p>
     * If the current view is within a ConstraintLayout, the function calculates
     * the new vertical bias based on the given bias value and updates the
     * view's layout parameters accordingly. The horizontal position is recalculated
     * to maintain the view's position relative to its width.
     * </p>
     *
     * <p>
     * If the view is not part of a ConstraintLayout, a warning message is logged,
     * indicating that the position cannot be adjusted.
     * </p>
     *
     * @param id                the identifier of the view whose position is used as a reference
     *                          for calculating the new position of the current view
     * @param bias              the fraction of the parent's height by which to adjust the view's
     *                          vertical position
     *
     * @return                  this ViewManager instance
     */
    public ViewManager down(int id, double bias)
    {
        this.parent.post(() ->
        {
            final Paint paint = new Paint();
            paint.setTypeface(this.textView.getTypeface());
            paint.setAntiAlias(true);

            if (this.clParams != null)
            {
                final View view1 = activity.findViewById(id);

                final int view1Width = view1.getWidth(), view1Height = view1.getHeight();
                final int parentWidth = this.parent.getWidth(), parentHeight = this.parent.getHeight();

                final double x = (parentWidth - view1Width) * this.clParams.horizontalBias + 0.5 * view1Width;
                final double y = (parentHeight - view1Height) * this.clParams.verticalBias + 0.5 * view1Height;
                final double newY = y + bias * parentHeight;

                final double width = this.view instanceof TextView ? paint.measureText(this.text) : this.view.getWidth();
                this.clParams.horizontalBias = (float)((x - 0.5 * width) / (parentWidth - width));
                this.clParams.verticalBias = (float)((newY - 0.5 * this.view.getHeight()) / (parentHeight - this.view.getHeight()));

                this.view.setLayoutParams(this.clParams);
            }
            else
            {
                try {this.activity.logger.warning("Cannot set: ", this.view.getId(), "'s position, because it is not in ConstrainLayout");}
                catch (Exception e) {System.err.println(BasicUtils.getStackTraceAsString(e));}
            }
        });

        return this;
    }

    /**
     * Sets the position of the view, given as bias in the layout.
     * The bias is a value between 0 and 1 that specifies the
     * fraction of the parent that the view should occupy.
     *
     * @param horizontalBias    the horizontal bias
     * @param verticalBias      the vertical bias
     * 
     * @return                  this object
     */
    public ViewManager setPosition(double horizontalBias, double verticalBias)
    {
        this.parent.post(() ->
        {
            if (this.clParams != null)
            {
                this.clParams.horizontalBias = (float)(horizontalBias);
                this.clParams.verticalBias = (float)(verticalBias);
                this.view.setLayoutParams(this.clParams);
            }
            else
            {
                try {this.activity.logger.warning("Cannot set: ", this.view.getId(), "'s position, because it is not in ConstrainLayout");}
                catch (Exception e) {System.err.println(BasicUtils.getStackTraceAsString(e));}
            }
        });

        return this;
    }

    /**
     * Sets the size of the view, given as bias in the layout.
     * The bias is a value between 0 and 1 that specifies the
     * fraction of the parent that the view should occupy.
     *
     * @param type              the type of the bias, given as either
     *                          RELATIVE_TO_WIDTH or RELATIVE_TO_HEIGHT
     * @param bias              the bias value
     *
     * @return                  this object
     */
    public ViewManager setSize(byte type, double bias)
    {
        this.parent.post(() ->
        {
            int value;

            if (type == RELATIVE_TO_WIDTH)
                value = (int)(Math.round(bias * this.parent.getWidth()));
            else
                value = (int)(Math.round(bias * this.parent.getHeight()));

            if (this.clParams != null)
            {
                this.clParams.width = value;
                this.clParams.height = value;
                this.view.setLayoutParams(this.clParams);
            }
            else
            {
                this.params.width = value;
                this.params.height = value;
                this.view.setLayoutParams(this.params);
            }
        });

        return this;
    }

    /**
     * Sets the size of the view, given as bias in the layout.
     * The bias is a value between 0 and 1 that specifies the
     * fraction of the parent that the view should occupy.
     *
     * @param widthBias         the horizontal size bias
     * @param heightBias        the vertical size bias
     * 
     * @return                  this object
     */
    public ViewManager setSize(double widthBias, double heightBias)
    {
        this.parent.post(() ->
        {
            if (this.clParams != null)
            {
                this.clParams.width = (int)(Math.round(widthBias * this.parent.getWidth()));
                this.clParams.height = (int)(Math.round(heightBias * this.parent.getHeight()));
            }
            else
            {
                this.params.width = (int)(Math.round(widthBias * this.parent.getWidth()));
                this.params.height = (int)(Math.round(heightBias * this.parent.getHeight()));
            }
        });

        return this;
    }

    /**
     * Sets the style of the text in the view. The style should be
     * one of the following constants from the Typeface class:
     * <ul>
     *  <li>{@link android.graphics.Typeface#NORMAL}</li>
     *  <li>{@link android.graphics.Typeface#ITALIC}</li>
     *  <li>{@link android.graphics.Typeface#BOLD}</li>
     *  <li>{@link android.graphics.Typeface#BOLD_ITALIC}</li>
     * </ul>
     *
     * @param style          the style of the text
     *
     * @return               this object
     */
    public ViewManager setStyle(int style)
    {
        if (this.textView == null)
        {
            try {this.activity.logger.warning("Cannot set text style in: ", this.view.getId(), ", because there is no text in this view!");}
            catch (Exception e) {System.err.println(BasicUtils.getStackTraceAsString(e));}
        }
        else this.parent.post(() -> textView.setTypeface(textView.getTypeface(), style));

        return this;
    }

    /**
     * Sets the text size of the view in pixels. If the view does not contain
     * text, logs a warning. The text size is set in the UI thread.
     *
     * @param size              the size of the text in pixels
     *
     * @return                  this object
     */
    public ViewManager setTextSize(double size)
    {
        if (this.textView == null)
        {
            try {this.activity.logger.warning("Cannot set text size in: ", this.view.getId(), ", because there is no text in this view!");}
            catch (Exception e) {System.err.println(BasicUtils.getStackTraceAsString(e));}
        }
        else this.parent.post(() -> this.textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float)(size)));
        return this;
    }

    /**
     * Sets the size of the text in the view, given as a bias of the
     * screen width. The algorithm works by doing a binary search to find
     * the ideal text size that makes the text width closest to the target
     * width. The text size is set in the UI thread.
     *
     * @param bias              the bias of the screen width
     *
     * @return                  this object
     */
    public ViewManager setTextSizeBias(double bias)
    {
        if (this.textView == null)
        {
            try {this.activity.logger.warning("Cannot set text size in: ", this.view.getId(), ", because there is no text in this view!");}
            catch (Exception e) {System.err.println(BasicUtils.getStackTraceAsString(e));}
        }
        else
        {
            this.parent.post(() ->
            {
                final String content = this.textView.getText().toString();
                final double targetWidth = this.activity.screenWidth * bias;

                final Paint paint = new Paint();
                paint.setTypeface(this.textView.getTypeface());
                paint.setAntiAlias(true);

                float low = 1.0f;
                float high = 500.0f;
                float tolerance = 0.2f;
                float currentSize = 40.0f;

                for (int i = 0; i < 15; i++)
                {
                    currentSize = (low + high) / 2;
                    paint.setTextSize(currentSize);
                    float measuredWidth = paint.measureText(content);

                    if (Math.abs(measuredWidth - targetWidth) < tolerance) break;
                    else if (measuredWidth < targetWidth) low = currentSize;
                    else high = currentSize;
                }

                this.textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, currentSize);
            });
        }

        return this;
    }

    /**
     * Sets the text color of the view, given as a color resource.
     * The resource can be a color value or a color state list.
     * Supports TextView, EditText, Button.
     * 
     * @param color             the color resource
     * @return                  this object
     */
    public ViewManager setTextColor(int color)
    {
        if (this.textView == null)
        {
            try {this.activity.logger.warning("Cannot set text color in: ", this.view.getId(), ", because there is no text in this view!");}
            catch (Exception e) {System.err.println(BasicUtils.getStackTraceAsString(e));}
        }
        else this.parent.post(() -> this.textView.setTextColor(color));

        return this;
    }

    /**
     * Sets the text color of the view, given as the red, green and blue components of the color.
     * Supports TextView, EditText, Button.
     * 
     * @param r                the red component of the color
     * @param g                the green component of the color
     * @param b                the blue component of the color
     * @return                 this object
     */
    public ViewManager setTextColor(int r, int g, int b)
    {
        if (this.textView == null)
        {
            try {this.activity.logger.warning("Cannot set text color in: ", this.view.getId(), ", because there is no text in this view!");}
            catch (Exception e) {System.err.println(BasicUtils.getStackTraceAsString(e));}
        }
        else this.parent.post(() -> this.textView.setTextColor(Color.rgb(r, g, b)));

        return this;
    }

    /**
     * Sets the typeface of the text in the view using the specified font family name.
     * The font is loaded from the assets/fonts directory with a .ttf extension.
     * Supports TextView, EditText, Button.
     *
     * @param name              the name of the font family to apply
     * @return                  this object
     */
    public ViewManager setTextFamily(String name)
    {
        if (this.textView == null)
        {
            try {this.activity.logger.warning("Cannot translate content in: ", this.view.getId(), ", because there is no text in this view!");}
            catch (Exception e) {System.err.println(BasicUtils.getStackTraceAsString(e));}
        }
        else this.parent.post(() -> textView.setTypeface(Typeface.createFromAsset(activity.getAssets(), BasicUtils.combined("fonts/", name, ".ttf"))));

        return this;
    }

    /**
     * Sets the text of the view using a string resource ID. Retrieves the string
     * associated with the specified resource ID and assigns it to the text field.
     * This method is designed to be called on views that contain text, such as
     * TextView, EditText, or Button.
     * 
     * @param id                the resource ID of the string to set as text
     * @return                  this object
     */
    public ViewManager setText(int id)
    {
        if (this.textView == null)
        {
            try {this.activity.logger.warning("Cannot translate content in: ", this.view.getId(), ", because there is no text in this view!");}
            catch (Exception e) {System.err.println(BasicUtils.getStackTraceAsString(e));}
        }
        else this.text = this.activity.resources.getString(id);

        return this;
    }

    /**
     * Formats the text of the view, given as a format string and the arguments to format.
     * Supports TextView, EditText, Button.
     * 
     * @param args             the arguments to format
     * @return                 this object
     */
    public ViewManager formatText(final Object... args)
    {
        if (this.textView == null)
        {
            try {this.activity.logger.warning("Cannot translate content in: ", this.view.getId(), ", because there is no text in this view!");}
            catch (Exception e) {System.err.println(BasicUtils.getStackTraceAsString(e));}
        }
        else this.text = String.format(this.text, args);

        return this;
    }

    /**
     * Translates the text of the view to the current language.
     * Supports TextView, EditText, Button.
     * 
     * @return                  this object
     */
    public ViewManager translateText()
    {
        if (this.textView == null)
        {
            try {this.activity.logger.warning("Cannot translate content in: ", this.view.getId(), ", because there is no text in this view!");}
            catch (Exception e) {System.err.println(BasicUtils.getStackTraceAsString(e));}
        }
        else this.text = I18n.format(text);

        return this;
    }

    /**
     * Applies the changes to the view. This method should be called after
     * any of the methods in this class are called to actually update the
     * view's position or size.
     */
    public void applyChange()
    {
        if (this.clParams == null) this.view.setLayoutParams(this.params);
        else this.view.setLayoutParams(this.clParams);

        if (this.textView != null)
            this.textView.setText(this.text);

        this.view.requestLayout();
    }
}