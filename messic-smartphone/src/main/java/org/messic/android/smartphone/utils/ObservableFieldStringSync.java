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
package org.messic.android.smartphone.utils;

import android.databinding.ObservableField;
import android.text.Editable;
import android.text.TextWatcher;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;
import org.parceler.ParcelProperty;

/**
 * Utility class to sync the observable fields for binding.
 * (google please, improve this!)
 */
@Parcel
public class ObservableFieldStringSync implements TextWatcher {

    @ParcelProperty("field")
    ObservableField<String> field;

    @ParcelConstructor
    public ObservableFieldStringSync(@ParcelProperty("field") ObservableField<String> field) {
        this.field = field;
    }

    public ObservableField<String> getField() {
        return field;
    }

    public void setField(ObservableField<String> field) {
        this.field = field;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        field.set(s.toString());
    }
}
