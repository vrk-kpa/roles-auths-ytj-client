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
package fi.vm.kapa.rova.ytj.service;

import fi.prh.ytj.xroad.authorizationqueryservice.*;
import fi.vm.kapa.rova.external.model.ytj.CompanyAuthorizationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.Optional;

@Service
public class YtjService {

    private static final String FAULT_COMPANY_NOT_FOUND = "SOAP-ENV:BISAUTH102";

    @Autowired
    private AuthorizationQueryService authorizationQueryService;

    private ObjectFactory authorizationQueryServiceFactory = new ObjectFactory();

    public Optional<CompanyAuthorizationData> getCompanyAuthorizationData(String socialsec) throws YtjServiceException {
        Holder<XrdGetCompanyAuthorizationDataRequest> requestHolder = buildRequest(socialsec);
        Holder<XrdGetCompanyAuthorizationDataResponse> responseHolder = buildResponse();

        try {
            authorizationQueryService.getCompanyAuthorizationData(requestHolder, responseHolder);
        } catch (SOAPFaultException e) {
            if (e.getFault().getFaultCode().equals(FAULT_COMPANY_NOT_FOUND)) {
                return Optional.empty();
            }
            else {
                throw new YtjServiceException(e.getFault().getFaultCode(), e.getFault().getFaultString());
            }
        } catch (Exception e) {
            throw new YtjServiceException("", e.getMessage());
        }

        AuthorizationQueryResponse value = responseHolder.value.getGetCompanyAuthorizationDataResult().getValue();
        return Optional.of(new CompanyAuthorizationData(value.getBusinessId().getValue(), value.getTradeName().getValue()));
    }

    private Holder<XrdGetCompanyAuthorizationDataRequest> buildRequest(String socialsec) {
        AuthorizationQuery authorizationQuery = authorizationQueryServiceFactory.createAuthorizationQuery();
        authorizationQuery.setSocialSecurityNumber(authorizationQueryServiceFactory.createAuthorizationQuerySocialSecurityNumber(socialsec));
        XrdGetCompanyAuthorizationDataRequest request = authorizationQueryServiceFactory.createXrdGetCompanyAuthorizationDataRequest();
        request.setAuthorizationQuery(authorizationQueryServiceFactory.createXrdGetCompanyAuthorizationDataRequestAuthorizationQuery(authorizationQuery));
        return new Holder<>(request);
    }

    private Holder<XrdGetCompanyAuthorizationDataResponse> buildResponse() {
        XrdGetCompanyAuthorizationDataResponse response = authorizationQueryServiceFactory.createXrdGetCompanyAuthorizationDataResponse();
        return new Holder<>(response);
    }
}
