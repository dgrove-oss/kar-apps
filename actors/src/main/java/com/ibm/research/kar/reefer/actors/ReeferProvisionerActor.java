package com.ibm.research.kar.reefer.actors;
 
import static com.ibm.research.kar.Kar.actorCall;
import static com.ibm.research.kar.Kar.actorRef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.ws.rs.core.Response;

import com.ibm.research.kar.Kar;
import com.ibm.research.kar.actor.ActorRef;
import com.ibm.research.kar.actor.annotations.Activate;
import com.ibm.research.kar.actor.annotations.Actor;
import com.ibm.research.kar.actor.annotations.Remote;
import com.ibm.research.kar.actor.exceptions.ActorMethodNotFoundException;
import com.ibm.research.kar.reefer.ReeferAppConfig;
import com.ibm.research.kar.reefer.actors.ReeferActor.ReeferAllocationStatus;
import com.ibm.research.kar.reefer.common.ReeferAllocator;
import com.ibm.research.kar.reefer.common.ReeferState;
import com.ibm.research.kar.reefer.common.packingalgo.PackingAlgo;
import com.ibm.research.kar.reefer.common.packingalgo.SimplePackingAlgo;
import com.ibm.research.kar.reefer.model.JsonOrder;
import com.ibm.research.kar.reefer.model.ReeferDTO;
import com.ibm.research.kar.reefer.model.ReeferDTO.State;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;

@Actor
public class ReeferProvisionerActor extends BaseActor {

    private ReeferDTO[] reeferMasterInventory = null;
    
  //  private Map<String,JsonValue> inventory = new HashMap<>();

  //  private Map<Integer,ActorRef> reeferInventory = new HashMap<>();
  //  private PackingAlgo packingAlgo;

