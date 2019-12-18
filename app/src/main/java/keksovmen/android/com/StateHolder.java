package keksovmen.android.com;

import android.content.Context;

import androidx.fragment.app.FragmentManager;

import com.Abstraction.Client.LogicObserver;

public interface StateHolder {

    LogicObserver getObserver();

    Context getContext();

    FragmentManager getManager();
}
