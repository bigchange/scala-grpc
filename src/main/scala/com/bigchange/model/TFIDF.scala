package com.bigchange.model

/**
  * Created by Jerry on 2017/5/18.
  */
import java.io.{File => JavaFile}

import breeze.linalg.{SparseVector, norm}
import com.hankcs.hanlp.HanLP
import org.apache.spark.mllib.feature.{HashingTF, IDF, IDFModel}
import org.apache.spark.mllib.linalg.{Vector, SparseVector => SV}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.reflect.io.File

/**
  * Created by Administrator on 2016/1/14.
  *  spark mllib 中的tf-idf 算法计算文档相似度
  */
object TFIDF {

  val conf = new SparkConf().setAppName("TfIdfTest").setMaster("local")
  val sc = new SparkContext(conf)
  sc.setLogLevel("ERROR")

  val testResultPath = "/Users/devops/workspace/shell/jobtitle/JobTitle/test_result"

  val result = new ListBuffer[(String, String, Double)]

  val saveFile = "/Users/devops/workspace/shell/jobtitle/JobTitle/src-map"

  val dataMap = sc.textFile(saveFile).map(x => x.split("\t")).map(x => (x(0).toLong, x(1)))
    .collectAsMap()

  // feature number
  val hashingTF = new HashingTF(Math.pow(2, 18).toInt)
  /**
    * 分词
    * 得分到关键分词的结果
    * 得到关键字
    * 得到关键短语
    * 用来得到这个txt的tfidf向量值
    * 计算相似度
    * @param txt
    * @return
    */
  def segment(txt:String) = {
    val lb = new ListBuffer[String]
    val keyWords = HanLP.extractKeyword(txt, 8)
    val phrases = HanLP.extractPhrase(txt, 8)
    keyWords.addAll(phrases)
    // val list = HanLP.segment(txt)
    val size = keyWords.size()
    for (i <- 0 to size - 1) {
      val item = keyWords.get(i)
      lb.+=(item)
    }
    val list = HanLP.segment(txt)
    for (j <- 0 to list.size() - 1) {
      val item = list.get(j).word
      lb.+=(item)
    }

    lb.distinct
  }

  /**
    * 用一个职位去对比其中与哪个最相似
    * 只取top5观看匹配相似度结果
    * @param idAndTFIDFVector
    * @param iDFModel
    * @param array
    */
  def cosSimilarity(idAndTFIDFVector: RDD[(Long, Vector)], iDFModel: IDFModel, array: Array[String]) = {

    // 删除指定职位top5相似职位的匹配结果文件（每一次跑 需复写 不然出现Exception）
    new File(new JavaFile(testResultPath)).deleteRecursively()
    array.foreach { x =>
      val iDF = generateFeatrues(x, iDFModel)
      val sv1 = iDF.asInstanceOf[SV]
      val bsv1 = new SparseVector[Double](sv1.indices, sv1.values, sv1.size)
      val ret = idAndTFIDFVector.map {
        case (id2, idf2) =>
          val mapId2Value = dataMap.get(id2).getOrElse("None")
          val sv2 = idf2.asInstanceOf[SV]
          val bsv2 = new SparseVector[Double](sv2.indices, sv2.values, sv2.size)
          val cosSim = bsv1.dot(bsv2) / (norm(bsv1) * norm(bsv2))
          (x, mapId2Value, cosSim)
      }
      val top5 = ret.top(10)(ord = Ordering.by(_._3))
      top5
      result.++=:(top5)
    }
    result
  }

  /**
    * 模型内部训练数据相似性对比
    * @param idAndTFIDFVector
    */
  def cosSimilarity(idAndTFIDFVector: RDD[(Long, Vector)]) = {
    // broadcast tf-idf vectors
    val idAndTFIDFVectorBroadCast = sc.broadcast(idAndTFIDFVector.collect())
    // cal doc cosineSimilarity
    val docSims = idAndTFIDFVector.flatMap {
      case (id1, idf1) =>
        // filter the same doc id
        val idfs = idAndTFIDFVectorBroadCast.value.filter(_._1 != id1)
        val sv1 = idf1.asInstanceOf[SV]
        import breeze.linalg._
        val bsv1 = new SparseVector[Double](sv1.indices, sv1.values, sv1.size)
        idfs.map {
          case (id2, idf2) =>
            val sv2 = idf2.asInstanceOf[SV]
            val bsv2 = new SparseVector[Double](sv2.indices, sv2.values, sv2.size)
            val cosSim = bsv1.dot(bsv2) / (norm(bsv1) * norm(bsv2))
            (id1, id2, cosSim)
        }
    }
    docSims.foreach(println)
  }

