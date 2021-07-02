package com.atguigu.app

import java.text.SimpleDateFormat
import java.util.Date

import com.alibaba.fastjson.JSON
import com.atguigu.bean.StartUpLog
import com.atguigu.constants.GmallConstants
import com.atguigu.utils.MyKafkaUtil
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * @author gxf
 * @create 2021-07-02 11:50
 */
object DauApp {
  def main(args: Array[String]): Unit = {
    //1.创建ssc连接
    val sparkConf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("APP")

    val ssc: StreamingContext = new StreamingContext(sparkConf,Seconds(6))

    val startStream: InputDStream[ConsumerRecord[String, String]] = MyKafkaUtil.getKafkaStream(GmallConstants.KAFKA_TOPIC_STARTUP,ssc)

    val dateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH")

    val value: DStream[StartUpLog] = startStream.mapPartitions(partition => {
      partition.map(record => {
        //转为样例类
        val log: StartUpLog = JSON.parseObject(record.value(), classOf[StartUpLog])
        //补充字段
        val str: String = dateFormat.format(new Date(log.ts))

        log.logDate = str.split(" ")(0)
        log.logHour = str.split(" ")(1)
        log
      })
    })
    value.print()


//    startStream.foreachRDD(rdd=>{
//      rdd.foreach(record=>{
//        println(record.value())
//      })
//    })


    ssc.start()
    ssc.awaitTermination()
  }

}
