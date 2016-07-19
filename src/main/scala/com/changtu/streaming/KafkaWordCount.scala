package com.changtu.streaming

/**
  * Created by lubinsu on 6/30/2016.
  * Kafka 流计算示例 WordCount
  */

import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import org.joda.time.DateTime

object KafkaWordCount {
  def main(args: Array[String]) {
    if (args.length < 4) {
      System.err.println("Usage: KafkaWordCount <zkQuorum> <group> <topics> <numThreads>")
      System.exit(1)
    }

    val Array(zkQuorum, group, topics, numThreads) = args
    val sparkConf = new SparkConf().setAppName("KafkaWordCount")
    val ssc = new StreamingContext(sparkConf, Seconds(2))
    ssc.checkpoint("checkpoint")

    val topicMap = topics.split(",").map((_, numThreads.toInt)).toMap
    val lines = KafkaUtils.createStream(ssc, zkQuorum, group, topicMap).map(_._2)
    val words = lines.flatMap(_.split("\001"))
    val wordCounts = words.map(x => (x, 1L))
      .reduceByKeyAndWindow(_ + _, _ - _, Minutes(5), Seconds(10), 2)
    wordCounts.count().print()
      //.repartition(1).saveAsTextFiles("hdfs://nameservice1/user/hadoop/bigdata/biglogWordCount-".concat(DateTime.now().toString("yyyyMMddHHmmSS")))

    ssc.start()
    ssc.awaitTermination()
  }
}
