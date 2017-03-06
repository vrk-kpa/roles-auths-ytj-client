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

import bis.dataservices.companyquery.v1.*;
import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfstring;
import fi.prh.ytj.xroad.authorizationqueryservice.*;
import fi.vm.kapa.rova.external.model.ytj.CompanyAuthorizationData;
import fi.vm.kapa.rova.external.model.ytj.CompanyWithStatusDTO;
import fi.vm.kapa.rova.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.SOAPFaultException;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static fi.vm.kapa.rova.logging.Logger.Field.DURATION;
import static fi.vm.kapa.rova.logging.Logger.Field.OPERATION;

@Service
public class YtjService {

    private static final String FAULT_COMPANY_NOT_FOUND = "SOAP-ENV:BISAUTH102";
    private static Logger LOG = Logger.getLogger(YtjService.class);

    private static final String OPERATION_GET_COMPANY_AUTHORIZATION_DATA = "getCompanyAuthorizationData";
    private static final String OPERATION_GET_UPDATED_COMPANIES = "getUpdatedCompanies";
    private static final String OPERATION_GET_COMPANIES = "getCompanies";

    @Autowired
    private AuthorizationQueryService authorizationQueryService;

    @Autowired
    @Qualifier("updatedCompaniesQueryService")
    private CompanyQueryService updatedCompaniesQueryService;

    @Autowired
    @Qualifier("companiesQueryService")
    private CompanyQueryService companiesQueryService;

    private fi.prh.ytj.xroad.authorizationqueryservice.ObjectFactory authorizationQueryServiceFactory =
                                                        new fi.prh.ytj.xroad.authorizationqueryservice.ObjectFactory();
    private bis.dataservices.companyquery.v1.ObjectFactory companyQueryServiceFactory =
                                                        new bis.dataservices.companyquery.v1.ObjectFactory();

    public Optional<CompanyAuthorizationData> getCompanyAuthorizationData(String socialsec) throws Exception {

        Holder<XrdGetCompanyAuthorizationDataRequest> requestHolder = buildRequest(socialsec);
        Holder<XrdGetCompanyAuthorizationDataResponse> responseHolder = buildResponse();

        long startTime = System.currentTimeMillis();
        try {
            authorizationQueryService.getCompanyAuthorizationData(requestHolder, responseHolder);
        } catch (AuthorizationQueryServiceGetCompanyAuthorizationDataCompanyNotFoundFaultFaultFaultMessage ex) {
            logYTJRequest(OPERATION_GET_COMPANY_AUTHORIZATION_DATA, startTime, System.currentTimeMillis());
            return Optional.empty();
        } catch (SOAPFaultException e) {
            if (e.getFault().getFaultCode().equals(FAULT_COMPANY_NOT_FOUND)) {
                logYTJRequest(OPERATION_GET_COMPANY_AUTHORIZATION_DATA, startTime, System.currentTimeMillis());
                return Optional.empty();
            } else {
                throw e;
            }
        }

        logYTJRequest(OPERATION_GET_COMPANY_AUTHORIZATION_DATA, startTime, System.currentTimeMillis());
        AuthorizationQueryResponse value = responseHolder.value.getGetCompanyAuthorizationDataResult().getValue();

        return Optional.of(new CompanyAuthorizationData(value.getBusinessId().getValue(), value.getTradeName().getValue()));
    }

    public Optional<List<String>> getUpdatedCompanies(Date startDate) throws Exception {

        Holder<XrdGetUpdatedCompaniesRequest> requestHolder = buildUpdateCompaniesRequest(startDate);
        Holder<XrdGetUpdatedCompaniesResponse> responseHolder = buildUpdatedCompaniesResponse();
        long startTime = System.currentTimeMillis();

        try {
            updatedCompaniesQueryService.getUpdatedCompanies(requestHolder, responseHolder);
        } catch (SOAPFaultException e) {
            if (e.getFault().getFaultCode().equals(FAULT_COMPANY_NOT_FOUND)) {
                logYTJRequest(OPERATION_GET_UPDATED_COMPANIES, startTime, System.currentTimeMillis());
                return Optional.empty();
            } else {
                throw e;
            }
        }

        logYTJRequest(OPERATION_GET_UPDATED_COMPANIES, startTime, System.currentTimeMillis());

        if (responseHolder.value.getGetUpdatedCompaniesResult().isNil()) {
            return Optional.empty();
        }

        UpdatedCompaniesQueryResponse value = responseHolder.value.getGetUpdatedCompaniesResult().getValue();

        if (value == null) {
            return Optional.empty();
        }

        List<String> companyIds = value.getUpdatedCompanies().getValue().getUpdatedCompaniesQueryResult().stream()
                .map(company -> company.getBusinessId().getValue())
                .collect(Collectors.toList());

        return Optional.of(companyIds);
    }

