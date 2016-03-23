package org.messic.android.smartphone.activities.login;


import android.test.suitebuilder.annotation.LargeTest;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMLogin;
import org.messic.android.messiccore.datamodel.MDMMessicServerInstance;
import org.messic.android.messiccore.util.MessicPreferences;
import org.messic.android.messiccore.util.UtilDatabase;
import org.messic.android.messiccore.util.UtilNetwork;
import org.messic.android.messiccore.util.UtilRestJSONClient;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.util.MultiValueMap;

import rx.Scheduler;
import rx.functions.Action0;
import rx.functions.Action1;

@RunWith(JUnit4.class)
@LargeTest
public class LoginPresenterImplTest {

    private static LoginPresenterImpl impl;

    @BeforeClass
    public static void setup() {
        impl = new LoginPresenterImpl();
        impl.utilNetwork = Mockito.mock(UtilNetwork.class);
        impl.config = Mockito.mock(Configuration.class);
        impl.pref = Mockito.mock(MessicPreferences.class);
        impl.urj = Mockito.mock(UtilRestJSONClient.class);
        impl.ud = Mockito.mock(UtilDatabase.class);
        impl.le = Mockito.mock(LoginEvents.class);
    }

    private void resetMock() {
        Mockito.reset(impl.utilNetwork);
        Mockito.reset(impl.config);
        Mockito.reset(impl.pref);
        Mockito.reset(impl.urj);
        Mockito.reset(impl.ud);
        Mockito.reset(impl.le);
    }

    private MDMMessicServerInstance getTestServerInstance() {
        MDMMessicServerInstance msi = new MDMMessicServerInstance();
        msi.name = "test name";
        msi.description = "test description";
        msi.port = 666;
        msi.secured = true;
        msi.ip = "666.666.666.666";
        msi.version = "666";
        return msi;
    }

    @Test
    public void testSearchOnlineAction() {
        resetMock();
        impl.searchOnlineAction();
        Mockito.verify(impl.le, Mockito.times(1)).sendShowSearchServerMessicScreen();
    }

    @Test
    public void testLoginOfflineAction() {
        resetMock();
        impl.loginOfflineAction();
        Mockito.verify(impl.config, Mockito.times(1)).setOffline(true);

    }

    @Test
    public void testStatusOnlineAction() {
        resetMock();
        impl.statusOnlineAction();
        Mockito.verify(impl.le, Mockito.times(1)).sendShowSearchServerMessicScreen();
    }

    @Test
    public void testLoginAction() throws Exception {
        resetMock();
        MDMMessicServerInstance msi = getTestServerInstance();
        Mockito.when(impl.config.getCurrentMessicService()).thenReturn(msi);
        MDMLogin loginresp = new MDMLogin();
        loginresp.setMessic_token("1234567890");
        loginresp.setSuccess(true);
        loginresp.setTargetUrl("http://temporal.test");
        loginresp.setUserId("userid");
        Mockito.when(impl.urj.post(Mockito.anyString(), Mockito.any(MultiValueMap.class), Mockito.any(Class.class))).thenReturn(loginresp);

        boolean result = impl.loginAction(true, "hello", "bye");

        Assert.assertTrue(result);
        Mockito.verify(impl.pref, Mockito.times(1)).setRemember(true, "hello", "bye", msi);
        Mockito.verify(impl.config, Mockito.times(1)).setToken("1234567890");
        Mockito.verify(impl.config, Mockito.times(1)).setOffline(false);
        Mockito.verify(impl.le, Mockito.times(1)).sendShowMainScreen();

        //now we launch an error
        Mockito.reset(impl.le);
        Mockito.when(impl.urj.post(Mockito.anyString(), Mockito.any(MultiValueMap.class), Mockito.any(Class.class))).thenThrow(new Exception());
        result = impl.loginAction(true, "hello", "bye");
        Assert.assertTrue(!result);
        Mockito.verify(impl.le, Mockito.never()).sendShowMainScreen();
    }


