package geonet.obd.reader.activity;

/**
 * Created by antonio on 16/03/17.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;

import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.internal.util.Stopwatch;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import roboguice.RoboGuice;
import roboguice.activity.event.OnActivityResultEvent;
import roboguice.activity.event.OnContentChangedEvent;
import roboguice.activity.event.OnNewIntentEvent;
import roboguice.activity.event.OnPauseEvent;
import roboguice.activity.event.OnRestartEvent;
import roboguice.activity.event.OnResumeEvent;
import roboguice.activity.event.OnSaveInstanceStateEvent;
import roboguice.activity.event.OnStopEvent;
import roboguice.context.event.OnConfigurationChangedEvent;
import roboguice.context.event.OnCreateEvent;
import roboguice.context.event.OnDestroyEvent;
import roboguice.context.event.OnStartEvent;
import roboguice.event.EventManager;
import roboguice.inject.ContentViewListener;
import roboguice.inject.RoboInjector;
import roboguice.util.RoboContext;

/**

 Created by ajayk on 8/20/2016.
 */
public class RoboAppCompatActivity extends AppCompatActivity implements RoboContext {
    protected EventManager eventManager;
    protected HashMap<Key<?>,Object> scopedObjects = new HashMap<Key<?>, Object>();

    @Inject
    ContentViewListener ignored; // BUG find a better place to put this
    private Stopwatch stopwatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        stopwatch = new Stopwatch();
        final RoboInjector injector = RoboGuice.getInjector(this);
        stopwatch.resetAndLog("RoboActivity creation of injector");
        eventManager = injector.getInstance(EventManager.class);
        stopwatch.resetAndLog("RoboActivity creation of eventmanager");
        injector.injectMembersWithoutViews(this);
        stopwatch.resetAndLog("RoboActivity inject members without views");
        super.onCreate(savedInstanceState);
        stopwatch.resetAndLog("RoboActivity super onCreate");
        eventManager.fire(new OnCreateEvent(this,savedInstanceState));
        stopwatch.resetAndLog("RoboActivity fire event");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        eventManager.fire(new OnSaveInstanceStateEvent(this, outState));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        eventManager.fire(new OnRestartEvent(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        eventManager.fire(new OnStartEvent(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        eventManager.fire(new OnResumeEvent(this));
    }

    @Override
    protected void onPause() {
        super.onPause();
        eventManager.fire(new OnPauseEvent(this));
    }

    @Override
    protected void onNewIntent( Intent intent ) {
        super.onNewIntent(intent);
        eventManager.fire(new OnNewIntentEvent(this));
    }

    @Override
    protected void onStop() {
        try {
            eventManager.fire(new OnStopEvent(this));
        } finally {
            super.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            eventManager.fire(new OnDestroyEvent(this));
        } finally {
            try {
                RoboGuice.destroyInjector(this);
            } finally {
                super.onDestroy();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        final Configuration currentConfig = getResources().getConfiguration();
        super.onConfigurationChanged(newConfig);
        eventManager.fire(new OnConfigurationChangedEvent(this,currentConfig, newConfig));
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        RoboGuice.getInjector(this).injectViewMembers(this);
        eventManager.fire(new OnContentChangedEvent(this));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        eventManager.fire(new OnActivityResultEvent(this, requestCode, resultCode, data));
    }

    @Override
    public Map<Key<?>, Object> getScopedObjectMap() {
        return scopedObjects;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        if (RoboAppCompatActivity.shouldInjectOnCreateView(name))
            return RoboAppCompatActivity.injectOnCreateView(name, context, attrs);

        return super.onCreateView(name, context, attrs);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        if (RoboAppCompatActivity.shouldInjectOnCreateView(name))
            return RoboAppCompatActivity.injectOnCreateView(name, context, attrs);

        return super.onCreateView(parent, name, context, attrs);
    }

    /**

     @return true if name begins with a lowercase character (indicating a package) and it doesn't start with com.android
     */
    protected static boolean shouldInjectOnCreateView(String name) {
        return false; // && Character.isLowerCase(name.charAt(0)) && !name.startsWith("com.android") && !name.equals("fragment");
    }
    protected static View injectOnCreateView(String name, Context context, AttributeSet attrs) {
        try {
            final Constructor<?> constructor = Class.forName(name).getConstructor(Context.class, AttributeSet.class);
            final View view = (View) constructor.newInstance(context, attrs);
            RoboGuice.getInjector(context).injectMembers(view);
            RoboGuice.getInjector(context).injectViewMembers(view);
            return view;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}