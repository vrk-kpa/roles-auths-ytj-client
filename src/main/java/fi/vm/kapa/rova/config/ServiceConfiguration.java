/**
 * The MIT License
 * Copyright (c) 2016 Population Register Centre
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fi.vm.kapa.rova.config;

import fi.vm.kapa.rova.rest.validation.RequestValidationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class ServiceConfiguration {

    @Value("${api_key}")
    String apiKey;

    @Value("${api_path_prefix}")
    String pathPrefix;

    @Value("${request_alive_seconds}")
    Integer requestAliveSeconds;

    @Value("${ssl_keystoretype}")
    String sslKeyStoreType;

    @Value("${ssl_keystore}")
    String sslKeyStore;

    @Value("${ssl_keystorepassword}")
    String sslKeyStorePassword;

    @Value("${ssl_truststoretype}")
    String sslTrustStoreType;

    @Value("${ssl_truststore}")
    String sslTrustStore;

    @Value("${ssl_truststorepassword}")
    String sslTrustStorePassword;

    @PostConstruct
    public void init() {
        System.setProperty("javax.net.ssl.keyStoreType", sslKeyStoreType);
        System.setProperty("javax.net.ssl.keyStore", sslKeyStore);
        System.setProperty("javax.net.ssl.keyStorePassword", sslKeyStorePassword);
        System.setProperty("javax.net.ssl.trustStoreType", sslTrustStoreType);
        System.setProperty("javax.net.ssl.trustStore", sslTrustStore);
        System.setProperty("javax.net.ssl.trustStorePassword", sslTrustStorePassword);
    }

    @Bean(name = "apiValidationFilter")
    public FilterRegistrationBean apiValidationFilter() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(new RequestValidationFilter(apiKey, requestAliveSeconds, pathPrefix));
        filterRegistrationBean.addUrlPatterns("/rest/*");
        filterRegistrationBean.setOrder(FilterRegistrationBean.HIGHEST_PRECEDENCE);
        return filterRegistrationBean;
    }
}
