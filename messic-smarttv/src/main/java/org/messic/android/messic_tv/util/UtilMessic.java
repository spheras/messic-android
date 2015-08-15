package org.messic.android.messic_tv.util;

import android.content.Context;

import org.messic.android.messic_tv.R;
import org.messic.android.messiccore.datamodel.MDMRandomList;

/**
 * Created by spheras on 30/07/15.
 */
public class UtilMessic {

    public static String getRandomTitle(Context ctx, MDMRandomList rl) {
        if (rl.getName().equals("RandomListName-Random")) {
            return ctx.getString(R.string.RandomListNameRandom);
        } else if (rl.getName().equals("RandomListName-Author")) {
            return ctx.getString(R.string.RandomListNameAuthor);
        } else if (rl.getName().equals("RandomListName-Date")) {
            return ctx.getString(R.string.RandomListNameDate);
        } else if (rl.getName().equals("RandomListName-Genre")) {
            return ctx.getString(R.string.RandomListNameGenre);
        } else if (rl.getName().equals("RandomListName-LessPlayed")) {
            return ctx.getString(R.string.RandomListNameLessPlayed);
        } else if (rl.getName().equals("RandomListName-Loved")) {
            return ctx.getString(R.string.RandomListNameLoved);
        } else if (rl.getName().equals("RandomListName-MostPlayed")) {
            return ctx.getString(R.string.RandomListNameMostPlayed);
        } else if (rl.getName().equals("RandomListName-Recent")) {
            return ctx.getString(R.string.RandomListNameRecent);
        }
        return "unknown";
    }

}
