package com.mahui.aidlunderstand.service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.mahui.aidlunderstand.Book;
import com.mahui.aidlunderstand.IBookManager;
import com.mahui.aidlunderstand.IOnNewBookArrivedListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by 76047 on 2018/8/20.
 */

public class BookManagerService extends Service {

    private static final String TAG="BMS";
    int count = 0;
    private AtomicBoolean mIsServiceDestoryed = new AtomicBoolean(false);
    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<Book>();
    private RemoteCallbackList<IOnNewBookArrivedListener> mListenerList = new RemoteCallbackList<IOnNewBookArrivedListener>();

    private Binder mBinder = new IBookManager.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public List<Book> getBookList() throws RemoteException {
            return mBookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            mBookList.add(book);
        }

        @Override
        public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
            mListenerList.register(listener);
            count++;
            Log.d(TAG,"registerListener size:"+count);
        }

        @Override
        public void unregisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
            mListenerList.unregister(listener);
            count--;
            Log.d(TAG,"unregister listener succeed size:"+count);
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();
        mBookList.add(new Book(1,"Android"));
        mBookList.add(new Book(2,"Ios"));
        new Thread(new ServiceWorker()).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        /*int check = checkCallingOrSelfUriPermission(Uri.parse("com.mahui.aidlunderstand.ACCESS_BOOKSERVICE"),PackageManager.PERMISSION_GRANTED);
        Log.e(TAG,"参数："+check);
        if(check == PackageManager.PERMISSION_DENIED){
            return null;
        }*/
        return mBinder;
    }

    @Override
    public void onDestroy() {
        mIsServiceDestoryed.set(true);
        super.onDestroy();
    }

    private class ServiceWorker implements Runnable{
        @Override
        public void run() {
            while (!mIsServiceDestoryed.get()){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int bookId = mBookList.size() + 1;
                Book newBook = new Book(bookId,"new book#"+bookId);
                try {
                    onNewBookArrived(newBook);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void onNewBookArrived(Book book) throws RemoteException{
        mBookList.add(book);
        final int N = mListenerList.beginBroadcast();
        for(int i = 0; i < N;i++){
            IOnNewBookArrivedListener listener = mListenerList.getBroadcastItem(i);
            if(listener != null){
                try {
                    listener.onNewBookArrived(book);
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
            mListenerList.finishBroadcast();
        }
    }
}
