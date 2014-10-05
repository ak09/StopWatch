package edu.nyu.pqs.stopwatch.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import edu.nyu.pqs.stopwatch.api.IStopwatch;

/**
 * This class is an thread-safe implementation of IStopwatch.
 * For mutual exclusion it synchronizes on Object lock. It uses AtomicBoolean  
 * to maintain the state of the stopwatch. Before synchronizing it checks the 
 * state of the stopwatch (where ever state matters), using AtomicBoolean 
 * because acquiring lock is a heavy operation. It again checks the state after
 * acquiring the lock.
 * @author ajaykhanna
 *
 */
public class Stopwatch implements IStopwatch {
  private String id;
  private List<Long> lapTimes = Collections.synchronizedList(new ArrayList<Long>());
  private AtomicLong lastStartorLapTime = new AtomicLong(0L);
  private AtomicBoolean running = new AtomicBoolean(false);
  private final static Long ZERO = new Long(0L);
  private final static Long NANOTOMILLI = new Long(1000000L);
  private final Object lock = new Object();
  
  /**
   * Constructs a new Stopwatch.
   * @param id stopwatch id
   * @throws IllegalArgumentException if <code>id</code> is empty, null.
   */
  public Stopwatch(String id){
    if(id == null || id.isEmpty()){
      throw new IllegalArgumentException("id is invalid");
    }
    this.id = id;
  }
  
  @Override
  public String getId() {
    return id;
  }

  /**
   * State of the stopwatch matters in this method. 
   * @throws IllegalStateException if stopwatch is running
   */
  @Override
  public void start() {
    if(running.get()){
      throw new IllegalStateException("The Stopwatch is running");
    }
    synchronized (lock) {
      Long currentTime = System.nanoTime()/NANOTOMILLI;
      Long lapTimeElapsed = ZERO; 
      if(running.getAndSet(true)){
        throw new IllegalStateException("The Stopwatch is running");
      }
      if(lapTimes.size() !=0){
        lapTimeElapsed = lapTimes.get(lapTimes.size()-1);
        lapTimes.remove(lapTimes.size()-1);
      }
      lastStartorLapTime.set(currentTime - lapTimeElapsed);
    }
  }

  /**
   * State of the stopwatch matters in this method. 
   * @throws IllegalStateException if stopwatch is not running
   */
  @Override
  public void lap() {
    if(!running.get()){
      throw new IllegalStateException("The Stopwatch is not running");
    }
    synchronized (lock) {
      Long currentTime = System.nanoTime()/NANOTOMILLI;
      if(!running.get()){
        throw new IllegalStateException("The Stopwatch is not running");
      }
      lapTimes.add(currentTime- lastStartorLapTime.get());
      lastStartorLapTime.set(currentTime);
    }
  }

  /**
   * State of the stopwatch matters in this method. 
   * @throws IllegalStateException if stopwatch is not running
   */
  @Override
  public void stop() {
    if(!running.get()){
      throw new IllegalStateException("The Stopwatch is not running");
    }
    synchronized (lock) {
      Long currentTime = System.nanoTime()/NANOTOMILLI;
      if(!running.getAndSet(false)){
        throw new IllegalStateException("The Stopwatch is not running");
      }
      lapTimes.add(currentTime - lastStartorLapTime.get());
    }
  }
  
  /**
   * State of the stopwatch doesn't matter in this method.  
   */
  @Override
  public void reset() {
    synchronized (lock) {
      running.set(false);
      lastStartorLapTime.set(ZERO);
      lapTimes.clear();
    }
  }

  /**
   * Makes a new copy to make sure user can't add into the stopwatch's list of 
   * laptimes.
   * @return new copy of List containing laptimes of the stopwatch 
   */
  @Override
  public List<Long> getLapTimes() {
    synchronized (lock) {
      return new ArrayList<Long>(lapTimes);
    }
  }

  @Override
  public String toString() {
    return "Stopwatch id is " + id;
  }
}
