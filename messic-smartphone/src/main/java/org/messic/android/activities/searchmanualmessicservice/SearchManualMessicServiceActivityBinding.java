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
package org.messic.android.activities.searchmanualmessicservice;

public interface SearchManualMessicServiceActivityBinding {
    String getName();

    void setName(String name);

    String getHostname();

    void setHostname(String hostname);

    String getPort();

    int getIntPort();

    void setPort(String port);

    Boolean getSecured();

    void setSecured(Boolean secured);

    String getDescription();

    void setDescription(String description);


}