  /**
    * featrue 生成
    * @return
    */
  def generateFeatrues(txt:String, iDFModel: IDFModel) = {
    val tf = hashingTF.transform(segment(txt))
    val idf = iDFModel.transform(tf)
    idf
  }

  /**
    * data mapping
 *
    * @param file
    */
  def getDataMap(file: String) = {
    val src = sc.textFile(file).map(_.split("\t")(0))
    val dataSort= src.distinct().zipWithIndex().sortBy(_._2, ascending= true, numPartitions = 1)
    val dataMap = dataSort
      .map(x => (x._2 + 1, x._1))
      .mapValues(x => x.toLowerCase)
      .map(x => x._1  + "\t" + x._2)
    // 删除结果文件（exception）
    new File(new JavaFile(saveFile)).deleteRecursively()
    dataMap.saveAsTextFile("file://" + saveFile)
    println("saving map finished!!!!!")

  }

  /**
    * 按照结果集 职位名称数据集
    * 训练tfidf模型
    * @param file
    * @return
    */
  def trainModel(file:String) = {

    val src = sc.textFile(file).map(_.split("\t")(0))
    val dataSort= src.distinct().zipWithIndex().sortBy(_._2, ascending= true, numPartitions = 1)
    val data = dataSort.map(x => (segment(x._1), x._2 + 1))
    val idAndTFVector = data.map { case (seq, num) =>
      val tf = hashingTF.transform(seq)
      (num, tf)
    }
    idAndTFVector.cache()
    // build idf model
    val idf = new IDF().fit(idAndTFVector.values)
    // transform tf vector to tf-idf vector
    val idAndTFIDFVector = idAndTFVector.mapValues(v => idf.transform(v))
    idAndTFIDFVector.cache()

    (idf, idAndTFIDFVector, data)

  }

  /**
    * 标准tfidf模型训练流程化
    */
  def wholeProcess = {
    // Load documents (one per line).要求每行作为一个document,这里zipWithIndex将每一行的行号作为doc id
    val documents = sc.parallelize(Source.fromFile("J:\\github\\dataSet\\TFIDF-DOC").getLines()
      .filter(_.trim.length > 0).toSeq)
      .map(_.split(" ").toSeq)
      .zipWithIndex()
    // feature number
    val hashingTF = new HashingTF(Math.pow(2, 18).toInt)
    //line number for doc id，每一行的分词结果生成tf vector
    val idAndTFVector = documents.map {
      case (seq, num) =>
        val tf = hashingTF.transform(seq)
        (num + 1, tf)
    }

    idAndTFVector.cache()
    // build idf model
    val idf = new IDF().fit(idAndTFVector.values)
    // transform tf vector to tf-idf vector
    val idAndTFIDFVector = idAndTFVector.mapValues(v => idf.transform(v))
    // broadcast tf-idf vectors
    val idAndTFIDFVectorBroadCast = sc.broadcast(idAndTFIDFVector.collect())

    // cal doc cosineSimilarity
    val docSims = idAndTFIDFVector.flatMap {
      case (id1, idf1) =>
        // filter the same doc id
        val idfs = idAndTFIDFVectorBroadCast.value.filter(_._1 != id1)
        val sv1 = idf1.asInstanceOf[SV]
        import breeze.linalg._
        val bsv1 = new SparseVector[Double](sv1.indices, sv1.values, sv1.size)
        idfs.map {
          case (id2, idf2) =>
            val sv2 = idf2.asInstanceOf[SV]
            val bsv2 = new SparseVector[Double](sv2.indices, sv2.values, sv2.size)
            val cosSim = bsv1.dot(bsv2) / (norm(bsv1) * norm(bsv2))
            (id1, id2, cosSim)
        }
    }
    docSims.foreach(println)

    sc.stop()
  }

  def main(args: Array[String]) {

    // val trainData = "/Users/devops/workspace/shell/jobtitle/JobTitle/result/job_title_dict_src.txt"
    val trainData = "/Users/devops/workspace/shell/jd/result/*/*"
    // val testData = "/Users/devops/workspace/shell/jobtitle/JobTitle/position_dict.txt"

    /*val lb = sc.textFile(testData)
      .map(_.split("\t"))
      .map(x => x(0))
      .randomSplit(Array(0.8, 0.2))
      .apply(1).collect()*/

    val lb = Array("销售经理", "技术总监/产品总监", "美工", "前端开发工程师", "车间距离检测员")

    // 保存职位的map（训练tdidf模型的之前要确保map是最新的）
    // getDataMap(trainData)

   val model = trainModel(trainData)
   val saveResult = cosSimilarity(model._2, model._1, lb)
    sc.parallelize(saveResult).saveAsTextFile(path = "file://" + testResultPath)

    // 在一个指定相似度值之下的职位匹配，可以判断其为不相似（指定的相似度值初步可设定为0.5）
    println("predict finished!!")

  }
}