    @Activate
    public void init() {
        System.out.println(
            "ReeferProvisionerActor.init() called- Actor ID:" + this.getId());
            /*
        try {
            
            if ( ReeferAppConfig.PackingAlgoStrategy.equals("simple")) {
                packingAlgo = new SimplePackingAlgo();
            }
            
            System.out.println(
                "ReeferProvisionerActor.init() Fetching Inventory from Actor State" );
     
            Map<String,JsonValue> currentInventory = 
                Kar.actorGetAllState(this);
            System.out.println(
                    "ReeferProvisionerActor.init() Fetched Inventory from Actor State" );
           
            if ( currentInventory == null || currentInventory.isEmpty()) {
                System.out.println(
                "ReeferProvisionerActor.init() - inventory not available");
                addReefers(10000);
               
                inventory.forEach( (key,value) -> {
                   Kar.actorSetState(this, key, value);
                 });
                System.out.println("ReeferProvisionerActor.init() - saved reefer inventory ");

            } else {
                System.out.println(
                    "ReeferProvisionerActor.init() - inventory available ");
                inventory = currentInventory;
            }
 

        } catch( Exception e) {
            e.printStackTrace();
        }
 */
    }
/*
    private void addReefers(int howMany) {
        JsonObject params = Json.createObjectBuilder()
            .add(ReeferActor.ReeferMaxCapacityKey, ReeferAppConfig.ReeferMaxCapacityValue )
            .build();


        for( int i=0; i < howMany; i++ ) {
            JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
            String id = UUID.randomUUID().toString();
            jsonBuilder.add(ReeferState.REEFER_ID_KEY, id).add(ReeferState.VOYAGE_ID_KEY,"").
                add(ReeferState.ALLOCATION_STATUS_KEY, ReeferAllocationStatus.EMPTY.toString()).
                add(ReeferState.MAX_CAPACITY_KEY, 1000).add(ReeferState.REMAINING_CAPACITY_KEY, 1000);
            inventory.put(id, jsonBuilder.build());
        }
        System.out.println("ReeferProvisioner.addReefers() - created "+howMany+" Actors");
    }
    @Remote
    public JsonObject updateReeferLocation(JsonObject message) {
        JsonArray reefers = message.getJsonArray("reefers");
        String location = message.getString("location");
        String allocationStatus = message.getString(ReeferState.ALLOCATION_STATUS_KEY);
        reefers.forEach(reefer -> {
            String reeferId = reefer.asJsonObject().toString();
            JsonObject params = Json.createObjectBuilder()
            .add("location",  location)
            .add(ReeferState.ALLOCATION_STATUS_KEY, allocationStatus )
                .build();
            ActorRef reeferActor =  actorRef(ReeferAppConfig.ReeferActorName,reeferId);
            try {
                actorCall( reeferActor, "changeLocation", params);
               
            } catch( ActorMethodNotFoundException ee) {
                ee.printStackTrace();
            } catch( Exception ee) {
                ee.printStackTrace();
            }
        });
        return Json.createObjectBuilder().add("status", "OK").build();
    }
    */
    private void initMasterInventory(int inventorySize) {
        reeferMasterInventory = new ReeferDTO[inventorySize]; 
    }
    private int getReeferInventorySize() {
        Response response = Kar.restGet("reeferservice", "reefers/inventory/size");
        JsonValue size = response.readEntity(JsonValue.class);
        System.out.println("ReeferProvisionerActor.getReeferInventorySize() - Inventory Size:"+size);
        return Integer.valueOf(size.toString());
    }
    private void createReeferActor(ReeferDTO reefer) {
        ActorRef reeferActor =  Kar.actorRef(ReeferAppConfig.ReeferActorName,String.valueOf(reefer.getId()));

        try {
            JsonObject params = Json.createObjectBuilder()
                .add(ReeferState.ORDER_ID_KEY,  reefer.getOrderId())
                .add(ReeferState.MAX_CAPACITY_KEY, ReeferAppConfig.ReeferMaxCapacityValue)
                .add(ReeferState.VOYAGE_ID_KEY, reefer.getVoyageId() )
                .add(ReeferState.STATE_KEY, Json.createValue(ReeferState.State.ALLOCATED.name()))
                .build();
            actorCall( reeferActor, "reserve", params);
        
        } catch( ActorMethodNotFoundException ee) {
            ee.printStackTrace();
        } catch( Exception ee) {
            ee.printStackTrace();
        }
    }
    @Remote
    public JsonObject unreserveReefer(JsonObject message ) {
        JsonObjectBuilder reply = Json.createObjectBuilder();
    
        String reeferId = message.getString("reeferId");
        System.out.println("ReeferProvisionerActor.unreserverReefer() - freeing reefer "+reeferId+" ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        reeferMasterInventory[Integer.valueOf(reeferId)] = null;
        return reply.build();
    }
    @Remote
    public void reeferAnomaly(JsonObject message) {
        int reeferId = message.getInt(ReeferState.REEFER_ID_KEY);
        try {
            ActorRef reeferActor =  Kar.actorRef(ReeferAppConfig.ReeferActorName,String.valueOf(reeferId));
            // placeholder for future params
            JsonObject params = Json.createObjectBuilder()
                .build();
            if ( reeferSpoilt( actorCall( reeferActor, "anomaly", params) ) ) {
                ReeferDTO reefer = reeferMasterInventory[reeferId];
                if ( reefer != null ) {
                    reefer.setState(State.SPOILT);
                }
            }

        
        } catch( ActorMethodNotFoundException ee) {
            ee.printStackTrace();
        } catch( Exception ee) {
            ee.printStackTrace();
        }
    }
    private boolean reeferSpoilt(JsonValue response) {
        if ( response.toString().equals("SPOILT")) {
            return true;
        }
        return false;
    }
    @Remote
    public JsonObject bookReefers(JsonObject message) {

        // lazily initialize master reefer inventory list on the first call.
        // This is fast since all we do is just creating an array of
        // fixed size
        if ( reeferMasterInventory == null) {
            initMasterInventory(getReeferInventorySize());
        }

        JsonOrder order = new JsonOrder(message.getJsonObject(JsonOrder.OrderKey));
        System.out.println("ReeferProvisionerActor.bookReefers() called - Order:"+order.getId());
  
        if ( order.containsKey(JsonOrder.ProductQtyKey)) {
            
            int qty = order.getProductQty();
            List<ReeferDTO> orderReefers = ReeferAllocator.allocateReefers(reeferMasterInventory, qty, order.getId(), order.getVoyageId());
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

            for( ReeferDTO reefer : orderReefers ) {
                arrayBuilder.add(reefer.getId());
                createReeferActor(reefer);
            }
            int count = 0;
            for( int i=0; i < reeferMasterInventory.length; i++ ) {
                if ( reeferMasterInventory[i] != null ) {
                    count++;
                }
            }
            System.out.println(":::::: Reserved Reefer Count:"+count);
/*
            List<ReeferState> allocatedReefers = 
                ReeferAllocator.allocateReefers(packingAlgo, new ArrayList<JsonValue>(inventory.values()), qty, order.getVoyageId());

            System.out.println("ReeferProvisionerActor.bookReefers() product qty:"+qty +" Allocated Reefers:"+allocatedReefers.size());
            if ( allocatedReefers.size() == 0 ) {
                return Json.createObjectBuilder().add("status", "FAILED").add("ERROR","FailedToAllocateReefers").add(JsonOrder.IdKey, order.getId()).build();
            }

            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

            for( ReeferState reefer : allocatedReefers ) {
                arrayBuilder.add(reefer.getId());
            }
*/
            // Uncomment below when supporting reefers
            //JsonArrayBuilder arrayBuilder = reserveReefers(allocatedReefers, order);

            return  Json.createObjectBuilder()
                .add("status", "OK")
                .add("reefers",  arrayBuilder)
                .add(JsonOrder.OrderKey, order.getAsObject() )
                    .build();
       //     return reply;

        } else {
            return Json.createObjectBuilder().add("status", "FAILED").add("ERROR","ProductQuantityMissing").add(JsonOrder.IdKey, order.getId()).build();
        }
    }
    /*
    private JsonArrayBuilder reserveReefers(List<ActorRef> allocatedReefers, JsonOrder order ) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for( ActorRef reefer : allocatedReefers ) {

            JsonObject params = Json.createObjectBuilder()
            .add("reeferid",  reefer.getId())
            .add(JsonOrder.OrderKey, order.getAsObject() )
                .build();
            try {
                actorCall( reefer, "reserve", params);
                arrayBuilder.add(reefer.getId());
            } catch( ActorMethodNotFoundException ee) {
                ee.printStackTrace();
            } catch( Exception ee) {
                ee.printStackTrace();
            }
        }
        return arrayBuilder;
    }
    private List<ActorRef> createReefer(JsonObject order) {
        List<ActorRef> newReefers = new ArrayList<>();

        return newReefers;
    }
    
    private int randomIndex() {
        XoRoShiRo128PlusRandom xoroRandom = new XoRoShiRo128PlusRandom();
        return xoroRandom.nextInt(ReeferAppConfig.ReeferInventorySize);
    }
    */
}