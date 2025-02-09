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
package miui.app;

import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

public class MiuiFreeFormManager {
    public static final class MiuiFreeFormStackInfo implements Parcelable {
        public static final Creator<MiuiFreeFormStackInfo> CREATOR = new Creator<>() {
            @Override
            public MiuiFreeFormStackInfo createFromParcel(Parcel source) {
                throw new RuntimeException("Stub!");
            }

            @Override
            public MiuiFreeFormStackInfo[] newArray(int size) {
                throw new RuntimeException("Stub!");
            }
        };
        public Rect bounds;
        public Configuration configuration;
        public int cornerPosition;
        public int displayId;
        public Rect enterMiniFreeformFromRect;
        public String enterMiniFreeformReason;
        public float freeFormScale;
        public boolean hadHideStackFormFullScreen;
        public boolean inPinMode;
        public boolean isForegroundPin;
        public boolean isLandcapeFreeform;
        public boolean isNormalFreeForm;
        public boolean needAnimation;
        public String packageName;
        public Rect pinFloatingWindowPos;
        public Rect smallWindowBounds;
        public int stackId;
        public long timestamp;
        public int userId;
        public int windowState;

        public void setTo(MiuiFreeFormStackInfo other) {
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

        public void readFromParcel(Parcel source) {
            throw new RuntimeException("Stub!");
        }

        public MiuiFreeFormStackInfo() {
            throw new RuntimeException("Stub!");
        }

        private MiuiFreeFormStackInfo(Parcel source) {
            throw new RuntimeException("Stub!");
        }

        public String toString(String prefix) {
            throw new RuntimeException("Stub!");
        }

        public boolean isInMiniFreeFormMode() {
            throw new RuntimeException("Stub!");
        }

        public boolean isInFreeFormMode() {
            throw new RuntimeException("Stub!");
        }

        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }
}
