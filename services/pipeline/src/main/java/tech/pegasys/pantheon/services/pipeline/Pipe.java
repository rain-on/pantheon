/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.services.pipeline;

import tech.pegasys.pantheon.metrics.Counter;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Forms the connection between two pipeline stages. A pipe is essentially a blocking queue with the
 * added ability to signal when no further input is available because the pipe has been closed or
 * the pipeline aborted.
 *
 * <p>In most cases a Pipe is used through one of two narrower interfaces it supports {@link
 * ReadPipe} and {@link WritePipe}. These are designed to expose only the operations relevant to
 * objects either reading from or publishing to the pipe respectively.
 *
 * @param <T> the type of item that flows through the pipe.
 */
public class Pipe<T> implements ReadPipe<T>, WritePipe<T> {
  private static final Logger LOG = LogManager.getLogger();
  private final BlockingQueue<T> queue;
  private final int capacity;
  private final Counter inputCounter;
  private final Counter outputCounter;
  private final AtomicBoolean closed = new AtomicBoolean();
  private final AtomicBoolean aborted = new AtomicBoolean();

  public Pipe(final int capacity, final Counter inputCounter, final Counter outputCounter) {
    queue = new ArrayBlockingQueue<>(capacity);
    this.capacity = capacity;
    this.inputCounter = inputCounter;
    this.outputCounter = outputCounter;
  }

  @Override
  public boolean isOpen() {
    return !closed.get() && !aborted.get();
  }

  /**
   * Get the number of items that can be queued inside this pipe.
   *
   * @return the pipe's capacity.
   */
  public int getCapacity() {
    return capacity;
  }

  @Override
  public boolean hasRemainingCapacity() {
    return queue.remainingCapacity() > 0 && isOpen();
  }

  @Override
  public void close() {
    closed.set(true);
  }

  @Override
  public void abort() {
    aborted.set(true);
  }

  @Override
  public boolean hasMore() {
    if (aborted.get()) {
      return false;
    }
    return !closed.get() || !queue.isEmpty();
  }

  @Override
  public T get() {
    try {
      while (hasMore()) {
        final T value = queue.poll(1, TimeUnit.SECONDS);
        if (value != null) {
          outputCounter.inc();
          return value;
        }
      }
    } catch (final InterruptedException e) {
      LOG.trace("Interrupted while waiting for next item", e);
    }
    return null;
  }

  @Override
  public T poll() {
    final T item = queue.poll();
    if (item != null) {
      outputCounter.inc();
    }
    return item;
  }

  @Override
  public void drainTo(final Collection<T> output, final int maxElements) {
    final int count = queue.drainTo(output, maxElements);
    outputCounter.inc(count);
  }

  @Override
  public void put(final T value) {
    while (isOpen()) {
      try {
        if (queue.offer(value, 1, TimeUnit.SECONDS)) {
          inputCounter.inc();
          return;
        }
      } catch (final InterruptedException e) {
        LOG.trace("Interrupted while waiting to add to output", e);
      }
    }
  }
}
