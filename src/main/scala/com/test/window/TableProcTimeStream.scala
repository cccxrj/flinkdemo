package com.test.window

import java.util.Properties

import com.google.gson.Gson
import com.test.bean.User
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010
import org.apache.flink.table.api.scala._
import org.apache.flink.api.scala._

/**
  * author: zhangzhiqiang
  * date: 2019/7/5 16:22
  */
object TableProcTimeStream {

  var p: Properties = new Properties()
  p.setProperty("bootstrap.servers", "dev-hdp-2.huazhu.com:6667,dev-hdp-3.huazhu.com:6667,dev-hdp-4.huazhu.com:6667")

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val ds = env.addSource(new FlinkKafkaConsumer010[String]("user", new SimpleStringSchema(), p))
      .map(value => {
        new Gson().fromJson(value, classOf[User])
      })

    val tableEnv = StreamTableEnvironment.create(env)

    tableEnv.registerDataStream("users", ds, 'userId, 'name, 'age, 'sex, 'createTime, 'updateTime, 'procTime.proctime)

    val table = tableEnv.sqlQuery("select userId,name,age,sex,createTime,updateTime from users")

    // 将 procTime设置为时间属性，将 userId设置为 主键
    val rates = table.createTemporalTableFunction("procTime", "userId")
    //tableEnv.registerFunction(""，rates)



    //    tableEnv.toAppendStream(table, classOf[User])
    //    tableEnv.toAppendStream()

    env.execute()
  }

}