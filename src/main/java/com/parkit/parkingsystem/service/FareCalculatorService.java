package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();

        long durationInMilliSecond = outHour - inHour;
        double duration = (double) durationInMilliSecond / (1000 * 60 * 60);

        //If parking time is less than 30 minutes then the price is free.
        if (duration < 0.5) {
            duration = 0;
        }

        double price;

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                price = duration * Fare.CAR_RATE_PER_HOUR;
                break;
            }
            case BIKE: {
                price = duration * Fare.BIKE_RATE_PER_HOUR;
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }

        //5% reduction cost for discount ticket
        if (discount) {
            price = price * 0.95;
        }

        ticket.setPrice(price);
    }

    public void calculateFare(Ticket ticket){
        calculateFare(ticket, false);
    }
}