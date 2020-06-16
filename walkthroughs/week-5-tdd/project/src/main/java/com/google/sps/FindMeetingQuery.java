// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.Collections;
import com.google.sps.Event;
import com.google.sps.TimeRange;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;

public final class FindMeetingQuery {
  private boolean eventParticipantInMeeting(Set<String> eventAttendees, 
                                              Collection<String> meetingAttendees) {
    for (String eventAttendee : eventAttendees) {
      if (meetingAttendees.contains(eventAttendee)) {
        return true;
      }
    }
    return false;
  }

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<TimeRange> openMeetingTimes = new ArrayList<>();
    List<Event> eventList = new ArrayList<>(events);

    // Remove any events where there are no participants in event that are in meeting request.
    for(Iterator<Event> it = eventList.iterator(); it.hasNext();) {
      Event cur = it.next();
      if (eventParticipantInMeeting(cur.getAttendees(), request.getAttendees()) == false) {
        it.remove();
      }
    }
    Collections.sort(eventList, new Comparator<Event>() {
      @Override
      public int compare(Event a, Event b) {
        return Long.compare(a.getWhen().start(), b.getWhen().start());
      }
    });

    // Create start/end of day reference points to avoid extra conditionals outside main loop.
    eventList.add(new Event("EOD", 
        TimeRange.fromStartDuration(TimeRange.END_OF_DAY,0), request.getAttendees()));
    int endOfEarlierEvent = TimeRange.START_OF_DAY;
    
    for (int i = 0; i < eventList.size(); i++) {
      Event curEvent = eventList.get(i);
      int startOfCurEvent = curEvent.getWhen().start();

      boolean endTimeIsInclusive = false;
      if ("EOD".equals(curEvent.getTitle())) {
        endTimeIsInclusive = true;
      }
      TimeRange timeBetweenEvents = TimeRange.fromStartEnd(endOfEarlierEvent, 
                                        startOfCurEvent, endTimeIsInclusive);
      if (timeBetweenEvents.duration() >= request.getDuration()) {
        openMeetingTimes.add(timeBetweenEvents);
      }

      // Only move reference points of end of last event if the cur event end point is later 
      // than the end of the event with the latest end point so far.
      int endOfCurEvent = curEvent.getWhen().end();
      if (endOfCurEvent >= endOfEarlierEvent) {
        endOfEarlierEvent = endOfCurEvent;
      }
    }

    return openMeetingTimes;
  }
}
