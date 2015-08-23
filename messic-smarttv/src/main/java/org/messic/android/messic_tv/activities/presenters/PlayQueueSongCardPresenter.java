/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.messic.android.messic_tv.activities.presenters;

import android.support.v17.leanback.widget.ImageCardView;
import android.view.ViewGroup;

import org.messic.android.messiccore.util.UtilMusicPlayer;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand. 
 * It contains an Image CardView
 */
public class PlayQueueSongCardPresenter extends SongCardPresenter {

    protected void addWidgetsToCardView(ViewGroup parent, ImageCardView cardView) {
        int cursor = UtilMusicPlayer.getCursor(mContext);
    }
}
