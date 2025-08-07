package org.project.domain.ride.entities;

import org.project.domain.ride.enumerations.RideRule;
import org.project.domain.ride.enumerations.RideStatus;
import org.project.domain.ride.enumerations.SeatStatus;
import org.project.domain.ride.value_object.*;
import org.project.domain.shared.annotations.Nullable;
import org.project.domain.shared.exceptions.IllegalDomainArgumentException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.project.domain.shared.util.Utils.required;

public class Ride {
  public static final int MAX_RIDE_RULES = 12;

  private final RideID id;
  private final RideOwner rideOwner;
  private Route route;
  private final RideTime rideTime;
  private final Price price;
  private SeatMap seatMap;
  private RideStatus status;
  private boolean isDeliveryAvailable;
  private @Nullable Price deliveryPrice;
  private final RideDesc rideDesc;
  private final Set<RideRule> rideRules;

  private Ride(
          RideID id,
          RideOwner rideOwner,
          Route route,
          RideTime rideTime,
          Price price,
          SeatMap seatMap,
          RideStatus status,
          boolean isDeliveryAvailable,
          Price deliveryPrice,
          RideDesc rideDesc,
          Set<RideRule> rideRules) {

    this.id = id;
    this.rideOwner = rideOwner;
    this.route = route;
    this.rideTime = rideTime;
    this.price = price;
    this.seatMap = seatMap;
    this.status = status;
    this.isDeliveryAvailable = isDeliveryAvailable;
    this.deliveryPrice = deliveryPrice;
    this.rideRules = rideRules;
    this.rideDesc = rideDesc;
  }

  public static Ride of(
          RideOwner rideOwner,
          Route route,
          RideTime rideTime,
          Price price,
          SeatMap seatMap,
          RideDesc rideDesc,
          Set<RideRule> rideRules) {

    required("rideOwner", rideOwner);
    required("route", route);
    required("rideTime", rideTime);
    required("price", price);
    required("seatMap", seatMap);
    required("rideDesc", rideDesc);
    required("rideRules", rideRules);
    if (rideRules.size() > MAX_RIDE_RULES)
      throw new IllegalDomainArgumentException("Too many rules for ride, don't be so boring");

    return new Ride(RideID.newID(),  rideOwner, route, rideTime, price, seatMap, RideStatus.PENDING, false, null, rideDesc,rideRules);
  }

  public static Ride of(
          RideOwner rideOwner,
          Route route,
          RideTime rideTime,
          SeatMap seatMap,
          Price price,
          Price deliveryPrice,
          RideDesc rideDesc,
          Set<RideRule> rideRules) {

    required("rideOwner", rideOwner);
    required("route", route);
    required("rideTime", rideTime);
    required("price", price);
    required("deliveryPrice", deliveryPrice);
    required("seatMap", seatMap);
    required("rideDesc", rideDesc);
    required("rideRules", rideRules);
    if (rideRules.size() > MAX_RIDE_RULES)
      throw new IllegalDomainArgumentException("Too many rules for ride, don't be so boring");

    return new Ride(RideID.newID(),  rideOwner, route, rideTime, price, seatMap,
            RideStatus.PENDING, true, deliveryPrice, rideDesc, rideRules);
  }

  public static Ride fromRepository(
          RideID id,
          RideOwner rideOwner,
          Route route,
          RideTime rideTime,
          Price price,
          SeatMap seatMap,
          RideStatus status,
          boolean isDeliveryAvailable,
          Price deliveryPrice,
          RideDesc rideDesc,
          Set<RideRule> rideRules) {

    return new Ride(id, rideOwner, route, rideTime, price, seatMap, status, isDeliveryAvailable, deliveryPrice, rideDesc, rideRules);
  }

  public RideID id() {
    return id;
  }

  public RideOwner rideOwner() {
    return rideOwner;
  }

  public Route route() {
    return route;
  }

  public Route addStop(Location location) {
    if (status != RideStatus.PENDING)
      throw new IllegalDomainArgumentException("Cannot add stop when ride is already on the road");

    this.route = this.route.addStop(location);
    return route;
  }

  public Route removeStop(Location location) {
    if (status != RideStatus.PENDING)
      throw new IllegalDomainArgumentException("Cannot remove stop when ride is already on the road");

    this.route = this.route.removeStop(location);
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

  public SeatMap changePassenger(int index, SeatStatus status) {
    if (this.status != RideStatus.PENDING)
      throw new IllegalDomainArgumentException("Cannot add passenger when ride is already on the road");

    this.seatMap = seatMap.changePassenger(index, status);
    return seatMap;
  }

  public SeatMap removePassenger(int index) {
    if (this.status != RideStatus.PENDING)
      throw new IllegalDomainArgumentException("Cannot remove passenger when ride is already on the road");

    this.seatMap = seatMap.releaseSeat(index);
    return seatMap;
  }

  public RideStatus status() {
    return status;
  }

  public void start() {
    if (this.status != RideStatus.PENDING)
      throw new IllegalDomainArgumentException("Ride already started");

    this.status = RideStatus.ON_THE_ROAD;
  }

  public void cancel() {
    if (this.status != RideStatus.PENDING && this.status != RideStatus.ON_THE_ROAD)
      throw new IllegalDomainArgumentException("Ride cancellation is not possible if it`s already finished");

    this.status = RideStatus.CANCELED;
  }

  public void finish() {
    if (this.status != RideStatus.ON_THE_ROAD)
      throw new IllegalDomainArgumentException("You can`t finish the ride which was not going");

    this.status = RideStatus.ENDED_SUCCESSFULLY;
  }

  public boolean isDeliveryAvailable() {
    return isDeliveryAvailable;
  }

  public Price deliveryPrice() {
    return deliveryPrice;
  }

  public void enableDelivery(Price deliveryPrice) {
    required("deliveryPrice", deliveryPrice);

    if (isDeliveryAvailable)
      throw new IllegalDomainArgumentException("Delivery is already available");

    if (status != RideStatus.PENDING)
      throw new IllegalDomainArgumentException("You cannot modify ride if it`s already started.");

    this.isDeliveryAvailable = true;
    this.deliveryPrice = deliveryPrice;
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

  public boolean isModifiable() {
    return status == RideStatus.PENDING;
  }

  public boolean isActive() {
    return status == RideStatus.ON_THE_ROAD;
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
