package com.enernoc.open.oadr2.vtn

import javax.xml.datatype.Duration;

class EventSignal {

    static belongsTo = [event: Event]
    static hasMany = [intervals: EventInterval]
    static fetchMode = [intervals:'eager']
    
    String signalID = UUID.randomUUID().toString()
    String name = "simple"
    SignalType type = SignalType.LEVEL
    
    static constraints = {
        event nullable: false
        signalID blank: false 
        name blank: false
        type nullable: false
        intervals validator: EventSignal.&validateIntervals
    }
    
    public EventInterval getCurrentInterval() {
        Date now = new Date()
        /* + GGA : avoid display side effect of last log.warn */
        boolean intervalValidated = false;
        /* - GGA */

        // TODO not sure if this is correct:
        if ( event.cancelled || now > event.endDate )
            return null
        
        if  ( now < event.startDate )
            return null
        
        // at this point assume we're somewhere inside the event window
        def intervalEnd = event.startDate.time
        this.intervals.each { interval ->
            intervalEnd += interval.durationMillis
            if ( intervalEnd > now.time ) { // we're in this interval
                /* +GGA : display interval and manage display side effect */
                log.debug "Interval validated : $interval.level"
                intervalValidated = true;
                return interval.level
                /* -GGA */
            }
        }

        /* +GGA : seems that we have display side effect when we only have log.warn at function end => message seen even if interval found */
        if (!intervalValidated) {
            log.warn "Couldn't find an interval for event $event"
        }
        /* -GGA */
        return null
    }
    
    static validateIntervals( intervals, EventSignal signal ) {
      if ( intervals.size() < 1 ) return "empty"
      def eventLength = signal.event.durationMillis
      
      def intervalDuration = 0
      intervals.each { intervalDuration += it.durationMillis }
      if ( eventLength != intervalDuration ) {
          return "duration" // durations do not add up to event duration
      }
    } 
}


