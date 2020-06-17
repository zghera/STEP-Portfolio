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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class FindMeetingQuery {
  /**
   * Returns a boolean based on if at least one participant in the event is also a participant in
   * the meeting request.
   *
   * @param eventAttendeesCopy A copy of the set of attendees in a specific event.
   * @param meetingAttendees The set of attendees in the meeting request.
   * @return True if at least one participant in the set {@code eventAttendees} is also a
   *     participant in {@code meetingAttendees}. False otherwise.
   */
  private boolean eventParticipantInMeeting(
      Set<String> eventAttendeesCopy, Collection<String> meetingAttendees) {
    eventAttendeesCopy.retainAll(meetingAttendees);
    if (eventAttendeesCopy.isEmpty()) {
      return false;
    }
    return true;
  }

  /**
   * Remove any events where it does not contains at least one participant that is included in the
   * meeting request.
   *
   * @param eventList The list of events that are used to determine what periods of time that the
   *     meeting can take place. This list is 'filtered' and sorted in {@code query()}.
   * @param request The meeting request that contains the requirements for potential meetings.
   */
  private void removeEventsWithNoMeetingAttendees(List<Event> eventList, MeetingRequest request) {
    for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
      Event cur = it.next();
      if (!eventParticipantInMeeting(new HashSet<String>(cur.getAttendees()), 
                                     request.getAttendees())) {
        it.remove();
      }
    }
  }

  /**
   * Returns a collection of all meeting times that will work for all attendees' schedules.
   *
   * <p>The open meeting times are determined by iterating through each event and creating a new
   * {@code TimeRange} for each range between meetings. If the duration is greater than or equal to
   * the duration of the requested meeting, that {@code TimeRange} instance is added to the list
   * {@code openMeetingTimes}. The initial value of {@code endOfEarlierEvent} is set to the start of
   * the day for convenience when evaluating the first event. Also, the end of day time is appended
   * as an additional event to the current event list to avoid more conditional statements.
   *
   * @param eventList The list of events that are used to determine what periods of time that the
   *     meeting can take place. This list is 'filtered' and sorted in {@code query()}.
   * @param request The meeting request that contains the requirements for potential meetings.
   * @return A Collection of the feasible meeting {@code TimeRange}s.
   */
  private Collection<TimeRange> getMeettingTimes(List<Event> eventList, MeetingRequest request) {
    eventList.add(
        new Event(
            "EOD", TimeRange.fromStartDuration(TimeRange.END_OF_DAY, 0), request.getAttendees()));
    int endOfEarlierEvent = TimeRange.START_OF_DAY;

    Collection<TimeRange> openMeetingTimes = new ArrayList<>();
    for (int i = 0; i < eventList.size(); i++) {
      Event curEvent = eventList.get(i);
      int startOfCurEvent = curEvent.getWhen().start();

      boolean endTimeIsInclusive = false;
      if ("EOD".equals(curEvent.getTitle())) {
        endTimeIsInclusive = true;
      }
      TimeRange timeBetweenEvents =
          TimeRange.fromStartEnd(endOfEarlierEvent, startOfCurEvent, endTimeIsInclusive);
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

  /**
   * Returns a collection of all meeting times that will work for all attendees' schedules.
   *
   * <p>To determine the meeting times ({@code TimeRange}s) that works for all attendees, the event
   * list is filtered and sorted so that {@code getMeetingTimes()} can determine all feasible
   * meeting times. More specifically, filtered means that an event is removed if it does not
   * contain at least one participant that is included in the meeting request.
   *
   * @param events The Collection of events that are used to determine what periods of time that the
   *     meeting can take place.
   * @param request The Meeting request that contains the requirements for potential meetings.
   * @return A Collection of the feasible meeting {@code TimeRange}s.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    List<Event> eventList = new ArrayList<>(events);

    removeEventsWithNoMeetingAttendees(eventList, request);

    Collections.sort(
        eventList,
        new Comparator<Event>() {
          @Override
          public int compare(Event a, Event b) {
            return Long.compare(a.getWhen().start(), b.getWhen().start());
          }
        });

    return getMeettingTimes(eventList, request);
  }
}
