/* Copyright (c) 2014 Stanford University
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR(S) DISCLAIM ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL AUTHORS BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package edu.stanford.ramcloud

import edu.stanford.ramcloud.ClientException._
import edu.stanford.ramcloud.multiop._
import java.util._

import TestClient._

//remove if not needed
import scala.collection.JavaConverters._

object TestClient {

  // Load C++ shared library for JNI
  Util.loadLibrary("ramcloud_java")

  /**
    * A simple end-to-end test of the java bindings.
    */
  def main(argv: Array[String]): Unit = {
    new TestClient().go(argv)
  }

  def printBytes(array: Array[Byte]): Unit = {
    System.out.print("[")
    for (i <- 0 until array.length - 1) {
      System.out.print(String.format("%02X", (array(i))) + ", ")
    }
    println(String.format("%02X", array(array.length - 1)) + "]")
  }

  /* native */
  @native def test(ramcloudClusterHandle: Long, arg: Long): Unit

}

/**
  * A Java RAMCloud client used for testing. Will contain sample code eventually.
  */
class TestClient {

  private var ramcloud: RAMCloud = _

  private var tableId: Long = _

  private def go(argv: Array[String]): Unit = {
    // Include a pause to add gdb if need to debug c++ code
    val debug: Boolean = false
    if (debug) {
      System.out.print("Press [Enter] to continue:")
      val scn: Scanner = new Scanner(System.in)
      scn.nextLine()
    }

    ramcloud = new RAMCloud(argv(0))
    // System.out.println("Created RamCloud object");
    tableId = ramcloud.createTable("hi")

    // basicSpeedTest();
    // enumerationTest();
    // Run whatever here
    multiReadTest()
    // multiWriteTest();
    // multiRemoveTest();
    // test();

    ramcloud.dropTable("hi")
    ramcloud.disconnect()

  }

  private def multiReadTest(): Unit = {
    val numTimes: Int = 5000
    val reads: Array[MultiReadObject] = Array.ofDim[MultiReadObject](numTimes)
    for (i <- 0 until numTimes) {
      val key: Array[Byte] = Array.ofDim[Byte](30)
      for (j <- 0.until(4)) {
        key(j) = ((i >> (j * 8)) & 0xFF).toByte
      }
      val value: Array[Byte] = Array.ofDim[Byte](100)
      ramcloud.write(tableId, key, value, null)
      // System.out.println("Wrote:" + i);
      reads(i) = new MultiReadObject(tableId, key)
    }
    val start: Long = System.nanoTime()
    ramcloud.read(reads)
    val time: Long = System.nanoTime() - start
    println(
      "Average multiread time per object: " + (time.toDouble / numTimes / 1000.0))
    }
    // System.out.println("filled table");
    // System.out.println("filled table");

  private def multiWriteTest(): Unit = {
    val numTimes: Int = 5000
    val writes: Array[MultiWriteObject] =
      Array.ofDim[MultiWriteObject](numTimes)
    for (i <- 0 until numTimes) {
      val key: Array[Byte] = Array.ofDim[Byte](30)
      for (j <- 0.until(4)) {
        key(j) = ((i >> (j * 8)) & 0xFF).toByte
      }
      val value: Array[Byte] = Array.ofDim[Byte](100)
      writes(i) = new MultiWriteObject(tableId, key, value)
    }
    val start: Long = System.nanoTime()
    ramcloud.write(writes)
    val time: Long = System.nanoTime() - start
    //System.out.printf("%d,%f\n", limit, ((double) time / numTimes / 1000.0));
    println(
      "Average multiwrite time per object: " + (time.toDouble / numTimes / 1000.0))
  }
  // System.out.println("filled table");
  // System.out.println("filled table");

