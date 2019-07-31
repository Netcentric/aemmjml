/*
 * (C) Copyright 2019 Netcentric, a Cognizant Digital Business.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package biz.netcentric.cq.tools.aemmjml.impl;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.wrappers.SlingHttpServletRequestWrapper;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import biz.netcentric.cq.tools.aemmjml.impl.rewriter.SerializerFactoryImpl;

@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_VENDOR + "=Netcentric",
                ServletResolverConstants.SLING_SERVLET_EXTENSIONS + "=mjml",
                ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES + "=netcentric/aemmjml/foundation/components/page"
        }
)
public class MjmlPageProxyImpl extends SlingSafeMethodsServlet {

    @Override
    protected void doGet(@Nonnull SlingHttpServletRequest request, @Nonnull SlingHttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(request.getResource());

        if (dispatcher == null) {
            throw new ServletException("Failed to dispatch request with mjml extension to html renderer.");
        }

        request.setAttribute(SerializerFactoryImpl.REQUEST_RAW_ATTR, Boolean.TRUE);

        dispatcher.include(new SlingHttpServletRequestWrapper(request) {
            @Nonnull @Override public RequestPathInfo getRequestPathInfo() {
                return new RequestPathInfoWrapper(super.getRequestPathInfo()) {
                    @Override public String getExtension() {
                        return "html";
                    }
                };
            }
        }, response);
    }

}
