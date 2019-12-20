package keksovmen.android.com.Util;

import android.widget.TabHost;

import com.Abstraction.Client.LogicObserver;
import com.Abstraction.Pipeline.ACTIONS;

public class PageHolder implements LogicObserver {

    private final TabHost.TabSpec content;
    private final String tabName;
    private final LogicObserver observer;

    private int indexOnPane;
    private boolean isDisplayUnreadMessage;

    public PageHolder(TabHost.TabSpec content, String tabName, LogicObserver observer, int indexOnPane, boolean isDisplayUnreadMessage) {
        this.content = content;
        this.tabName = tabName;
        this.observer = observer;
        this.indexOnPane = indexOnPane;
        this.isDisplayUnreadMessage = isDisplayUnreadMessage;
    }

    public PageHolder(TabHost.TabSpec content, String tabName, LogicObserver observer, int indexOnPane) {
        this(content, tabName, observer, indexOnPane, false);
    }

    /**
     * indexOnPane will be set explicitly, by container
     *
     * @param content  represent tab
     * @param tabName  name to display and find
     * @param observer what can do
     */

    public PageHolder(TabHost.TabSpec content, String tabName, LogicObserver observer) {
        this(content, tabName, observer, -1, false);
//        this.content = content;
//        this.tabName = tabName;
//        this.observer = observer;
    }

    @Override
    public void observe(ACTIONS actions, Object[] objects) {
        observer.observe(actions, objects);
    }

    public TabHost.TabSpec getContent() {
        return content;
    }

    public String getTabName() {
        return tabName;
    }

    public LogicObserver getObserver() {
        return observer;
    }

    public int getIndexOnPane() {
        return indexOnPane;
    }

    public boolean isDisplayUnreadMessage() {
        return isDisplayUnreadMessage;
    }

    public void setIndexOnPane(int indexOnPane) {
        this.indexOnPane = indexOnPane;
    }

    public void setDisplayUnreadMessage(boolean displayUnreadMessage) {
        isDisplayUnreadMessage = displayUnreadMessage;
    }

    @Override
    public String toString() {
        return "PageHolder{" +
                "content=" + content +
                ", tabName='" + tabName + '\'' +
                ", observer=" + observer +
                ", indexOnPane=" + indexOnPane +
                ", isDisplayUnreadMessage=" + isDisplayUnreadMessage +
                '}';
    }
}
