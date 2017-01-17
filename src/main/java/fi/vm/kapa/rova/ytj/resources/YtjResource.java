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
import fi.vm.kapa.rova.external.model.ytj.CompanyDTO;
import fi.vm.kapa.rova.logging.Logger;
import fi.vm.kapa.rova.utils.HetuUtils;
import fi.vm.kapa.rova.ytj.service.YtjService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Path("/")
public class YtjResource {

    private static final Logger LOG = Logger.getLogger(YtjResource.class);
    
    @Autowired
    private YtjService ytjService;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/ytj")
    public Response getCompanyAuthorizationData(CompanyAuthorizationDataRequest request) throws Exception {
        LOG.debug("getCompanyAuthorizationData request received.");

        if (request == null || StringUtils.isEmpty(request.getSsn()) || !HetuUtils.isHetuValid(request.getSsn())) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        Optional<CompanyAuthorizationData> companyAuthorizationData = ytjService.getCompanyAuthorizationData(request.getSsn());
        if (!companyAuthorizationData.isPresent()) {
            return Response.status(Status.NOT_FOUND).build();
        }
        
        return Response.ok().entity(companyAuthorizationData.get()).build();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/ytj/companies/updated/startDate/{startDate}")
    public List<String> getUpdatedCompanies(@PathParam("startDate") long startDate) throws Exception {
        LOG.debug("getUpdatedCompanies request received.");

        Optional<List<String>> companies = ytjService.getUpdatedCompanies(new Date(startDate));
        if (companies.isPresent()) {
            return companies.get();
        }
        
        //returns HTTP 204 No Content
        return null;
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/ytj/companies")
    public List<CompanyDTO> getCompanies(List<String> companyIds) throws Exception {
        LOG.debug("getCompanies request received.");

        if(companyIds == null || companyIds.isEmpty()){
            return null;
        }
        
        Optional<List<CompanyDTO>> companies = ytjService.getCompanies(companyIds);
        if (companies.isPresent()) {
            return companies.get();
        }
        
        return null;
    }
}