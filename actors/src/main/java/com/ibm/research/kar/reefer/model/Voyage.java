/*
 * Copyright IBM Corporation 2020,2021
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.research.kar.reefer.model;

import java.time.Instant;

public class Voyage implements Comparable<Voyage>{
    public static final String ID="id";
    public static final String SAIL_DATE="sailDateObject";
    public static final String SAIL_DATE_STRING="sailDate";
    public static final String ARRIVAL_DATE="arrivalDate";
    public static final String ORDER_COUNT="orderCount";
    public static final String DISPLAY_ARRIVAL_DATE="displayArrivalDate";


    private String id;
    private Route route;

    private Instant sailDateObject;
    private String sailDate;
    private String arrivalDate;
    private String displayArrivalDate;
    private int orderCount=0;
    
    public Voyage(Route route, Instant sailDateObject, String arrivalDate) {
        this.route = route;
        this.sailDateObject = sailDateObject;
        this.arrivalDate = arrivalDate;
        this.displayArrivalDate = arrivalDate.substring(0,10);
        this.sailDate = sailDateObject.toString().substring(0,10);
        this.id = String.format("%s-%s",route.getVessel().getName(),this.sailDateObject.toString()).replaceAll("/","-");
    }
    public Voyage(String id, Route route, Instant sailDateObject, String arrivalDate) {
        this.route = route;
        this.sailDateObject = sailDateObject;
        this.arrivalDate = arrivalDate;
        this.displayArrivalDate = arrivalDate.substring(0,10);
        this.sailDate = sailDateObject.toString().substring(0,10);
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public Route getRoute() {
        return route;
    }
    public String getSailDate() {
        return sailDate;
    }
    public String getArrivalDate() {
        return arrivalDate;
    }
    public Instant getSailDateObject() {
        return sailDateObject;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public String getDisplayArrivalDate() {
        return displayArrivalDate;
    }

    @Override
    public String toString() {
        return "Voyage{" +
                "id='" + id + '\'' +
                ", route=" + route +
                ", sailDateObject=" + sailDateObject +
                ", sailDate='" + sailDate + '\'' +
                ", arrivalDate='" + arrivalDate + '\'' +
                ", displayArrivalDate='" + displayArrivalDate + '\'' +
                ", orderCount=" + orderCount +
                '}';
    }

    @Override
    public int compareTo(Voyage v) {
        if ( this.getSailDateObject().isBefore(v.getSailDateObject())) {
            return -1;
        }
        return 1;
    }
}