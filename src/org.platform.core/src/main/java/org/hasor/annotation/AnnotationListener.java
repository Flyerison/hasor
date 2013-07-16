/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hasor.annotation;
import java.util.Set;
import org.hasor.Hasor;
import org.hasor.context.AbstractHasorModule;
import org.hasor.context.ApiBinder;
import org.hasor.context.ApiBinder.BeanBindingBuilder;
import org.hasor.context.AppContext;
import org.hasor.context.EventManager;
import org.hasor.context.HasorEventListener;
import org.hasor.context.HasorSettingListener;
import org.hasor.context.Lifecycle;
import org.hasor.context.PhaseEventManager;
import org.more.util.ArrayUtils;
import org.more.util.StringUtils;
/**
 * 支持Bean注解功能。启动级别：Lv1Max
 * @version : 2013-4-8
 * @author 赵永春 (zyc@byshell.org)
 */
@Module(displayName = "AnnotationListener", description = "org.hasor.general软件包功能支持。", startIndex = Module.Lv_1Max)
public class AnnotationListener extends AbstractHasorModule {
    //
    /**初始化.*/
    @Override
    public void init(ApiBinder event) {
        if (event.getInitContext().getSettings().getBoolean("framework.annotation") == false) {
            Hasor.warning("init Annotation false!");
            return;
        }
        //1.Bean
        this.loadBean(event);
        //2.Settings
        this.loadSettings(event);
    }
    //
    /***/
    @Override
    public void start(AppContext appContext) {
        this.loadEvent(appContext);
    }
    //
    /**装载Bean*/
    protected void loadBean(ApiBinder event) {
        Set<Class<?>> beanSet = event.getClassSet(Bean.class);
        if (beanSet == null)
            return;
        for (Class<?> beanClass : beanSet) {
            Bean annoBean = beanClass.getAnnotation(Bean.class);
            String[] names = annoBean.value();
            if (ArrayUtils.isBlank(names)) {
                Hasor.warning("missing Bean name %s", beanClass);
                continue;
            }
            if (StringUtils.isBlank(names[0]))
                continue;
            BeanBindingBuilder beanBuilder = event.newBean(names[0]);
            Hasor.info("loadBean %s bind %s", names, beanClass);
            for (int i = 1; i < names.length; i++)
                beanBuilder.aliasName(names[i]);
            beanBuilder.bindType(beanClass);
        }
    }
    //
    /**装载事件*/
    protected void loadEvent(AppContext appContext) {
        //1.扫描classpath包
        Set<Class<?>> eventSet = appContext.getClassSet(EventListener.class);
        EventManager eventManager = appContext.getEventManager();
        //2.过滤未实现HasorModule接口的类
        for (Class<?> cls : eventSet) {
            if (HasorEventListener.class.isAssignableFrom(cls) == false) {
                Hasor.warning("not implemented HasorEventListener :%s", cls);
            } else {
                EventListener eventType = cls.getAnnotation(EventListener.class);
                String[] var = eventType.value();
                if (ArrayUtils.isBlank(var))
                    continue;
                HasorEventListener e = (HasorEventListener) appContext.getInstance(cls);
                for (String v : var)
                    if (!StringUtils.isBlank(v)) {
                        eventManager.addEventListener(v, e);
                        Hasor.info("event ‘%s’ binding to ‘%s’", v, e);
                    }
            }
        }
        Hasor.info("event binding finish.");
    }
    //
    /**装载设置*/
    private void loadSettings(final ApiBinder apiBinder) {
        Set<Class<?>> settingSet = apiBinder.getClassSet(SettingsListener.class);
        if (settingSet == null)
            return;
        PhaseEventManager phaseEventManager = (PhaseEventManager) apiBinder.getInitContext().getEventManager();
        for (final Class<?> settingClass : settingSet) {
            apiBinder.getGuiceBinder().bind(settingClass).asEagerSingleton();
            Hasor.info("%s bind SettingsListener.", settingClass);
            phaseEventManager.pushPhaseEventListener(Lifecycle.PhaseEvent_Start, new HasorEventListener() {
                @Override
                public void onEvent(String event, Object[] params) {
                    AppContext appContext = (AppContext) params[0];
                    HasorSettingListener settingObj = (HasorSettingListener) appContext.getInstance(settingClass);
                    appContext.getSettings().addSettingsListener(settingObj);
                    settingObj.onLoadConfig(appContext.getSettings());
                    Hasor.info("%s SettingsListener created.", settingObj);
                }
            });
        }
    }
}