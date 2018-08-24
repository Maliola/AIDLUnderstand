// IOnNewBookArrivedListeners.aidl
package com.mahui.aidlunderstand;
import com.mahui.aidlunderstand.Book;
// Declare any non-default types here with import statements

interface IOnNewBookArrivedListener {
    void onNewBookArrived(in Book newBook);
}
