/*
 * This file is part of SwitchFreeForm.

 * SwitchFreeForm is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2023-2025 HChenX
 */
package miui.process;

import android.os.Parcel;
import android.os.Parcelable;

public class ForegroundInfo implements Parcelable {
    public static final Creator<ForegroundInfo> CREATOR = new Creator<ForegroundInfo>() {
        @Override
        public ForegroundInfo createFromParcel(Parcel in) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public ForegroundInfo[] newArray(int size) {
            throw new RuntimeException("Stub!");
        }
    };
    public static final int FLAG_FOREGROUND_COLD_START = 1;
    public int mFlags;
    public String mForegroundPackageName;
    public int mForegroundPid;
    public int mForegroundUid;
    public String mLastForegroundPackageName;
    public int mLastForegroundPid;
    public int mLastForegroundUid;
    public String mMultiWindowForegroundPackageName;
    public int mMultiWindowForegroundUid;

    public ForegroundInfo() {
        throw new RuntimeException("Stub!");
    }

    public ForegroundInfo(ForegroundInfo origin) {
        throw new RuntimeException("Stub!");
    }

    private ForegroundInfo(Parcel in) {
        throw new RuntimeException("Stub!");
    }

    @Override
    public int describeContents() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        throw new RuntimeException("Stub!");
    }

    public boolean isColdStart() {
        throw new RuntimeException("Stub!");
    }

    public void addFlags(int flags) {
        throw new RuntimeException("Stub!");
    }

    public void resetFlags() {
        throw new RuntimeException("Stub!");
    }

    public String toString() {
        throw new RuntimeException("Stub!");
    }
}
