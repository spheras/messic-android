/*
 * Copyright (C) 2013
 *
 *  This file is part of Messic.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.messic.android.smarttv.views;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

public class FocusableRecyclerView extends RecyclerView implements View.OnFocusChangeListener, View.OnKeyListener {

    public FocusableRecyclerView(Context context) {
        super(context);
        init();
    }

    public FocusableRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FocusableRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setOnFocusChangeListener(this);
        setOnKeyListener(this);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            //we give the focus to the first element
            View first = getChildAt(0);
            if (first != null) {
                first.requestFocus();
            }
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
//        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
//            if (iFocus == 0) {
//                return false;
//            } else {
//                iFocus--;
//                View vfocus = getChildAt(iFocus);
//                vfocus.requestFocus();
//                return true;
//            }
//        }
//
//        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
//            if (iFocus == getChildCount()) {
//                return false;
//            } else {
//                iFocus++;
//                View vfocus = getChildAt(iFocus);
//                vfocus.requestFocus();
//                return true;
//            }
//        }

        return false;
    }
}
