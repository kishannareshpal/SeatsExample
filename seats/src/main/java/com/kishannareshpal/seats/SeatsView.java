package com.kishannareshpal.seats;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.method.HideReturnsTransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SeatsView extends ScrollView {

    Resources resources;
    private Context ctx;

    // Components
    TableLayout tableLayout;

    // Setters
    private String seatsPattern = "";
    private List<String> reservationsList = null;

    private List<String> selectedSeatsList = new ArrayList<>();
    private int maxAllowedSelection = -1; // unlimited by default.
    private int numberOfSeats = -1;

    /**
     * Interfaces
     */
    public interface OnSeatClickListener {
        void OnSeatClick(int seatId, boolean isSelected, List<String> selectedSeatsList);
    }

    /**
     * Constructors
     */
    public SeatsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.ctx = context;
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet set) {
        inflate(context, R.layout.seats, this);
        this.resources = getResources();
        this.tableLayout = findViewById(R.id.tablelayout);

        if (set != null){
            // custom xml attributes were set.
            TypedArray typedArray = context.obtainStyledAttributes(set, R.styleable.SeatsView);
            maxAllowedSelection = typedArray.getInteger(R.styleable.SeatsView_maxAllowedSelection, -1);
            typedArray.recycle();
        }
    }

    /**
     * Setters
     */
    // Use this to generate and show the seats on your layout.
    public SeatsView show(String seatsPattern, List<String> reservationsList, @Nullable OnSeatClickListener onSeatClickListener) {
        this.seatsPattern = seatsPattern;
        this.reservationsList = reservationsList;
        build(seatsPattern, reservationsList, onSeatClickListener);
        return this;
    }

    public SeatsView setMaxAllowedSelection(int maxAllowedSelection) {
        this.maxAllowedSelection = maxAllowedSelection;
        return this;
    }

    /**
     * Getters
     */
    public List<String> getSelectedSeats() {
        // return a list of string of selected seats id.
        return this.selectedSeatsList;
    }

    public int getNumberOfSeats() {
        // return the total number of seats.
        return this.numberOfSeats;
    }


    /**
     * Generates the seats according to the pattern provided.
     * @see SeatPatternChars to get an idea of the characters used to compose your pattern;
     */
    private void build(String pattern, List<String> reservations, @Nullable final OnSeatClickListener onSeatClickListener) {
        // 1st: update the provided pattern to contain the current reservations to contain the reservations.
        StringBuilder patternWithReservations = new StringBuilder(pattern);
        int position = -1;
        for (int i = 0; i < pattern.length(); i++) {
            // go through the pattern from left to right.
            String character = pattern.substring(i, i+1); // current character
            if (character.equals(SeatPatternChars.AVAILABLE)) {
                // update the position counter.
                position += 1;
                if (reservations.contains(String.valueOf(position))) {
                    // replaces the SeatPattern.Available at the index, with SeatPattern.RESERVED.
                    patternWithReservations.replace(i, i + 1, SeatPatternChars.RESERVED);
                }
            }
        }

        this.numberOfSeats = position + 1; // set the number of seats.

        // 2nd: Create the seats view based on the pattern with reservations provided.
        // 110X1,X101X,11011,11011,11011
        String[] items = patternWithReservations.toString().split(SeatPatternChars.NEW_ROW);

        int id = -1;
        for (String seatRowPattern: items) {
            TableRow seatRow = new TableRow(ctx);
            seatRow.setGravity(Gravity.CENTER);

            for (int i = 0; i < seatRowPattern.length(); i++) {
                String patt = seatRowPattern.substring(i, i+1);

                float sixDp = Utilities.convertDpToPixel(6, ctx);
                int thirtySix = resources.getDimensionPixelSize(R.dimen.thirty_six);
                TableRow.LayoutParams params = new TableRow.LayoutParams(thirtySix, thirtySix);

                switch (patt) {
                    case SeatPatternChars.DRIVER:
                        final Seat drv_seat = new Seat(ctx, true);
                        params.setMargins((int) sixDp, (int) sixDp, (int) sixDp, (int) sixDp);
                        seatRow.addView(drv_seat, params);
                        break;

                    case SeatPatternChars.RESERVED:
                        // reserved seat. unselectable.
                        id += 1;
                        final Seat res_seat = new Seat(ctx);
                        params.setMargins((int) sixDp, (int) sixDp, (int) sixDp, (int) sixDp);
                        res_seat.setAlpha(.2F);
                        seatRow.addView(res_seat, params);
                        break;

                    case SeatPatternChars.AVAILABLE:
                        // available seats
                        id += 1;
                        final Seat av_seat = new Seat(ctx);
                        av_seat.setSeatId(id);
                        params.setMargins((int) sixDp, (int) sixDp, (int) sixDp, (int) sixDp);
                        seatRow.addView(av_seat, params);

                        // click listener
                        av_seat.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // don't allow selections if maximum number of selectable seats has reached.
                                av_seat.toggleSelected(maxAllowedSelection, selectedSeatsList.size() + 1);
                                int seatId = av_seat.getSeatId();
                                boolean isSeatSelected = av_seat.isSelected();

                                if (isSeatSelected) {
                                    // add to the selected list.
                                    if ( (selectedSeatsList != null) && (!selectedSeatsList.contains(String.valueOf(seatId))) ) {
                                        selectedSeatsList.add(String.valueOf(seatId));
                                    }

                                } else {
                                    if (selectedSeatsList != null) {
                                        selectedSeatsList.remove(String.valueOf(seatId));
                                    }
                                }

                                if (onSeatClickListener != null) {
                                    onSeatClickListener.OnSeatClick(seatId, isSeatSelected, selectedSeatsList);
                                }
                            }
                        });
                        break;

                    case SeatPatternChars.EMPTY:
                        // empty space
                        Space spaceX = new Space(ctx);
                        seatRow.addView(spaceX, resources.getDimensionPixelSize(R.dimen.thirty_six), 0);
                        break;
                }
            }

            tableLayout.addView(seatRow);
        }
    }


    // Complementary functions
