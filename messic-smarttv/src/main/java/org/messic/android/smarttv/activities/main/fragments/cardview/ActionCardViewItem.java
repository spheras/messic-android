package org.messic.android.smarttv.activities.main.fragments.cardview;

public abstract class ActionCardViewItem implements CardViewItem {

    static Object defaultAction = new Object();

    @Override
    public Object getItem() {
        return defaultAction;
    }

}
