package org.messic.android.smartphone.activities.searchmessicservice;


import android.test.suitebuilder.annotation.LargeTest;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMMessicServerInstance;
import org.messic.android.messiccore.datamodel.dao.DAOServerInstance;
import org.messic.android.messiccore.util.UtilDatabase;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
@LargeTest
public class SearchMessicServicePresenterImplTest {

    private static SearchMessicServicePresenterImpl impl;

    @BeforeClass
    public static void setup() {
        impl = new SearchMessicServicePresenterImpl();
        impl.smsevents = Mockito.mock(SearchMessicServiceEvents.class);
        impl.config = Mockito.mock(Configuration.class);
        impl.daoServerInstance = Mockito.mock(DAOServerInstance.class);
        impl.ud = Mockito.mock(UtilDatabase.class);
    }

    @Test
    public void testSelectLayout() {
        resetMock();

        Mockito.when(impl.ud.checkEmptyDatabase()).thenReturn(true);
        SearchMessicServicePresenter.ShowControl sc = impl.selectLayout();
        Assert.assertTrue(!sc.showLoginOffline);

        Mockito.when(impl.ud.checkEmptyDatabase()).thenReturn(false);
        sc = impl.selectLayout();
        Assert.assertTrue(sc.showLoginOffline);
    }

    private void resetMock() {
        Mockito.reset(impl.smsevents);
        Mockito.reset(impl.config);
        Mockito.reset(impl.daoServerInstance);
        Mockito.reset(impl.ud);
    }

    @Test
    public void testIsSwipeable() {
        resetMock();
        MDMMessicServerInstance msi = new MDMMessicServerInstance();
        msi.setLastCheckedStatus(MDMMessicServerInstance.STATUS_DOWN);
        boolean result = impl.isSwipeable(msi);
        Assert.assertTrue(result);

        msi.setLastCheckedStatus(MDMMessicServerInstance.STATUS_UNKNOWN);
        result = impl.isSwipeable(msi);
        Assert.assertTrue(result);

        msi.setLastCheckedStatus(MDMMessicServerInstance.STATUS_RUNNING);
        result = impl.isSwipeable(msi);
        Assert.assertTrue(!result);
    }

    @Test
    public void testSwipe() {
        resetMock();
        MDMMessicServerInstance msi = new MDMMessicServerInstance();
        impl.swipe(msi);
        Mockito.verify(impl.daoServerInstance, Mockito.times(1)).remove(msi);
    }


    @Test
    public void testLoginOfflineAction() {
        resetMock();

        impl.loginOfflineAction();

        Mockito.verify(impl.config, Mockito.times(1)).setOffline(true);
    }
}
