package edu.nyu.pqs.stopwatch.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import edu.nyu.pqs.stopwatch.api.IStopwatch;

/**
 * The StopwatchFactory is a thread-safe factory class for IStopwatch objects.
 * It maintains references to all created IStopwatch objects and provides a
 * convenient method for getting a list of those objects.
 * 
 */
public class StopwatchFactory {
  private final static Object lock = new Object();
  private static ConcurrentMap<String, IStopwatch> stopWatches = new ConcurrentHashMap<String, IStopwatch>();
  
  // no need to create its object as it only has static methods 
  private StopwatchFactory(){
    
  }
  
  /**
   * Creates and returns a new IStopwatch object which underneath is an 
   * instance of Stopwatch. 
   * @param id
   *          The identifier of the new object
   * @return The new IStopwatch object
   * @throws IllegalArgumentException
   *           if <code>id</code> is empty, null, or already taken.
   */
  public static IStopwatch getStopwatch(String id) {
    synchronized (lock) {
      if(stopWatches.containsKey(id)){
        throw new IllegalArgumentException("id is already in use");
      }
      IStopwatch sw = new Stopwatch(id);
      stopWatches.put(id, sw);
      return sw;
    }
  }

  /**
   * Returns a new list of all the created stopwatches.
   * As the values() doesn't give guarantee of consistency so it is also 
   * synchronized on lock so that no modification can be made on stopwatches
   * @return a List of all creates IStopwatch objects. Returns an empty list if
   *         no IStopwatches have been created.
   */
  public static List<IStopwatch> getStopwatches() {
    synchronized (lock) {
      return new ArrayList<IStopwatch>(stopWatches.values());
    }
  }
  
  /**
   * Creates and returns a new IStopwatch object which underneath is an 
   * instance of Stopwatch. 
   * @param id
   *          The identifier of the new object
   * @return The new IStopwatch object
   * @throws IllegalArgumentException
   *           if <code>id</code> is empty, null, or already taken.
   */
  public static IStopwatch getFairStopwatch(String id) {
    synchronized (lock) {
      if(stopWatches.containsKey(id)){
        throw new IllegalArgumentException("id is already in use");
      }
      IStopwatch sw = new FairStopwatch(id);
      stopWatches.put(id, sw);
      return sw;
    }
  }
}