//    void createDriverRow() {
//        TableRow driverRow = new TableRow(ctx);
//        driverRow.setGravity(Gravity.CENTER);
//
//        float sixDp = Utilities.convertDpToPixel(6, ctx);
//        int thirtySix = resources.getDimensionPixelSize(R.dimen.thirty_six);
//        TableRow.LayoutParams params = new TableRow.LayoutParams(thirtySix, thirtySix);
//        params.setMargins((int) sixDp, (int) sixDp, (int) sixDp, (int) sixDp);
//
//        // add to driverRow
//        for (int i = 0; i < 3; i++) {
//            Space space = new Space(ctx);
//            driverRow.addView(space, params);
//        }
//
//        // steering wheel icon.
//        Seat seatview = new Seat(ctx);
//        driverRow.addView(seatview, params);
//        tableLayout.addView(driverRow);
//    }
//
//    void createSeatsRow() {
//        TableRow seatRow = new TableRow(ctx);
//        seatRow.setGravity(Gravity.CENTER);
//
//        for (int i = 0; i < 2; i++) {
//            final Seat seat = new Seat(ctx);
//            float sixDp = Utilities.convertDpToPixel(6, ctx);
//            int thirtySix = resources.getDimensionPixelSize(R.dimen.thirty_six);
//            TableRow.LayoutParams params = new TableRow.LayoutParams(thirtySix, thirtySix);
//            params.setMargins((int) sixDp, (int) sixDp, (int) sixDp, (int) sixDp);
//            seatRow.addView(seat, params);
//
//            seat.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    seat.toggleSeatMode();
//                }
//            });
//
//        }
//
//        Space spaceX = new Space(ctx);
//        seatRow.addView(spaceX, resources.getDimensionPixelSize(R.dimen.thirty_six), 0);
//
//        for (int i = 0; i < 2; i++) {
//            Seat seat = new Seat(ctx);
//            float sixDp = Utilities.convertDpToPixel(6, ctx);
//            int thirtySix = resources.getDimensionPixelSize(R.dimen.thirty_six);
//            TableRow.LayoutParams params = new TableRow.LayoutParams(thirtySix, thirtySix);
//            params.setMargins((int) sixDp, (int) sixDp, (int) sixDp, (int) sixDp);
//            seatRow.addView(seat, params);
//        }
//
//        tableLayout.addView(seatRow);
//    }
}
