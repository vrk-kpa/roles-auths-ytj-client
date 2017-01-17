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
package fi.vm.kapa.rova.ytj.resources;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.*;

import fi.vm.kapa.rova.external.model.ytj.CompanyAuthorizationData;
import fi.vm.kapa.rova.external.model.ytj.CompanyAuthorizationDataRequest;
import fi.vm.kapa.rova.external.model.ytj.CompanyDTO;
import fi.vm.kapa.rova.ytj.service.YtjService;
import fi.vm.kapa.rova.ytj.service.YtjServiceException;
import org.easymock.EasyMockRule;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.util.*;

public class YtjResourceTest extends EasyMockSupport {
    
    @Rule
    public EasyMockRule rule = new EasyMockRule(this);
    
    @Mock 
    private YtjService ytjService;
    
    @TestSubject
    private YtjResource ytjResource = new YtjResource();
    
    @Test
    public void invalidSsnReturnsBadRequest() throws Exception {
        assertEquals(Status.BAD_REQUEST.getStatusCode(), ytjResource.getCompanyAuthorizationData(buildParameters("fuulaa")).getStatus());
        assertEquals(Status.BAD_REQUEST.getStatusCode(), ytjResource.getCompanyAuthorizationData(buildParameters("010180-0000")).getStatus());
    }

    @Test
    public void unknownCompanyReturnsNotFound() throws Exception {
        expect(ytjService.getCompanyAuthorizationData("010180-9026")).andReturn(Optional.empty());
        replayAll();
        
        assertEquals(Status.NOT_FOUND.getStatusCode(), ytjResource.getCompanyAuthorizationData(buildParameters("010180-9026")).getStatus());
    }

    @Test
    public void validCompanyReturnsOk() throws Exception {
        CompanyAuthorizationData data = new CompanyAuthorizationData("1234567-8", "Yritys Ab");
        
        expect(ytjService.getCompanyAuthorizationData("010180-9026")).andReturn(Optional.of(data));
        replayAll();
        
        Response response = ytjResource.getCompanyAuthorizationData(buildParameters("010180-9026"));
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertTrue(response.hasEntity());
    }
    
    @Test
    public void validGetUpdatedCompaniesReturnsResult() throws Exception {
        
        String[] values = {"123456-7"}; 
        List<String> returnValue = new ArrayList<String>(Arrays.asList(values));
        
        expect(ytjService.getUpdatedCompanies(anyObject(Date.class))).andReturn(Optional.of(returnValue));
        replayAll();
        
        List<String> response = ytjResource.getUpdatedCompanies(23432423l);
        assertEquals(1, response.size());
    }
    
    
    @Test
    public void getCompaniesWithEmptyList() throws Exception {
        expect(ytjService.getCompanyAuthorizationData("010180-9026")).andReturn(Optional.empty());
        replayAll();
        
        assertNull(ytjResource.getCompanies(new ArrayList()));
    }
    
    @Test
    public void validGetCompaniesReturnsResult() throws Exception {
        
        String[] input = {"123456-7"}; 
        List<String> inputValue = new ArrayList<String>(Arrays.asList(input));
        
        List<CompanyDTO> returnValues = new ArrayList();
        
        CompanyDTO companyDTO = new CompanyDTO();
        companyDTO.setTradeName("tradeName");
        
        returnValues.add(companyDTO);
        
        expect(ytjService.getCompanies(inputValue)).andReturn(Optional.of(returnValues));
        replayAll();
        
        List<CompanyDTO> response = ytjResource.getCompanies(inputValue);
        assertEquals(1, response.size());
    }
    

    private CompanyAuthorizationDataRequest buildParameters(String ssn) {
        CompanyAuthorizationDataRequest request = new CompanyAuthorizationDataRequest();
        request.setSsn(ssn);
        return request;
    }
}