    public Optional<List<CompanyWithStatusDTO>> getCompanies(List<String> companyIds) throws Exception {

        Holder<XrdGetCompaniesRequest> requestHolder = buildCompaniesRequest(companyIds);
        ;
        Holder<XrdGetCompaniesResponse> responseHolder = buildCompaniesResponse();
        long startTime = System.currentTimeMillis();

        try {
            companiesQueryService.getCompanies(requestHolder, responseHolder);
        } catch (SOAPFaultException e) {
            if (e.getFault().getFaultCode().equals(FAULT_COMPANY_NOT_FOUND)) {
                logYTJRequest(OPERATION_GET_COMPANIES, startTime, System.currentTimeMillis());
                return Optional.empty();
            } else {
                throw e;
            }
        }
        logYTJRequest(OPERATION_GET_COMPANIES, startTime, System.currentTimeMillis());

        if (responseHolder.value.getGetCompaniesResult().isNil() ||
                responseHolder.value.getGetCompaniesResult().getValue().getCompanies().isNil()) {

            return Optional.empty();
        }

        List<Company> value = responseHolder.value.getGetCompaniesResult().getValue().getCompanies().getValue().getCompany();

        return createCompanyDTOs(value);
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

    private Holder<XrdGetUpdatedCompaniesRequest> buildUpdateCompaniesRequest(Date startDate) throws DatatypeConfigurationException {
        UpdatedCompaniesQuery updatedCompaniesQuery = companyQueryServiceFactory.createUpdatedCompaniesQuery();

        GregorianCalendar gregory = new GregorianCalendar();
        gregory.setTime(startDate);

        XMLGregorianCalendar calendar = DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(
                        gregory);

        updatedCompaniesQuery.setStartDate(calendar);

        XrdGetUpdatedCompaniesRequest request = companyQueryServiceFactory.createXrdGetUpdatedCompaniesRequest();
        request.setUpdatedCompaniesQuery(companyQueryServiceFactory.createXrdGetUpdatedCompaniesRequestUpdatedCompaniesQuery(updatedCompaniesQuery));
        return new Holder<>(request);
    }

    private Holder<XrdGetUpdatedCompaniesResponse> buildUpdatedCompaniesResponse() {
        XrdGetUpdatedCompaniesResponse response = companyQueryServiceFactory.createXrdGetUpdatedCompaniesResponse();
        return new Holder<>(response);
    }

    private Holder<XrdGetCompaniesRequest> buildCompaniesRequest(List<String> companyIds) throws DatatypeConfigurationException {
        CompaniesQuery companiesQuery = companyQueryServiceFactory.createCompaniesQuery();

        ArrayOfstring arrayOfstring = new ArrayOfstring();
        arrayOfstring.getString().addAll(companyIds);

        companiesQuery.setBusinessIds(companyQueryServiceFactory.createCompaniesQueryBusinessIds(arrayOfstring));

        XrdGetCompaniesRequest request = companyQueryServiceFactory.createXrdGetCompaniesRequest();
        request.setCompaniesQuery(companyQueryServiceFactory.createXrdGetCompaniesRequestCompaniesQuery(companiesQuery));
        return new Holder<>(request);
    }

    private Holder<XrdGetCompaniesResponse> buildCompaniesResponse() {
        XrdGetCompaniesResponse response = companyQueryServiceFactory.createXrdGetCompaniesResponse();
        return new Holder<>(response);
    }

    private Optional<List<CompanyWithStatusDTO>> createCompanyDTOs(List<Company> companies) {

        if (companies == null) {
            return Optional.empty();
        }

        List<CompanyWithStatusDTO> companyDTOs = companies.stream().map(c -> new CompanyWithStatusDTO(
                c.getBusinessId().isNil() ? null : c.getBusinessId().getValue(),
                c.getTradeName().isNil() ? null : c.getTradeName().getValue().getName().getValue(),
                c.getCompanyStatus().isNil() ? null : c.getCompanyStatus().getValue().getStatus().getValue().getPrimaryCode().getValue(),
                c.getCompanyStatus().isNil() ? null
                        : c.getCompanyStatus().getValue().getBusinessIdStatus().getValue().getPrimaryCode().getValue(),
                createTradeNames(c.getAuxiliaryTradeNames()),
                createTradeNames(c.getParallelTradeNames())))
                .collect(Collectors.toList());
        return Optional.of(companyDTOs);
    }

    private List<String> createTradeNames(JAXBElement<ArrayOfTradeName> tradeNames) {

        if (tradeNames.isNil() || tradeNames.getValue().getTradeName().isEmpty()) {
            return null;
        }

        List<String> parallelTradeNamesResult = tradeNames.getValue().getTradeName().stream()
                .map(name -> name.getName().getValue())
                .collect(Collectors.toList());
        return parallelTradeNamesResult;
    }

    private void logYTJRequest(String operation, long startTime, long currentTimeMillis) {
        Logger.LogMap logmap = LOG.infoMap();
        logmap.set(OPERATION, operation);
        logmap.set(DURATION, currentTimeMillis - startTime);
        logmap.log();
    }
}
