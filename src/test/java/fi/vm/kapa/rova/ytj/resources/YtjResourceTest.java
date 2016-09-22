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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.easymock.EasyMock.*;

import fi.vm.kapa.rova.external.model.ytj.CompanyAuthorizationData;
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

import java.util.Optional;

public class YtjResourceTest extends EasyMockSupport {
    
    @Rule
    public EasyMockRule rule = new EasyMockRule(this);
    
    @Mock 
    private YtjService ytjService;
    
    @TestSubject
    private YtjResource ytjResource = new YtjResource();
    
    @Test
    public void invalidHetuReturnsBadRequest() throws YtjServiceException {
        assertEquals(Status.BAD_REQUEST.getStatusCode(), ytjResource.getCompanyAuthorizationData("fuulaa").getStatus());
        assertEquals(Status.BAD_REQUEST.getStatusCode(), ytjResource.getCompanyAuthorizationData("010180-0000").getStatus());
    }

    @Test
    public void unknownCompanyReturnsNotFound() throws YtjServiceException {
        expect(ytjService.getCompanyAuthorizationData("010180-111N")).andReturn(Optional.empty());
        replayAll();
        
        assertEquals(Status.NOT_FOUND.getStatusCode(), ytjResource.getCompanyAuthorizationData("010180-111N").getStatus());
    }

    @Test
    public void validCompanyReturnsOk() throws YtjServiceException {
        CompanyAuthorizationData data = new CompanyAuthorizationData("1234567-8", "Yritys Ab");
        
        expect(ytjService.getCompanyAuthorizationData("010180-111N")).andReturn(Optional.of(data));
        replayAll();
        
        Response response = ytjResource.getCompanyAuthorizationData("010180-111N");
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertTrue(response.hasEntity());
    }
}
