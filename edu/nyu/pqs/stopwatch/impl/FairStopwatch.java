package edu.nyu.pqs.stopwatch.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import edu.nyu.pqs.stopwatch.api.IStopwatch;

public class FairStopwatch implements IStopwatch{
  private String id;
  private List<Long> lapTimes = Collections.synchronizedList(new ArrayList<Long>());
  private AtomicLong lastStartorLapTime = new AtomicLong(0L);
  private AtomicBoolean running = new AtomicBoolean(false);
  private final static Long ZERO = new Long(0L);
  private final static Long NANOTOMILLI = new Long(1000000L);
  private final ReentrantLock lock = new ReentrantLock(true);
  
  /**
   * Constructs a new Stopwatch.
   * @param id stopwatch id
   * @throws IllegalArgumentException if <code>id</code> is empty, null.
   */
  public FairStopwatch(String id){
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
    lock.lock();
    try{
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
    finally{
      lock.unlock();
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
    lock.lock();
    try{
      Long currentTime = System.nanoTime()/NANOTOMILLI;
      if(!running.get()){
        throw new IllegalStateException("The Stopwatch is not running");
      }
      lapTimes.add(currentTime- lastStartorLapTime.get());
      lastStartorLapTime.set(currentTime);
    }
    finally{
      lock.unlock();
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
    lock.lock();
    try{
      Long currentTime = System.nanoTime()/NANOTOMILLI;
      if(!running.getAndSet(false)){
        throw new IllegalStateException("The Stopwatch is not running");
      }
      lapTimes.add(currentTime - lastStartorLapTime.get());
    }
    finally{
      lock.unlock();
    }
  }
  
  /**
   * State of the stopwatch doesn't matter in this method.  
   */
  @Override
  public void reset() {
    lock.lock();
    try{
      running.set(false);
      lastStartorLapTime.set(ZERO);
      lapTimes.clear();
    }
    finally{
      lock.unlock();
    }
  }

  /**
   * Makes a new copy to make sure user can't add into the stopwatch's list of 
   * laptimes.
   * @return new copy of List containing laptimes of the stopwatch 
   */
  @Override
  public List<Long> getLapTimes() {
    lock.lock();
    try{
      return new ArrayList<Long>(lapTimes);
    }
    finally{
      lock.unlock();
    }
  }

  @Override
  public String toString() {
    return "Stopwatch id is " + id;
  }
}
