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
package org.messic.android.utils;

import android.databinding.ObservableField;
import android.widget.CompoundButton;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;
import org.parceler.ParcelProperty;

/**
 * Utility class to sync the observable fields for binding.
 * (google please, improve this!)
 */
@Parcel
public class ObservableFieldCheckSync implements CompoundButton.OnCheckedChangeListener {

    @ParcelProperty("field")
    ObservableField<Boolean> field;

    @ParcelConstructor
    public ObservableFieldCheckSync(@ParcelProperty("field") ObservableField<Boolean> field) {
        this.field = field;
    }

    public ObservableField<Boolean> getField() {
        return field;
    }

    public void setField(ObservableField<Boolean> field) {
        this.field = field;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        field.set(isChecked);
    }
}
