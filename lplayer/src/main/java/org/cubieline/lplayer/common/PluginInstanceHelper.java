package org.cubieline.lplayer.common;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import org.cubieline.lplayer.CannotInstancePluginException;
import org.cubieline.lplayer.media.ILPlayerPlugin;
import org.cubieline.lplayer.media.StreamPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by LeiGuoting on 14/11/11.
 */
public class PluginInstanceHelper {
    private static final String TAG = "PluginInstanceHelper";

    public static SparseArray<ILPlayerPlugin> loadPlugins(String [] customPlugins, boolean isNeedDefault, Context context, ILPlayerPlugin.OnPluginListener pluginListener){
        int size;
        boolean hasCustom = false;
        if(null == customPlugins || 0 == customPlugins.length){
            size = 1;
            isNeedDefault = true;
        }else{
            hasCustom = true;
            if(isNeedDefault){
                size = customPlugins.length + 1;
            }else{
                size = customPlugins.length;
            }
        }

        SparseArray<ILPlayerPlugin> pluginMapping = new SparseArray<ILPlayerPlugin>(size);
        ILPlayerPlugin plugin;
        if(isNeedDefault){
            plugin = new StreamPlugin(context, pluginListener);
            pluginMapping.put(plugin.getPluginCode(), plugin);
        }

        if(hasCustom){
            for(String clazz : customPlugins){
                plugin = instancePlugin(clazz,context, pluginListener);
                pluginMapping.put(plugin.getPluginCode(), plugin);
            }
        }
        return pluginMapping;
    }

    private static ILPlayerPlugin instancePlugin(String classPath, Context context, ILPlayerPlugin.OnPluginListener pluginListener) throws CannotInstancePluginException{
        Log.d(TAG, "@instancePlugin _________  plugin[" + classPath + "]");
        Class<?> pluginClazz;
        try{
            pluginClazz =  Class.forName(classPath);
        }catch(ClassNotFoundException e){
            throw new CannotInstancePluginException(e);
        }

        ILPlayerPlugin plugin;
        try {
            Constructor constructor = pluginClazz.getConstructor(Context.class, ILPlayerPlugin.OnPluginListener.class);
            plugin = (ILPlayerPlugin)constructor.newInstance(context, pluginListener);
        } catch (NoSuchMethodException e) {
            throw new CannotInstancePluginException("The plugin must include the constructor(Context, ILPlayerPlugin.OnLPlayerPluginListener, ILPlayerPlugin.LPlayerCallback), but We did not found that.", e);
        } catch (InvocationTargetException e) {
            throw new CannotInstancePluginException(e);
        } catch (InstantiationException e) {
            throw new CannotInstancePluginException(e);
        } catch (IllegalAccessException e) {
            throw new CannotInstancePluginException(e);
        }
        return plugin;
    }
}