  private def multiRemoveTest(): Unit = {
    val numTimes: Int = 5000
    val removes: Array[MultiRemoveObject] =
      Array.ofDim[MultiRemoveObject](numTimes)
    for (i <- 0 until numTimes) {
      val key: Array[Byte] = Array.ofDim[Byte](30)
      for (j <- 0.until(4)) {
        key(j) = ((i >> (j * 8)) & 0xFF).toByte
      }
      val value: Array[Byte] = Array.ofDim[Byte](100)
      removes(i) = new MultiRemoveObject(tableId, key)
    }
    val start: Long = System.nanoTime()
    ramcloud.remove(removes)
    val time: Long = System.nanoTime() - start
    //System.out.printf("%d,%f\n", limit, ((double) time / numTimes / 1000.0));
    println(
      "Average multiremove time per object: " + (time.toDouble / numTimes / 1000.0))
  }
  // System.out.println("filled table");
  // System.out.println("filled table");

  private def test(): Unit = {
    val numTimes: Int = 100
    var before: Long = 0L
    var elapsed: Long = 0L
    before = System.nanoTime()
    for (i <- 0 until numTimes) {}
    elapsed = System.nanoTime() - before
    System.out.printf("Average create object time: %.3f\n",
                      elapsed / 1000.0 / numTimes)
  }

  private def basicSpeedTest(): Unit = {
    val numTimes: Int = 100000
    var before: Long = 0L
    var elapsed: Long = 0L
    // Read tests
    val key: Array[Byte] = Array.ofDim[Byte](30)
    val value: Array[Byte] = Array.ofDim[Byte](100)
    ramcloud.write(tableId, key, value, null)
    val times: Array[Double] = Array.ofDim[Double](numTimes)
    println("time")
    for (i <- 0 until numTimes) {
      before = System.nanoTime()
      val unused: RAMCloudObject = ramcloud.read(tableId, key)
      elapsed = System.nanoTime() - before
      times(i) = elapsed / 1000.0
    }
    // System.out.printf("%d,%f\n", i, times[i]);
    // System.out.printf("%d,%f\n", i, times[i]);
    Arrays.sort(times)
    System.out.printf("Median Java read time: %.3f\n", times(numTimes / 2))
    ramcloud.remove(tableId, key)
    // Write tests
    before = System.nanoTime()
    for (i <- 0 until numTimes) {
      key(0) = (Math.random() * 255).toByte
      before = System.nanoTime()
      ramcloud.write(tableId, key, value, null)
      elapsed = System.nanoTime() - before
      times(i) = elapsed / 1000.0
      ramcloud.remove(tableId, key)
    }
    Arrays.sort(times)
    System.out.printf("Median Java write time: %.3f\n", times(numTimes / 2))
  }

  private def basicTest(): Unit = {
    // Do basic read/write/table/delete tests
    println("created table, id = " + tableId)
    val tableId2: Long = ramcloud.getTableId("hi")
    println("getTableId says tableId = " + tableId2)
    println(
      "wrote obj version = " +
        ramcloud.write(tableId, "thisIsTheKey", "thisIsTheValue"))
    val o: RAMCloudObject = ramcloud.read(tableId, "thisIsTheKey")
    println(
      "read object: key = [" + o.getKey + "], value = [" + o.getValue +
        "], version = " +
        o.getVersion)
  }

  private def enumerationTest(): Unit = {
    val numTimes: Int = 1000000
    val writes: Array[MultiWriteObject] = Array.ofDim[MultiWriteObject](numTimes)
    for (i <- 0 until numTimes) {
      writes(i) = new MultiWriteObject(tableId, "" + i, "" + i)
    }
    ramcloud.write(writes)
    val it: TableIterator = ramcloud.getTableIterator(tableId)
    var current: RAMCloudObject = null
    var count: Int = 0
    val start: Long = System.nanoTime()
    while (it.hasNext) {
      current = it.next()
      count += 1
    }
    val time: Long = System.nanoTime() - start
    println(
      "Average enumerate time per object: " + (time.toDouble / numTimes / 1000.0))
    println(count)
  }
  // Test Table Enumeration
  // System.out.println("filled table");
  // Test Table Enumeration
  // System.out.println("filled table");

}

