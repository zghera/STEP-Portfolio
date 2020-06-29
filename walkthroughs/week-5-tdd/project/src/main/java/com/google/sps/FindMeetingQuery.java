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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class FindMeetingQuery {
  /**
   * Returns a boolean if at least one participant in the event is also a meeting attendee from the
   * mandatory and/or optional attendee list.
   *
   * @param eventAttendeesCopy A copy of the set of attendees in a specific event.
   * @param meetingAttendees The set of attendees in the meeting request.
   * @return True if at least one participant in the set {@code eventAttendees} is also a
   *     participant in {@code meetingAttendees}. False otherwise.
   */
  private boolean eventAttendeeIsMeetingAttendee(
      Set<String> eventAttendeesCopy, Collection<String> meetingAttendees) {
    eventAttendeesCopy.retainAll(meetingAttendees);
    return !eventAttendeesCopy.isEmpty();
  }

  /**
   * Returns a collection of all meeting times that will work for all attendees' schedules.
   *
   * <p>Initially, the meeting attendees is set based on {@code considerOptionalAttendees}. Once
   * this is determined, the event list is filtered and sorted. More specifically, the events are
   * filtered if it does not contain at least one participant that is also a meeting participant.
   * The open meeting times are determined by iterating through each event and creating a new {@code
   * TimeRange} for each range between meetings. If the duration is greater than or equal to the
   * duration of the requested meeting, that {@code TimeRange} instance is added to the list {@code
   * openMeetingTimes}. The initial value of {@code endOfEarlierEvent} is set to the start of the
   * day for convenience when evaluating the first event. Also, the end of day time is appended as
   * an additional event to the current event list to avoid more conditional statements.
   *
   * @param eventList The list of events that are used to determine what periods of time that the
   *     meeting can take place. This list is 'filtered' and sorted in {@code query()}.
   * @param request The meeting request that contains the requirements for potential meetings.
   * @return A Collection of the feasible meeting {@code TimeRange}s.
   */
  private Collection<TimeRange> getMeetingTimes(
      Collection<Event> events, MeetingRequest request, boolean considerOptionalAttendees) {
    Collection<String> meetingAttendees = new HashSet<String>(request.getAttendees());
    if (considerOptionalAttendees) {
      meetingAttendees.addAll(request.getOptionalAttendees());
    }

    List<Event> eventList =
        events.stream()
            .filter(event
                -> eventAttendeeIsMeetingAttendee(
                    new HashSet<String>(event.getAttendees()), meetingAttendees))
            .sorted(new Comparator<Event>() {
              @Override
              public int compare(Event a, Event b) {
                return Long.compare(a.getWhen().start(), b.getWhen().start());
              }
            })
            .collect(Collectors.toList());

    String eod_title = "EOD";
    eventList.add(new Event(
        eod_title, TimeRange.fromStartDuration(TimeRange.END_OF_DAY, 0), request.getAttendees()));
    int endOfEarlierEvent = TimeRange.START_OF_DAY;

    Collection<TimeRange> openMeetingTimes = new ArrayList<>();
    for (Event curEvent : eventList) {
      int startOfCurEvent = curEvent.getWhen().start();

      boolean endTimeIsInclusive = eod_title.equals(curEvent.getTitle());
      TimeRange timeBetweenEvents =
          TimeRange.fromStartEnd(endOfEarlierEvent, startOfCurEvent, endTimeIsInclusive);
      if (timeBetweenEvents.duration() >= request.getDuration()) {
        openMeetingTimes.add(timeBetweenEvents);
      }

      // Only move reference points of end of last event if the cur event end point is later
      // than the end of the event with the latest end point so far.
      endOfEarlierEvent = max(endOfEarlierEvent, curEvent.getWhen().end());
    }

    return openMeetingTimes;
  }

  /**
   * Returns a collection of all meeting times that will work for attendees' schedules.
   *
   * <p>Initially, meeting times ({@code TimeRange}s) that will works for both mandatory and
   * optional attendees are identified. If there are no meeting times that work for all attendees,
   * only meeting times with mandatory attendees are identified and returned.
   *
   * @param events The Collection of events that are used to determine what periods of time that the
   *     meeting can take place.
   * @param request The Meeting request that contains the requirements for potential meetings.
   * @return A Collection of the feasible meeting {@code TimeRange}s.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<TimeRange> mandatoryAndOptionalTimesList =
        getMeetingTimes(events, request, /*considerOptionalAttendees=*/true);

    // In the special case where there are no mandatory attendees, at least one optional attendee,
    // and no times work out, return an empty list rather than considering only mandatory attendees.
    // This will result in a time range for the whole day and is not desirable.
    if (!mandatoryAndOptionalTimesList.isEmpty()
        || request.getAttendees().isEmpty() && !request.getOptionalAttendees().isEmpty()) {
      return mandatoryAndOptionalTimesList;
    }

    return getMeetingTimes(events, request, /*considerOptionalAttendees=*/false);
  }
}
