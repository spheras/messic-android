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
package org.messic.android.smartphone.activities;

/**
 * Abstract presenter to work as base for every presenter created in the application. This
 * presenter
 * declares some methods to attach the fragment/activity lifecycle.
 *
 * @author Pedro Vicente Gómez Sánchez
 */
public interface Presenter {

  /**
   * Called when the presenter is initialized, this method represents the start of the presenter
   * lifecycle.
   */
  void initialize();

  /**
   * Called when the presenter is resumed. After the initialization and when the presenter comes
   * from a pause state.
   */
  void resume();

  /**
   * Called when the presenter is paused.
   */
  void pause();
}
