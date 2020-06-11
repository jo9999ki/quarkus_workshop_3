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

import de.jk.quarkus.trains.exception.RecordNotFoundException;
import de.sn.quarkus.businessfunctions.model.Item;
import de.sn.quarkus.businessfunctions.model.Project;
import io.quarkus.panache.common.Page;

@Path("/items")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class ItemResource {
	
	@Inject
    EntityManager em;
	
	@Inject 
	Validator validator;

	@GET
	@Path("/project/{projectid}")
    public Response getPagableItemListForProject(
    		@PathParam("projectid") @NotNull Long projectid,
    		@QueryParam("pageNum") @DefaultValue("0") @Min(0) int pageNum, 
    		@QueryParam("pageSize") @DefaultValue("10") @Min(0) int pageSize) {
    	
		long timestamp = System.currentTimeMillis();
		List<Item> items = Item.findByProjectId(projectid).page(Page.of(pageNum, pageSize)).list();
    	return Response
    			.ok(items)
        		.header("responsetime", (System.currentTimeMillis() - timestamp))
    			.build();
    }
	
	@GET
	@Path("/project/{projectid}/level/{level}")
    public Response getPagableItemListForProjectAndLevel(
    		@PathParam("projectid") @NotNull Long projectid,
    		@PathParam("level") @NotNull Integer level,
    		@QueryParam("pageNum") @DefaultValue("0") @Min(0) int pageNum, 
    		@QueryParam("pageSize") @DefaultValue("10") @Min(0) int pageSize) {
    	
		long timestamp = System.currentTimeMillis();
		List<Item> items = Item.findByLevelAndProjectId(level, projectid).page(Page.of(pageNum, pageSize)).list();
    	return Response
    			.ok(items)
        		.header("responsetime", (System.currentTimeMillis() - timestamp))
    			.build();
    }
	
	@GET
    @Path("/{id}")
	public Response getItemById(
    		@PathParam("id") @NotNull Long id) {
    	long timestamp = System.currentTimeMillis();
    	Item myItem = Item.findById(id);
    	if (myItem != null) {
    	   	return Response
        			.ok(myItem)
            		.header("responsetime", (System.currentTimeMillis() - timestamp))
        			.build();
    	}
    	return Response.status(Response.Status.NOT_FOUND).build();
    }
	
	@POST
	@Path("/project/{projectid}")
	public Response addMainItem(
			@PathParam("projectid") @NotNull Long projectid,
			@Valid Item item) throws Exception{
		
			Project project = Project.findById(projectid);
			if (project == null) {	
				throw new RecordNotFoundException("Project with id " + projectid +" does not exist");
			}
			item.id = null;
			item.project = project;
			Item itemAbove = null;
			if (item.item != null) {
				itemAbove = Item.findById(item.item.id);
				if (itemAbove == null) {
					throw new RecordNotFoundException("Item with id " + item.item.id +" does not exist");
				}
				item.item = itemAbove;
			}
			Item storedItem = em.merge(item);
			project.items.add(storedItem);
			project.persist();
			if (itemAbove !=null) {
				itemAbove.items.add(storedItem);
				itemAbove.persist();
			}
			return Response.status(Response.Status.CREATED).entity(storedItem).build();		
	}
	@POST
	@Path("/project/{projectid}/item/{itemabove}")
	public Response addSubItem(
			@PathParam("projectid") @NotNull Long projectid,
			@PathParam("itemabove") @NotNull Long itemAboveId,
			@Valid Item item) throws Exception{
		
			Project project = Project.findById(projectid);
			if (project == null) {	
				throw new RecordNotFoundException("Project with id " + projectid +" does not exist");
			}
			
			Item itemAbove = Item.findById(itemAboveId);
			if (itemAbove == null) {
				throw new RecordNotFoundException("Item with id " + item.item.id +" does not exist");
			}
			item.id = null;
			item.item = itemAbove; //Addmain item			
			item.project = project; //Add item to project
			Item storedItem = em.merge(item);

			return Response.status(Response.Status.CREATED)
					.entity(storedItem)
					.build();		
	}
	
	@PUT
	 public Response change(Item item) {
	 	//Can Change item attributes only, no references to project, higher or sub items
		
		Item myItem  = Item.findById(item.id);
    	if (myItem != null) {
    		myItem.name = item.name;
    		myItem.imageURL = item.imageURL;
    		myItem.level = item.level;
    		Item storedItem = em.merge(myItem);
    		return Response.status(Response.Status.OK).entity(storedItem).build();
    	}else {
    		return Response.status(Response.Status.NOT_FOUND).build();
    	}
	}
	
	@DELETE
    @Path("/{id}")
	public Response delete(@PathParam("id") Long id) throws Exception{
    	Item item  = Item.findById(id);
    	long itemID = item.id;
    	if (item == null) {
    		return Response.status(Response.Status.NOT_FOUND).build();
    	}
    	
    	if (item.items.size() > 0) {
    		throw new Exception("Item contains sub items, which have to be deleted before!");
    	}    		

    	//Delete item from project
		item.project.items.remove(item);

		//Delete item from main items item list
		if (item.item != null) {
			if (item.item.items.size() > 0) {
				item.item.items.remove(item);
			}
		}
		
		item.delete();
		
    	return Response
        		.status(Response.Status.NO_CONTENT)
        		.build();    	
    }
	
}