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
package org.hasor.context.binder;
import java.util.Set;
import org.hasor.context.ApiBinder;
import org.hasor.context.InitContext;
import org.more.util.StringUtils;
import com.google.inject.Binder;
import com.google.inject.Module;
/**
 * 
 * @version : 2013-4-12
 * @author ������ (zyc@byshell.org)
 */
public abstract class ApiBinderModule implements ApiBinder, Module {
    private InitContext           initContext = null;
    private BeanInfoModuleBuilder beanBuilder = new BeanInfoModuleBuilder(); /*Beans*/
    //
    protected ApiBinderModule(InitContext initContext) {
        this.initContext = initContext;
    }
    @Override
    public void configure(Binder binder) {
        binder.install(this.beanBuilder);
    }
    @Override
    public InitContext getInitContext() {
        return this.initContext;
    }
    @Override
    public Set<Class<?>> getClassSet(Class<?> featureType) {
        if (featureType == null)
            return null;
        return this.getInitContext().getClassSet(featureType);
    }
    @Override
    public BeanBindingBuilder newBean(String beanName) {
        if (StringUtils.isBlank(beanName) == true)
            throw new NullPointerException(beanName);
        return this.beanBuilder.newBeanDefine(this.getGuiceBinder()).aliasName(beanName);
    }
}