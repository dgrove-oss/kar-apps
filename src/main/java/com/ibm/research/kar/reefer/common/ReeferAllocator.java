package com.ibm.research.kar.reefer.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import com.ibm.research.kar.actor.ActorRef;
import com.ibm.research.kar.reefer.actors.ReeferActor.ReeferAllocationStatus;
import com.ibm.research.kar.reefer.common.packingalgo.PackingAlgo;

public class ReeferAllocator {

    public static List<ActorRef> allocate( PackingAlgo packingStrategy, List<ActorRef> availableReefers, int productQuantity, String voyageId) {
        int remainingProductQuantity = productQuantity;

        List<ActorRef>  reefers = new ArrayList<>();
        System.out.println("ReeferAllocator.allocate() - Reefer Count:"+availableReefers.size());
        
        for( ActorRef reefer : availableReefers ) {
            try {
                ReeferState reeferState = new ReeferState(reefer);
                // skip Allocated reefers and those partially allocated to another voyage
                if ( reeferState.alreadyAllocated() ||
                     reeferState.partiallyAllocatedToAnotherVoyage(voyageId)) {
                    System.out.println("ReeferAllocator.allocate() - Reefer:"+
                    reefer.getId()+
                    " not avaialable. Allocated:"
                    +reeferState.alreadyAllocated()
                    +" Allocated to another voyage:"+reeferState.partiallyAllocatedToAnotherVoyage(voyageId)
                    +"- Skipping");
                    continue;
                }
                remainingProductQuantity = packingStrategy.pack(reeferState, remainingProductQuantity, voyageId);
                // save reefer state changes made in in the packing algo
                reeferState.save();
                reefers.add(reefer);
                // we are done if all products packed into reefers
                if ( remainingProductQuantity == 0 ) {
                    break;
                }
            } catch( Exception e) {
                e.printStackTrace();
                // this should result in rejected order due to reefer allocation problem
                return Collections.emptyList();

            }
        }
        return reefers;
    }
}