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

import bis.dataservices.companyquery.v1.CompanyQueryService;
import fi.prh.ytj.xroad.authorizationqueryservice.AuthorizationQueryService;
import fi.vm.kapa.rova.prh.soap.AuthorizationQueryServiceHeaderHandler;
import fi.vm.kapa.rova.prh.soap.CompaniesHeaderHandler;
import fi.vm.kapa.rova.prh.soap.UpdatedCompaniesHeaderHandler;
import fi.vm.kapa.rova.prh.soap.XroadHeaderHandler;
import org.apache.cxf.clustering.LoadDistributorFeature;
import org.apache.cxf.clustering.RandomStrategy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SoapConfiguration {

    @Value("${xroad_endpoint}")
    private String xroadEndpoint;

    @Autowired
    private AuthorizationQueryServiceHeaderHandler headerHandler;
    
    @Autowired
    private UpdatedCompaniesHeaderHandler updatedCompaniesHeaderHandler;
    
    @Autowired
    private CompaniesHeaderHandler companiesHeaderHandler;
    
    @Bean
    public AuthorizationQueryService authorizationQueryService() {
        return (AuthorizationQueryService) jaxWsProxyFactoryBean(AuthorizationQueryService.class, headerHandler).create();
    }

    @Bean
    @Qualifier("updatedCompaniesQueryService") 
    public CompanyQueryService updatedCompaniesQueryService() {
        return (CompanyQueryService) jaxWsProxyFactoryBean(CompanyQueryService.class, updatedCompaniesHeaderHandler).create();
    }
    
    @Bean
    @Qualifier("companiesQueryService") 
    public CompanyQueryService companiesQueryService() {
        return (CompanyQueryService) jaxWsProxyFactoryBean(CompanyQueryService.class, companiesHeaderHandler).create();
    }

    private JaxWsProxyFactoryBean jaxWsProxyFactoryBean(Class target, XroadHeaderHandler handler) {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        // load distribution
        LoadDistributorFeature loadDistributorFeature = new RovaLoadDistributorFeature();
        RandomStrategy ldStrategy = new RandomStrategy();
        ldStrategy.setAlternateAddresses(getEndpoints());
        loadDistributorFeature.setStrategy(ldStrategy);
        factory.getFeatures().add(loadDistributorFeature);
        factory.getHandlers().add(handler);
        factory.setServiceClass(target);
        return factory;
    }

    private List<String> getEndpoints() {
        String[] endpoints = xroadEndpoint.split(",");
        List<String> list = Arrays.asList(endpoints);
        return list;
    }
}
