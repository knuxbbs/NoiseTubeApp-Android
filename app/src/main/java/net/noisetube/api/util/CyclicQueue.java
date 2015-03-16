/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation)
 *
 *  Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 *  Portions contributed by Vrije Universiteit Brussel (BrusSense team), 2008-2012
 *  Portions contributed by University College London (ExCiteS group), 2012
 * --------------------------------------------------------------------------------
 *  This library is free software; you can redistribute it and/or modify it under
 *  the terms of the GNU Lesser General Public License, version 2.1, as published
 *  by the Free Software Foundation.
 *
 *  This library is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU Lesser General Public License along
 *  with this library; if not, write to:
 *    Free Software Foundation, Inc.,
 *    51 Franklin Street, Fifth Floor,
 *    Boston, MA  02110-1301, USA.
 *
 *  Full GNU LGPL v2.1 text: http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 *  NoiseTube project source code repository: http://code.google.com/p/noisetube
 * --------------------------------------------------------------------------------
 *  More information:
 *   - NoiseTube project website: http://www.noisetube.net
 *   - Sony Computer Science Laboratory Paris: http://csl.sony.fr
 *   - VUB BrusSense team: http://www.brussense.be
 * --------------------------------------------------------------------------------
 */

package net.noisetube.api.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Cyclic Queue implementation
 * <p/>
 * Method names loosely inspired by
 * http://download.oracle.com/javase/6/docs/api/java/util/Queue.html
 *
 * @author mstevens, maisonneuve, sbarthol, humberto
 */
