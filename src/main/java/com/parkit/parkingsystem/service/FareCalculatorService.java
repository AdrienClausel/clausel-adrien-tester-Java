package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();

        long durationInMilliSecond = outHour - inHour;
        BigDecimal duration = BigDecimal.valueOf(durationInMilliSecond)
                .divide(BigDecimal.valueOf(1000 * 60 * 60),2, RoundingMode.HALF_UP);

        //If parking time is less than 30 minutes then the price is free.
        if (duration.compareTo(BigDecimal.valueOf(0.5)) < 0){
            duration = BigDecimal.ZERO;
        }

        BigDecimal rate;

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                rate = Fare.CAR_RATE_PER_HOUR;
                break;
            }
            case BIKE: {
                rate = Fare.BIKE_RATE_PER_HOUR;
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }

        BigDecimal price = duration.multiply(rate);

        //5% reduction cost for discount ticket
        if (discount) {
            price = price.multiply(BigDecimal.valueOf(0.95));
        }

        ticket.setPrice(price);
    }

    public void calculateFare(Ticket ticket){
        calculateFare(ticket, false);
    }
}