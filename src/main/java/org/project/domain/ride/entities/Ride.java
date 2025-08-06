package org.project.domain.ride.entities;

import org.project.domain.ride.value_object.*;
import org.project.domain.shared.annotations.Nullable;

import java.util.Objects;

import static org.project.domain.shared.util.Utils.required;

public class Ride {
  private final RideID id;
  private final RideOwner rideOwner;
  private final Route route;
  private final RideTime rideTime;
  private final Price price;
  private final boolean isDeliveryAvailable;
  private final @Nullable Price deliveryPrice;

  private Ride(
          RideID id,
          RideOwner rideOwner,
          Route route,
          RideTime rideTime,
          Price price,
          boolean isDeliveryAvailable,
          Price deliveryPrice) {

    this.id = id;
    this.rideOwner = rideOwner;
    this.route = route;
    this.rideTime = rideTime;
    this.price = price;
    this.isDeliveryAvailable = isDeliveryAvailable;
    this.deliveryPrice = deliveryPrice;
  }

  public static Ride of(
          RideOwner rideOwner,
          Route route,
          RideTime rideTime,
          Price price) {

    required("rideOwner", rideOwner);
    required("route", route);
    required("rideTime", rideTime);
    required("price", price);

    return new Ride(RideID.newID(),  rideOwner, route, rideTime, price, false, null);
  }

  public static Ride of(
          RideOwner rideOwner,
          Route route,
          RideTime rideTime,
          Price price,
          Price deliveryPrice) {

    required("rideOwner", rideOwner);
    required("route", route);
    required("rideTime", rideTime);
    required("price", price);
    required("deliveryPrice", deliveryPrice);

    return new Ride(RideID.newID(),  rideOwner, route, rideTime, price, true, deliveryPrice);
  }

  public static Ride fromRepository(
          RideID id,
          RideOwner rideOwner,
          Route route,
          RideTime rideTime,
          Price price,
          boolean isDeliveryAvailable,
          Price deliveryPrice) {

    return new Ride(id, rideOwner, route, rideTime, price, isDeliveryAvailable, deliveryPrice);
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

  public RideTime rideTime() {
    return rideTime;
  }

  public Price price() {
    return price;
  }

  public boolean isDeliveryAvailable() {
    return isDeliveryAvailable;
  }

  public Price deliveryPrice() {
    return deliveryPrice;
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