public class CyclicQueue<T> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 7342032115948785281L;

    private static final int DEFAULT_CAPACITY = 20;

    private int capacity = 0;

    private ArrayList<T> queue;

    public CyclicQueue() {
        this(DEFAULT_CAPACITY);
    }

    public CyclicQueue(int capacity) {
        this.capacity = capacity;
        if (this.capacity <= 0) {
            throw new IllegalArgumentException("Queue capacity needs to be > 0");
        }
        this.queue = new ArrayList<T>(capacity);
    }

    public void clear() {

        this.queue.clear();
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int newCapacity) {

        if (newCapacity < 0) {
            return;

        } else if (newCapacity == 0) {
            queue.clear();

        } else if (newCapacity < capacity && !queue.isEmpty()) {
            int to = (queue.size() < newCapacity) ? queue.size() - 1 : newCapacity - 1;
            List<T> temp = new ArrayList<T>(queue.subList(0, to));
            queue.clear();
            queue.addAll(temp);
        }

        this.capacity = newCapacity;
    }

    /**
     * Returns the size of this queue. Notice that it can not be larger than the
     * capacity, but it is well possible that is is smaller, in case the queue
     * isn't completely filled.
     *
     * @return The size of the queue
     */
    public int getSize() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public boolean isFull() {
        return queue.size() == capacity;
    }

    /**
     * add an element to the queue (at "the back")
     *
     * @return the element that was dropped from the queue if it was already
     * full, null if it was not
     */
    public T offer(T o) {

        T dropped = null;
        if (isFull()) {
            dropped = queue.remove(0);
        }
        queue.add(o);

        return dropped;
    }


    public T removeFirstElement() {

        T dropped = null;

        if (getSize() == 1) {
            dropped = queue.remove(0);


        }


        return dropped;
    }

    /**
     * Retrieves and removes(!) the head of this queue, or null if this queue is
     * empty.
     *
     * @return the head of this queue (= oldest element), or null if this queue
     * is empty
     */
    public T poll() {
        try {
            return serve();
        } catch (NoSuchElementException e) {
            Logger.getInstance().error(e, "poll method");
            return null;
        }
    }

    /**
     * Retrieves and removes(!) the head of this queue (= the oldest element).
     * This method differs from the poll method in that it throws an exception
     * if this queue is empty.
     *
     * @return oldest element
     * @throws NoSuchElementException - if this queue is empty
     */
    public T serve() {
        if (!isEmpty()) {
            T toServe = queue.get(0);

            return toServe;
        } else
            throw new NoSuchElementException("Queue is empty");
    }

    /**
     * Retrieves, but does not remove, the head of this queue, returning null if
     * this queue is empty.
     *
     * @return the item at the head of the queue (= the oldest element still in
     * the queue)
     */
    public T peek() {
        if (!isEmpty())
            return queue.get(0);
        else
            return null;
    }

    /**
     * Retrieves, but does not remove, the head of this queue. This method
     * differs from the peek method only in that it throws an exception if this
     * queue is empty.
     *
     * @return the element at the head of the queue (= the oldest element still
     * in the queue)
     * @throws NoSuchElementException - if this queue is empty
     */
    public T head() {
        if (!isEmpty())
            return queue.get(0);
        else
            throw new NoSuchElementException("Queue is empty");
    }

    /**
     * Retrieves the last element which was added to the queue
     *
     * @return the most recently added element
     * @throws NoSuchElementException - if this queue is empty
     */
    public T tail() {
        return getElement(queue.size() - 1);
    }

    /**
     * Returns the element on logical position i, starting from 0, running till
     * size-1 A higher i denotes a newer (more recently offered) element.
     * <p/>
     * position index
     *
     * @return the element at logical position i
     * @throws NoSuchElementException - if this queue is empty
     */
    public T getElement(int i) {
        if (isEmpty())
            throw new NoSuchElementException("Queue is empty");
        return queue.get(i);
    }

    public T getElementByHashCode(int code) throws NoSuchElementException {
        if (isEmpty())
            throw new NoSuchElementException("Queue is empty");
        for (T item : queue) {
            if (item.hashCode() == code) {
                return item;
            }
        }

        throw new NoSuchElementException("The hashcode provided is incorrect.");
    }


    /**
     * Gives us an enumeration which enumerates starting from the oldest element
     * (the first element added).
     *
     * @return The enumeration.
     */
    public Enumeration<T> getEnumeration() {
        return new CQEnumerator<T>(this);
    }

    public ArrayList<T> getValues() {
        return queue;

    }

    public void setValues(ArrayList<T> queue) {
        this.queue = queue;
    }

    /**
     * Gives us a reversed enumeration which enumerates starting from the newest
     * element (the last element added).
     *
     * @return The reversed enumeration.
     */
    public Enumeration<T> getElementsReversed() {
        return new CQEnumerator<T>(this, true);
    }

    /**
     * Checks if a given object is in the queue
     * <p/>
     * object to look for
     *
     * @return boolean indicating presence or absence of the object
     */
    public boolean contains(T element) {
        Enumeration<T> enumerator = getEnumeration();
        while (enumerator.hasMoreElements()) {
            if (enumerator.nextElement() == element)
                return true;
        }
        return false;
    }
}

/**
 * Enumeration implementation for CyclicQueue
 * <p/>
 * Supports forwards and backwards traversals Note: this class is probably
 * NOT threadsafe
 *
 * @author mstevens, humberto
 */
class CQEnumerator<T> implements Enumeration<T> {

    private boolean reverse;
    private int currentPos;
    private CyclicQueue<T> queue;

    public CQEnumerator(CyclicQueue<T> queue) {
        this(queue, false);
        this.queue = queue;
    }

    public CQEnumerator(CyclicQueue<T> queue, boolean reverse) {
        this.reverse = reverse;
        currentPos = 0;
        this.queue = queue;
    }

    public boolean hasMoreElements() {
        return (currentPos < queue.getSize());
    }

    public T nextElement() {
        if (currentPos >= queue.getSize())
            throw new NoSuchElementException("No more elements");
        T element = (reverse ? queue.getElement(queue.getSize() - currentPos - 1)
                : queue.getElement(currentPos));
        currentPos++;
        return element;
    }

}