    @Test
    public void testSelectLayout() {
        resetMock();
        LoginActivityBinding binding = Mockito.mock(LoginActivityBinding.class);

        //checking with all false
        LoginPresenter.ShowControl sc = impl.selectLayout(binding);
        Assert.assertTrue(!sc.showWelcomeActivity);
        Assert.assertTrue(!sc.showLoginOnline);
        Assert.assertTrue(sc.showSearchOnline);
        Assert.assertTrue(sc.showLoginOffline);
        Assert.assertTrue(!sc.showSearchActivity);
        Assert.assertNull(binding.getPassword());
        Assert.assertNull(binding.getUsername());
        Assert.assertTrue(!binding.getRemember());
        Assert.assertNull(binding.getServername());
        Assert.assertNull(binding.getServerip());

        //check showing welcome the first time
        Mockito.when(impl.config.isFirstTime()).thenReturn(true);
        sc = impl.selectLayout(binding);
        Assert.assertTrue(sc.showWelcomeActivity);
        Mockito.when(impl.config.isFirstTime()).thenReturn(false);

        //check with a last messic server used
        MDMMessicServerInstance msi = getTestServerInstance();
        Mockito.when(impl.config.getLastMessicServerUsed()).thenReturn(msi);
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Action1<UtilNetwork.MessicServerConnectionStatus> a1 = (Action1<UtilNetwork.MessicServerConnectionStatus>) invocation.getArguments()[2];
                UtilNetwork.MessicServerConnectionStatus status = new UtilNetwork.MessicServerConnectionStatus(true, false);
                a1.call(status);
                Mockito.verify(impl.le, Mockito.atLeastOnce()).sendServerStatus(status);
                return null;
            }
        }).when(impl.utilNetwork).checkMessicServerUpAndRunning(Mockito.any(MDMMessicServerInstance.class),
                Mockito.any(Scheduler.class),
                Mockito.any(Action1.class),
                Mockito.any(Action1.class),
                Mockito.any(Action0.class)
        );
        sc = impl.selectLayout(binding);

        Mockito.verify(impl.utilNetwork, Mockito.times(1)).checkMessicServerUpAndRunning(
                Mockito.any(MDMMessicServerInstance.class),
                Mockito.any(Scheduler.class),
                Mockito.any(Action1.class),
                Mockito.any(Action1.class),
                Mockito.any(Action0.class)
        );
        Assert.assertTrue(!sc.showWelcomeActivity);
        Assert.assertTrue(sc.showLoginOnline);
        Assert.assertTrue(!sc.showSearchOnline);
        Assert.assertTrue(sc.showLoginOffline);
        Assert.assertTrue(!sc.showSearchActivity);
        Assert.assertNull(binding.getPassword());
        Assert.assertNull(binding.getUsername());
        Assert.assertTrue(!binding.getRemember());
        Assert.assertNull(binding.getServername());
        Assert.assertNull(binding.getServerip());

        //now we check again only if the server is not reachable or something giving an error,
        //then the result is a no reachable and not running server status
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Action1<Throwable> a1 = (Action1<Throwable>) invocation.getArguments()[3];
                UtilNetwork.MessicServerConnectionStatus status = new UtilNetwork.MessicServerConnectionStatus(false, false);
                a1.call(new Throwable());
                Mockito.verify(impl.le, Mockito.atLeastOnce()).sendServerStatus(Mockito.eq(status));
                return null;
            }
        }).when(impl.utilNetwork).checkMessicServerUpAndRunning(Mockito.any(MDMMessicServerInstance.class),
                Mockito.any(Scheduler.class),
                Mockito.any(Action1.class),
                Mockito.any(Action1.class),
                Mockito.any(Action0.class)
        );
        sc = impl.selectLayout(binding);

        //resetting method
        Mockito.doNothing().when(impl.utilNetwork).checkMessicServerUpAndRunning(Mockito.any(MDMMessicServerInstance.class),
                Mockito.any(Scheduler.class),
                Mockito.any(Action1.class),
                Mockito.any(Action1.class),
                Mockito.any(Action0.class)
        );


        //check with an empty database
        Mockito.when(impl.ud.checkEmptyDatabase()).thenReturn(true);
        sc = impl.selectLayout(binding);
        Assert.assertTrue(!sc.showWelcomeActivity);
        Assert.assertTrue(sc.showLoginOnline);
        Assert.assertTrue(!sc.showSearchOnline);
        Assert.assertTrue(!sc.showLoginOffline);
        Assert.assertTrue(!sc.showSearchActivity);
        Assert.assertNull(binding.getPassword());
        Assert.assertNull(binding.getUsername());
        Assert.assertTrue(!binding.getRemember());
        Assert.assertNull(binding.getServername());
        Assert.assertNull(binding.getServerip());


        //check with a current messic service and don't remember
        Mockito.when(impl.config.getCurrentMessicService()).thenReturn(msi);
        Mockito.when(impl.pref.getRemember()).thenReturn(false);
        sc = impl.selectLayout(binding);
        Assert.assertTrue(!sc.showWelcomeActivity);
        Assert.assertTrue(sc.showLoginOnline);
        Assert.assertTrue(!sc.showSearchOnline);
        Assert.assertTrue(!sc.showLoginOffline);
        Assert.assertTrue(!sc.showSearchActivity);
        Mockito.verify(binding).setServername(msi.name);
        Mockito.verify(binding).setServerip(msi.ip);
        Mockito.verify(binding).setRemember(false);
        Assert.assertNull(binding.getPassword());
        Assert.assertNull(binding.getUsername());

        //check with a current messic service and remember
        Mockito.reset(binding);
        Mockito.when(impl.pref.getRemember()).thenReturn(true);
        Mockito.when(impl.config.getLastMessicUser()).thenReturn("userTest");
        Mockito.when(impl.config.getLastMessicPassword()).thenReturn("passwordTest");
        sc = impl.selectLayout(binding);
        Assert.assertTrue(!sc.showWelcomeActivity);
        Assert.assertTrue(sc.showLoginOnline);
        Assert.assertTrue(!sc.showSearchOnline);
        Assert.assertTrue(!sc.showLoginOffline);
        Assert.assertTrue(!sc.showSearchActivity);
        Mockito.verify(binding).setServername(msi.name);
        Mockito.verify(binding).setServerip(msi.ip);
        Mockito.verify(binding).setRemember(true);
        Mockito.verify(binding).setPassword("passwordTest");
        Mockito.verify(binding).setUsername("userTest");
    }

}
