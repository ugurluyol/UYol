package org.project.domain.ride.entities;

import org.project.domain.ride.enumerations.RideRule;
import org.project.domain.ride.enumerations.RideStatus;
import org.project.domain.ride.value_object.*;
import org.project.domain.shared.exceptions.IllegalDomainArgumentException;
import org.project.domain.shared.value_objects.CarID;
import org.project.domain.shared.value_objects.Dates;
import org.project.domain.shared.value_objects.UserID;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.project.domain.shared.util.Utils.required;

public class Ride {
  public static final int MAX_RIDE_RULES = 12;

  private final RideID id;
  private final CarID carID;
  private final RideOwner rideOwner;
  private final Route route;
  private final RideTime rideTime;
  private final Price price;
  private SeatMap seatMap;
  private RideStatus status;
  private final RideDesc rideDesc;
  private final Set<RideRule> rideRules;
  private final Dates dates;
  private final boolean hasActiveContract;
  private Fee fee;

  private Ride(
          RideID id,
          CarID carID,
          RideOwner rideOwner,
          Route route,
          RideTime rideTime,
          Price price,
          SeatMap seatMap,
          RideStatus status,
          RideDesc rideDesc,
          Set<RideRule> rideRules,
          Dates dates,
          boolean hasActiveContract,
          Fee fee) {

    this.id = id;
    this.carID = carID;
    this.rideOwner = rideOwner;
    this.route = route;
    this.rideTime = rideTime;
    this.price = price;
    this.seatMap = seatMap;
    this.status = status;
    this.rideRules = rideRules;
    this.rideDesc = rideDesc;
    this.dates = dates;
    this.hasActiveContract = hasActiveContract;
    this.fee = fee;
  }

  public static Ride of(
          CarID carID,
          RideOwner rideOwner,
          Route route,
          RideTime rideTime,
          Price price,
          SeatMap seatMap,
          RideDesc rideDesc,
          Set<RideRule> rideRules) {

    required("carID", carID);
    required("rideOwner", rideOwner);
    required("route", route);
    required("rideTime", rideTime);
    required("price", price);
    required("seatMap", seatMap);
    required("rideDesc", rideDesc);
    required("rideRules", rideRules);
    if (rideRules.size() > MAX_RIDE_RULES)
      throw new IllegalDomainArgumentException("Too many rules for ride, don't be so boring");

    return new Ride(RideID.newID(), carID, rideOwner, route, rideTime, price,
            seatMap, RideStatus.PENDING, rideDesc, rideRules, Dates.defaultDates(), false, Fee.zero());
  }

  public static Ride fromRepository(
          RideID id,
          CarID carID,
          RideOwner rideOwner,
          Route route,
          RideTime rideTime,
          Price price,
          SeatMap seatMap,
          RideStatus status,
          RideDesc rideDesc,
          Set<RideRule> rideRules,
          Dates dates,
          boolean hasActiveContract,
          Fee fee) {

    return new Ride(id, carID, rideOwner, route, rideTime, price, seatMap, status, rideDesc, rideRules, dates, hasActiveContract, fee);
  }

  public RideID id() {
    return id;
  }

  public CarID carID() {
    return carID;
  }

  public RideOwner rideOwner() {
    return rideOwner;
  }

  public Route route() {
    return route;
  }

  public RideTime rideTime() {
    return rideTime;
  }

  public Price price() {
    return price;
  }

  public SeatMap seatMap() {
    return seatMap;
  }

  public RideContract book(UserID userID, BookedSeats bookedSeats) {
    if (this.status != RideStatus.PENDING)
      throw new IllegalDomainArgumentException("Cannot add passenger when ride is already on the road");

    SeatMap newSeatsState = null;
    BigDecimal total = BigDecimal.ZERO;
    for (PassengerSeat bookedSeat : bookedSeats.bookedSeats()) {
      newSeatsState = seatMap.occupy(bookedSeat.index(), bookedSeat.status());
      total = total.add(price.amount());
    }

    this.seatMap = newSeatsState;
    this.fee = fee.calculateFeeForBooking(new Price(total));
    return RideContract.of(userID, id, price, bookedSeats);
  }

  public RideStatus status() {
    return status;
  }

  public void start() {
    if (this.status != RideStatus.PENDING)
      throw new IllegalDomainArgumentException("Ride already started or canceled/finished");

    this.status = RideStatus.ON_THE_ROAD;
  }

  public void cancel() {
    if (this.status != RideStatus.PENDING)
      throw new IllegalDomainArgumentException("Ride cancellation is not possible if it`s already on the road or finished");
    if (hasActiveContract)
      throw new IllegalDomainArgumentException("You already have contracts with passengers, so ride cannot be cancelled");

    this.status = RideStatus.CANCELED;
  }

  public void finish() {
    if (this.status != RideStatus.ON_THE_ROAD)
      throw new IllegalDomainArgumentException("You can`t finish the ride which was not going");

    this.status = RideStatus.ENDED_SUCCESSFULLY;
  }

  public RideDesc rideDesc() {
    return rideDesc;
  }

  public Set<RideRule> rideRules() {
    return new HashSet<>(rideRules);
  }

  public void addRideRule(RideRule rideRule) {
    required("rideRule", rideRule);

    if (status != RideStatus.PENDING)
      throw new IllegalDomainArgumentException("You can`t add rules when ride is already active.");

    if (rideRules.size() > MAX_RIDE_RULES)
      throw new IllegalDomainArgumentException("Too much ride rules");

    rideRules.add(rideRule);
  }

  public void removeRideRule(RideRule rideRule) {
    required("rideRule", rideRule);

    if (status != RideStatus.PENDING)
      throw new IllegalDomainArgumentException("You can`t remove rules when ride is already active.");

    rideRules.remove(rideRule);
  }

  public Dates dates() {
    return dates;
  }

  public boolean hasActiveContract() {
    return hasActiveContract;
  }

  public Fee fee() {
    return fee;
  }

  public boolean isModifiable() {
    return status == RideStatus.PENDING;
  }

  public boolean isActive() {
    return status == RideStatus.ON_THE_ROAD;
  }

  public boolean isFinished() {
    return status == RideStatus.ENDED_SUCCESSFULLY;
  }

  public boolean hasRule(RideRule rule) {
    return rideRules.contains(rule);
  }

  public boolean canAcceptPassenger() {
    return isModifiable() && seatMap.hasAvailableSeats();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Ride ride = (Ride) o;
    return Objects.equals(id, ride.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
