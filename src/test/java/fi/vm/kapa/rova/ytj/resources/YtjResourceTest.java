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

import fi.vm.kapa.rova.external.model.ytj.CompanyAuthorizationData;
import fi.vm.kapa.rova.external.model.ytj.CompanyAuthorizationDataRequest;
import fi.vm.kapa.rova.external.model.ytj.CompanyWithStatusDTO;
import fi.vm.kapa.rova.rest.exception.WebApplicationException;
import fi.vm.kapa.rova.ytj.YTJ;
import fi.vm.kapa.rova.ytj.service.YtjService;
import org.easymock.EasyMockRule;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.*;

public class YtjResourceTest extends EasyMockSupport {
    
    @Rule
    public EasyMockRule rule = new EasyMockRule(this);
    
    @Mock 
    private YtjService ytjService;
    
    @TestSubject
    private YTJ ytjResource = new YtjResource();
    
    @Test(expected = WebApplicationException.class)
    public void invalidSsnReturnsBadRequest() throws Exception {
        ytjResource.getCompanyAuthorizationDataResponse(buildParameters("010180-0000")).getStatusCodeValue();
    }

    @Test(expected = WebApplicationException.class)
    public void invalidSsn2ReturnsBadRequest() throws Exception {
        ytjResource.getCompanyAuthorizationDataResponse(buildParameters("fuulaa")).getStatusCodeValue();
    }
    @Test
    public void unknownCompanyReturnsNotFound() throws Exception {
        expect(ytjService.getCompanyAuthorizationData("010180-9026")).andReturn(Optional.empty());
        replayAll();
        
        assertEquals(HttpStatus.NO_CONTENT.value(),
                ytjResource.getCompanyAuthorizationDataResponse(buildParameters("010180-9026")).getStatusCodeValue());
    }

    @Test
    public void validCompanyReturnsOk() throws Exception {
        CompanyAuthorizationData data = new CompanyAuthorizationData("1234567-8", "Yritys Ab");
        
        expect(ytjService.getCompanyAuthorizationData("010180-9026")).andReturn(Optional.of(data));
        replayAll();
        
        ResponseEntity response = ytjResource.getCompanyAuthorizationDataResponse(buildParameters("010180-9026"));
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        assertTrue(response.hasBody());
    }
    
    @Test
    public void validGetUpdatedCompaniesReturnsResult() throws Exception {
        
        String[] values = {"123456-7"}; 
        List<String> returnValue = new ArrayList<>(Arrays.asList(values));
        
        expect(ytjService.getUpdatedCompanies(anyObject(Date.class))).andReturn(Optional.of(returnValue));
        replayAll();
        
        ResponseEntity<List<String>> response = ytjResource.getUpdatedCompaniesResponse(23432423l);
        assertEquals(1, response.getBody().size());
    }
    
    
    @Test
    public void getCompaniesWithEmptyList() throws Exception {
        expect(ytjService.getCompanyAuthorizationData("010180-9026")).andReturn(Optional.empty());
        replayAll();
        
        assertNull(ytjResource.getCompaniesResponse(new ArrayList()).getBody());
    }
    
    @Test
    public void validGetCompaniesReturnsResult() throws Exception {
        
        String[] input = {"123456-7"}; 
        List<String> inputValue = new ArrayList<>(Arrays.asList(input));
        
        List<CompanyWithStatusDTO> returnValues = new ArrayList();
        
        CompanyWithStatusDTO dto = new CompanyWithStatusDTO();
        dto.setTradeName("tradeName");
        
        returnValues.add(dto);
        
        expect(ytjService.getCompanies(inputValue)).andReturn(Optional.of(returnValues));
        replayAll();
        
        ResponseEntity<List<CompanyWithStatusDTO>> response = ytjResource.getCompaniesResponse(inputValue);
        assertEquals(1, response.getBody().size());
    }
    

    private CompanyAuthorizationDataRequest buildParameters(String ssn) {
        CompanyAuthorizationDataRequest request = new CompanyAuthorizationDataRequest();
        request.setSsn(ssn);
        return request;
    }
}
