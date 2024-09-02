package com.example.mytvapplication;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class JokeBoxView extends View {
    private String joke = "";

    public JokeBoxView(Context context) {
        super(context);
    }

    public JokeBoxView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public JokeBoxView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setJoke(String joke) {
        this.joke = joke;
        invalidate(); // Request a redraw to display the joke
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Create a Paint object for drawing the text
        Paint paint = new Paint();
        paint.setColor(Color.BLACK); // Set text color
        paint.setTextSize(14 * getResources().getDisplayMetrics().density); // Set text size
        paint.setTextAlign(Paint.Align.CENTER); // Center-align the text horizontally

        // Get the width and height of the View
        int viewWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int viewHeight = getHeight() - getPaddingTop() - getPaddingBottom();

        // Break the joke into lines that fit within the view width
        List<String> lines = new ArrayList<>();
        String[] words = joke.split(" ");
        StringBuilder lineBuilder = new StringBuilder();

        for (String word : words) {
            if (paint.measureText(lineBuilder.toString() + " " + word) <= viewWidth) {
                lineBuilder.append(word).append(" ");
            } else {
                lines.add(lineBuilder.toString());
                lineBuilder = new StringBuilder(word).append(" ");
            }
        }
        lines.add(lineBuilder.toString()); // Add the last line

        // Calculate the total height of the text block (all lines combined)
        float textBlockHeight = lines.size() * (paint.descent() - paint.ascent());

        // Calculate the vertical position to start drawing the text block so it is centered
        float yPos = getPaddingTop() + (viewHeight / 2f) - (textBlockHeight / 2f) - paint.ascent();

        // Draw each line of text centered horizontally
        for (String line : lines) {
            canvas.drawText(line.trim(), getWidth() / 2f, yPos, paint); // Center horizontally using getWidth() / 2f
            yPos += paint.descent() - paint.ascent(); // Move to the next line
        }
    }

}

