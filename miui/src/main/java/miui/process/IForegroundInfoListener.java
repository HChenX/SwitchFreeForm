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

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IForegroundInfoListener extends IInterface {
    String DESCRIPTOR = "miui.process.IForegroundInfoListener";

    void onForegroundInfoChanged(ForegroundInfo foregroundInfo) throws RemoteException;


    class Default implements IForegroundInfoListener {
        @Override
        public void onForegroundInfoChanged(ForegroundInfo foregroundInfo) throws RemoteException {
            throw new RuntimeException("Stub!");
        }

        @Override
        public IBinder asBinder() {
            throw new RuntimeException("Stub!");
        }
    }


    abstract class Stub extends Binder implements IForegroundInfoListener {
        static final int TRANSACTION_onForegroundInfoChanged = 1;

        public Stub() {
            throw new RuntimeException("Stub!");
        }

        public static IForegroundInfoListener asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public IBinder asBinder() {
            throw new RuntimeException("Stub!");
        }

        public static String getDefaultTransactionName(int transactionCode) {
            throw new RuntimeException("Stub!");
        }


        public String getTransactionName(int transactionCode) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            throw new RuntimeException("Stub!");
        }


        private static class Proxy implements IForegroundInfoListener {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                throw new RuntimeException("Stub!");
            }

            @Override
            public IBinder asBinder() {
                throw new RuntimeException("Stub!");
            }

            public String getInterfaceDescriptor() {
                throw new RuntimeException("Stub!");
            }

            @Override
            public void onForegroundInfoChanged(ForegroundInfo foregroundInfo) throws RemoteException {
                throw new RuntimeException("Stub!");
            }
        }

        public int getMaxTransactionId() {
            throw new RuntimeException("Stub!");
        }
    }
}
