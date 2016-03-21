/*
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
package com.datazuul.iiif.catalog.portal.config;

//import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * Replaces web.xml.
 *
 * @author ralf
 */
//@Order(2)
public class WebappInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

  @Override
  protected Class<?>[] getRootConfigClasses() {
    return new Class<?>[]{SpringConfig.class};
  }

  @Override
  protected Class<?>[] getServletConfigClasses() {
    return null;
  }

  @Override
  protected String[] getServletMappings() {
    return new String[]{"/*"};
  }

//    @Override
//    protected Filter[] getServletFilters() {
//        // jpa session
//        OpenEntityManagerInViewFilter openEntityManagerInViewFilter = new OpenEntityManagerInViewFilter();
//        openEntityManagerInViewFilter.setEntityManagerFactoryBeanName("entityManagerFactory");
//
//        // session id for logging, see log4j.xml
//        final LogSessionIdFilter logSessionIdFilter = new LogSessionIdFilter();
//
//        return new Filter[]{logSessionIdFilter, openEntityManagerInViewFilter};
//    }
  @Override
  public void onStartup(ServletContext servletContext) throws ServletException {
    super.onStartup(servletContext);
    // servletContext.addListener(new HttpSessionListenerImpl()); // session and servlet context config see now ServletContextListenerImpl
  }
}
