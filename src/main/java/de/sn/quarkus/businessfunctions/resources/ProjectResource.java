package de.sn.quarkus.businessfunctions.resources;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.jaxrs.PathParam;

import de.sn.quarkus.businessfunctions.model.Item;
import de.sn.quarkus.businessfunctions.model.Project;
import io.quarkus.panache.common.Page;

@Path("/projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class ProjectResource {
	
	@Inject
    EntityManager em;
	
	@Inject Validator validator;

	@GET
    public Response getPagableList( 
    		@QueryParam("pageNum") @DefaultValue("0") @Min(0) int pageNum, 
    		@QueryParam("pageSize") @DefaultValue("10") @Min(0) int pageSize) {
    	
		long timestamp = System.currentTimeMillis();
		List<Project> locomotives = Project
    			.findAll().page(Page.of(pageNum, pageSize)).list();
    	return Response
    			.ok(locomotives)
        		.header("responsetime", (System.currentTimeMillis() - timestamp))
    			.build();
    }
	
	@GET
    @Path("/{id}")
	public Response getProjectById(
    		@PathParam("id") @NotNull Long id) {
    	long timestamp = System.currentTimeMillis();
    	Project myProject = Project.findById(id);
    	if (myProject != null) {
    	   	return Response
        			.ok(myProject)
            		.header("responsetime", (System.currentTimeMillis() - timestamp))
        			.build();
    	}
    	return Response.status(Response.Status.NOT_FOUND).build();
    }
	
	@POST
	public Response add(@Valid Project project) {
		project.id = null;
		//Items will not be stored in this method
		if (project.items != null) project.items.clear();
		Project storedProject = em.merge(project);
		return Response.status(Response.Status.CREATED).entity(storedProject).build();
	}
	
	@PUT
	 public Response change(@Valid Project project) {
	 	Project myProject  = Project.findById(project.id);
    	if (myProject != null) {
    		myProject.name = project.name;
    		myProject.persist();
    		return Response.status(Response.Status.OK).entity(myProject).build();
    	}else {
    		return Response.status(Response.Status.NOT_FOUND).build();
    	}
	}
	
	@DELETE
    @Path("/{id}")
	public Response delete(@PathParam("id") Long id) throws Exception{
    	Project project  = Project.findById(id);
    	if (project != null) {
    		List<Item> items = Item.findByProjectId(id).list();
    		if (items != null){
    			if (items.size() > 0) throw new Exception("Project contains items, which must be deleted before!");
			}
    		project.delete();
        	return Response
            		.status(Response.Status.NO_CONTENT)
            		.build();
    	} else {
    		return Response.status(Response.Status.NOT_FOUND).build();
		}    	
    }
}